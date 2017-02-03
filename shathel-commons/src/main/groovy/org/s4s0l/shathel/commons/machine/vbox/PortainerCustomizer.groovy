package org.s4s0l.shathel.commons.machine.vbox

import groovy.json.JsonSlurper
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import groovyx.net.http.Status
import org.apache.commons.lang.StringUtils
import org.apache.http.entity.mime.FormBodyPart
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.s4s0l.shathel.commons.swarm.SwarmClusterWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static groovyx.net.http.ContentType.JSON

class PortainerCustomizer {
    private static
    final Logger LOGGER = LoggerFactory.getLogger(PortainerCustomizer.class);

    def log(String x) {
        LOGGER.info("-----portainer:" + x);
    }


    def customizePortainer(int portainerPort,
                           String ip, SwarmClusterWrapper machine) {
        String adminPassword = "qwerty"
        def json = new JsonSlurper()

        def portainer = new RESTClient("http://${ip}:${portainerPort}")
        portainer.handler['404'] = portainer.handler.get(Status.SUCCESS)

        int attempt = 0, maxAttempts = 15;
        while (true) {
            attempt++
            try {
                log "testing http://${ip}:${portainerPort}..."
                HttpResponseDecorator result = portainer.get(
                        path: '/'
                )
                if (result.status != 200) {
                    throw new Exception("Non 200 resp")
                } else {
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

        log "Checking if already initialized"

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


        portainer.encoder.'multipart/form-data' = {
            File file ->
                final MultipartEntity e = new MultipartEntity(
                        HttpMultipartMode.STRICT)
                e.addPart(new FormBodyPart('file', new FileBody(file, file.getName(), 'application/x-x509-ca-cert', 'UTF8')))
                e
        }

        machine.getAllNodeNames()
                .each {
            def machineName = it
            def envs = machine.getMachineEnvs(machineName)
            def certPath = envs['DOCKER_CERT_PATH']
            def machineIp = envs['DOCKER_HOST']
            def tls = !StringUtils.isEmpty(certPath)
            log "Adding $machineName as endpoint"
            result = portainer.post(
                    requestContentType: JSON,
                    contentType: JSON,
                    query: [active: false],
                    path: '/api/endpoints',
                    headers: [Authorization: "Bearer $token"],
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
                            headers: [Authorization: "Bearer $token"],
                            body: new File(certPath, "${fileName}.pem"),
                    )
                    assert result.status == 200
                }


                uploadFile "ca"
                uploadFile "cert"
                uploadFile "key"
            }
        }

    }
}