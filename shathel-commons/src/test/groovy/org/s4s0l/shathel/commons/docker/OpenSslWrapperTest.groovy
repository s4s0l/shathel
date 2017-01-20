package org.s4s0l.shathel.commons.docker

import spock.lang.Specification

/**
 * @author Matcin Wielgus
 */
class OpenSslWrapperTest extends Specification {

    def  "should generate keys withproper key usageand extensions"(){
        given:
        OpenSslWrapper w = new OpenSslWrapper()

        when:
        w.generateKeyPair("someCommonName",
        ["1.2.3.4","1.1.1.1"],
        ["alterNateDns"],
        "build/tmp/z/out.key", "./build/tmp/z/out.crt")

        then:
        new File("build/tmp/z/out.key").exists()
        new File("build/tmp/z/out.crt").exists()

        w.getCertInfo("build/tmp/z/out.crt").contains("alterNateDns")
        w.getCertInfo("build/tmp/z/out.crt").contains("1.2.3.4")
    }
}
