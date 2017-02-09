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

    def waitAndGetClient(String address, int expectedStatus = 200, int maxAttempts = 30) {
        def portainer = new RESTClient(address)
        portainer.handler['404'] = portainer.handler.get(Status.SUCCESS)

        int attempt = 0
        while (true) {
            attempt++
            try {
                HttpResponseDecorator result = portainer.get(
                        path: '/'
                )
                if (result.status != 200) {
                    throw new Exception("Non 200 resp")
                } else {
                    log "$address ready."
                    break;
                }
            } catch (Exception e) {
                log "$address not ready..."
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
