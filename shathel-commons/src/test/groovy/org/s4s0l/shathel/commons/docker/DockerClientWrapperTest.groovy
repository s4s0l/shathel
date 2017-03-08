package org.s4s0l.shathel.commons.docker

import spock.lang.Specification

/**
 * @author Matcin Wielgus
 */
class DockerClientWrapperTest extends Specification {

    def testInfoLocal() {
        given:
        def client = new DockerClientWrapper([:])
        when:
        def info = client.info()

        then:
        info != null
    }




}
