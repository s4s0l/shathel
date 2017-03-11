import groovy.json.JsonSlurper
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import groovyx.net.http.Status
import org.apache.commons.lang.StringUtils
import org.apache.http.entity.mime.FormBodyPart
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.s4s0l.shathel.commons.core.environment.ExecutableApiFacade
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.core.environment.StackCommand
import org.s4s0l.shathel.commons.scripts.HttpApis

import static groovyx.net.http.ContentType.JSON


def log(String x) {
    LOGGER.info("-----portainer:" + x);
}


EnvironmentContext environmentContext = context;
ExecutableApiFacade apii = api;
StackCommand stackCommand = command;
HttpApis httpApi = http

String ip = apii.getIpForManagementNode();
int portainerPort = 9001
String adminPassword = "qwerty"
def address = "http://${ip}:${portainerPort}"
def portainer = httpApi.waitAndGetClient(address)
def AUTH_ON = false
def HEADERS = [:]
log "Checking if already initialized"

if (AUTH_ON) {
    HttpResponseDecorator result = portainer.post([
            requestContentType: JSON,
            contentType       : JSON,
            path              : '/api/auth',
            body              : [username: "admin", password: adminPassword]]

    )
    if (result.status != 200) {
        log "Initiating password"

        result = portainer.post(
                requestContentType: JSON,
                contentType: JSON,
                path: '/api/users/admin/init',
                body: [password: adminPassword]
        )
        assert result.status == 200

    }
    log "Getting token"

    result = portainer.post(
            requestContentType: JSON,
            contentType: JSON,
            path: '/api/auth',
            body: [username: "admin", password: adminPassword]
    )
    assert result.status == 200


    def token = result.data.jwt;
    HEADERS = [Authorization: "Bearer $token"]
}

result = portainer.get(
        contentType: JSON,
        path: '/api/endpoints',
        headers: HEADERS,
)

def nodesDefined = result.data.collect { it.Name }


portainer.encoder.'multipart/form-data' = {
    File file ->
        final MultipartEntity e = new MultipartEntity(
                HttpMultipartMode.STRICT)
        e.addPart(new FormBodyPart('file', new FileBody(file, file.getName(), 'application/x-x509-ca-cert', 'UTF8')))
        e
}

apii.getNodeNames()
        .findAll { !nodesDefined.contains(it) }
        .each {

    def machineName = it
    def envs = apii.getDockerEnvs(machineName)
    def certPath = envs['DOCKER_CERT_PATH']
    def machineIp = envs['DOCKER_HOST']
    if (machineIp == null) {
        return;
    }
    def tls = !StringUtils.isEmpty(certPath)
    log "Adding $machineName as endpoint"
    result = portainer.post(
            requestContentType: JSON,
            contentType: JSON,
            query: [active: false],
            path: '/api/endpoints',
            headers: HEADERS,
            body: [Name: machineName, URL: machineIp, TLS: tls]
    )
    assert result.status == 200
    if (tls) {
        def endpointId = result.data.Id
        def uploadFile = { String fileName ->
            log "Uploading $fileName to $machineName endpoint"
            result = portainer.post(
                    requestContentType: 'multipart/form-data',
                    contentType: JSON,
                    path: "/api/upload/tls/$endpointId/$fileName",
                    headers: HEADERS,
                    body: new File(certPath, "${fileName}.pem"),
            )
            assert result.status == 200
        }


        uploadFile "ca"
        uploadFile "cert"
        uploadFile "key"
    }
}

