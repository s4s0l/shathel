#!/usr/bin/env groovy
import groovy.json.JsonSlurper
@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7.1')
@Grab('org.apache.httpcomponents:httpmime:4.2.1')
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import org.apache.http.entity.mime.FormBodyPart
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody

import static groovyx.net.http.ContentType.JSON


def json = new JsonSlurper()
def clusterName = args.size() > 0 ? args[0] : "consul"
def adminPassword = args.size() > 1 ? args[1] : "adminadmin"
def portainerPort = 9000
def ip = "docker-machine ip ${clusterName}-manager-1".execute().text.trim()


def log(String x){
    println "-----portainer:" +x
}

def portainer = new RESTClient("http://${ip}:${portainerPort}")

int attempt = 0, maxAttempts = 10;
while (true) {
    attempt++
    try {
        log "testing http://${ip}:${portainerPort}..."
        HttpResponseDecorator result = portainer.get(
                path: '/'
        )
        if (result.status != 200) {
            throw new Exception("Non 200 resp")
        }else{
            log "http://${ip}:${portainerPort} ready."
            break;
        }
    } catch (Exception e) {
        log "http://${ip}:${portainerPort} not ready..."
        if (attempt < maxAttempts) {
            sleep(1000)
        } else {
            throw new Exception("http://${ip}:${portainerPort} not ready!")
        }
    }


}
log "Initiating password"

HttpResponseDecorator result = portainer.post(
        requestContentType: JSON,
        contentType: JSON,
        path: '/api/users/admin/init',
        body: [password: adminPassword]
)
assert result.status == 200

log "Getting token"

result = portainer.post(
        requestContentType: JSON,
        contentType: JSON,
        path: '/api/auth',
        body: [username: "admin", password: adminPassword]
)
assert result.status == 200

def token = result.data.jwt;

//////////////////////////////ADDING NODES
portainer.encoder.'multipart/form-data' = {
    File file ->
        final MultipartEntity e = new MultipartEntity(
                HttpMultipartMode.STRICT)
        e.addPart(new FormBodyPart('file', new FileBody(file, file.getName(), 'application/x-x509-ca-cert', 'UTF8')))
        e
}

"docker-machine ls -q --filter name=${clusterName}-.*".execute().text.split("\n").each {
    def machineName = it
    def machineEnvs = "docker-machine env ${it}".execute().text
    def certPath = (machineEnvs =~ /[^\s]+ DOCKER_CERT_PATH="(.+)"/)[0][1]
    def machineIp = (machineEnvs =~ /[^\s]+ DOCKER_HOST="(.+)"/)[0][1]
    log "Adding $machineName as endpoint"
    result = portainer.post(
            requestContentType: JSON,
            contentType: JSON,
            query: [active: false],
            path: '/api/endpoints',
            headers: [Authorization: "Bearer $token"],
            body: [Name: machineName, URL: machineIp, TLS: true]
    )
    assert result.status == 200
    def endpointId = result.data.Id
    def uploadFile = { String fileName ->
        log "Uploading $fileName to $machineName endpoint"
        result = portainer.post(
                requestContentType: 'multipart/form-data',
                contentType: JSON,
                path: "/api/upload/tls/$endpointId/$fileName",
                headers: [Authorization: "Bearer $token"],
                body: new File(certPath, "${fileName}.pem"),
        )
        assert result.status == 200
    }


    uploadFile "ca"
    uploadFile "cert"
    uploadFile "key"
}






