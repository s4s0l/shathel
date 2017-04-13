package org.s4s0l.shathel.commons.cert

import org.apache.commons.io.FileUtils
import org.s4s0l.shathel.commons.docker.DockerWrapper
import org.s4s0l.shathel.commons.docker.OpenSslWrapper
import spock.lang.Specification

/**
 * @author Marcin Wielgus
 */
class CertificateManagerImplTest extends Specification {

    String getRootDirName() {
        "build/Test${getClass().getSimpleName()}"
    }

    File getRootDir() {
        return new File(getRootDirName())
    }

    def setup() {
        FileUtils.deleteDirectory(getRootDir())
    }

    def "Generates Root key and cert when client cert is requested"() {
        given:
        def certManager = new CertificateManagerImpl(getRootDir(), "pass".bytes, "commonCaName")

        when:
        def cert = certManager.generateClientKeyAndCert()

        then:
        cert.key.exists()
        cert.cert.exists()
        cert.ca.exists()
        //test if in same directory
        cert.key.getParent() == cert.ca.getParent()
        cert.ca.getName() == "ca.pem"
        cert.cert.getName() == "cert.pem"
        cert.key.getName() == "key.pem"

        new File(getRootDir(), "ca-key.pem").exists()
        new File(getRootDir(), "ca-cert.pem").exists()
        new OpenSslWrapper().getCertInfo(cert.cert.absolutePath).contains("TLS Web Client Authentication")
        new OpenSslWrapper().getCertInfo(certManager.getRootCaCert().absolutePath).contains("commonCaName")
    }


    def "Generates Server certificates with proper key usage and subject names"() {
        given:
        def certManager = new CertificateManagerImpl(getRootDir(), "pass".bytes, "commonCaName")

        when:
        def cert = certManager.generateKeyAndCert("xxx", "serverCommonName", ["dns1"], ["1.1.1.1"])

        then:
        cert.key.exists()
        cert.cert.exists()

        cert.key.getParent() == cert.cert.getParent()
        cert.cert.getParentFile().getName() == "xxx"
        cert.cert.getName() == "server-cert.pem"
        cert.key.getName() == "server-key.pem"

        new OpenSslWrapper().getCertInfo(cert.cert.absolutePath).contains("TLS Web Client Authentication")
        new OpenSslWrapper().getCertInfo(cert.cert.absolutePath).contains("1.1.1.1")
        new OpenSslWrapper().getCertInfo(cert.cert.absolutePath).contains("dns1")

    }

}
