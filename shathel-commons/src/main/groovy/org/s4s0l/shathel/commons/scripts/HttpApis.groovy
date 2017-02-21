package org.s4s0l.shathel.commons.scripts

import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import groovyx.net.http.Status
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Matcin Wielgus
 */
class HttpApis {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpApis.class)

    def log(String x) {
        LOGGER.info(x);
    }

    def waitAndGetClient(String address, List<String> expectedStatus = [200,401], String path = "/", int maxAttempts = 30) {
        def portainer = new RESTClient(address)
        portainer.handler[Status.FAILURE] = portainer.handler.get(Status.SUCCESS)
        portainer.handler["$expectedStatus"] = portainer.handler.get(Status.SUCCESS)
        def ok = expectedStatus
        int attempt = 0
        while (true) {
            attempt++
            try {
                HttpResponseDecorator result = portainer.get(
                        path: path
                )
                if (!ok.contains(result.status)) {
                    throw new Exception("Non ${ok} resp, got ${result.status}")
                } else {
                    log "$address ready."
                    break;
                }
            } catch (Exception e) {
                log "$address not ready...(${e.getMessage()})"
                if (attempt < maxAttempts) {
                    sleep(1000)
                } else {
                    throw new Exception("$address not ready!")
                }
            }
        }
        return portainer;
    }
}
