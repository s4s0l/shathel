package org.s4s0l.shathel.commons.docker

import spock.lang.Specification

/**
 * @author Marcin Wielgus
 */
class OpenSslWrapperTest extends Specification {
    def getRootDir() {
        return "build/Test${getClass().getSimpleName()}"
    }

    def "should generate keys withproper key usageand extensions"() {
        given:
        OpenSslWrapper w = new OpenSslWrapper()

        when:
        w.generateKeyPair("someCommonName",
                ["1.2.3.4", "1.1.1.1"],
                ["alterNateDns"],
                "${rootDir}/out.key", "./${rootDir}/out.crt")

        then:
        new File("${rootDir}/out.key").exists()
        new File("${rootDir}/out.crt").exists()

        w.getCertInfo("${rootDir}/out.crt").contains("alterNateDns")
        w.getCertInfo("${rootDir}/out.crt").contains("1.2.3.4")
    }
}
