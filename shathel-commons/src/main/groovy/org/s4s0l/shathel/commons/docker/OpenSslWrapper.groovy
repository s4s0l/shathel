package org.s4s0l.shathel.commons.docker

import org.s4s0l.shathel.commons.utils.ExecWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Marcin Wielgus
 */
class OpenSslWrapper {
    private static
    final Logger LOGGER = LoggerFactory.getLogger(OpenSslWrapper.class);

    final ExecWrapper exec = new ExecWrapper(LOGGER, "openssl")

    def generateKeyPair(String cn,
                        List<String> ips, List<String> dns,
                        String keyOutFile, String certOutFile) {
        LOGGER.info("openssl: generating key $keyOutFile for ${cn} (${ips.join(",")})")
        new File(keyOutFile).getParentFile().mkdirs()
        new File(certOutFile).getParentFile().mkdirs()
        File tmp = File.createTempFile("sslcfg", "cnf");
        tmp.text = """
${OpenSslWrapper.getResource('/openssl.cnf').text}
[ v3_xxx ]
basicConstraints = critical, CA:true
keyUsage=critical, digitalSignature,keyEncipherment,keyAgreement,dataEncipherment,keyCertSign
extendedKeyUsage=critical,serverAuth,clientAuth,codeSigning
subjectAltName=${(ips.collect { "IP:$it" } + dns.collect { "DNS:$it" }).join(",")}
"""
        exec.executeForOutput("""
            req -newkey rsa:4096 -nodes -sha256 -x509 -days 365 -subj /CN=${cn}
            -extensions v3_xxx -config ${tmp.absolutePath}
            -keyout ${keyOutFile} 
            -out ${certOutFile}
        """.replace("\n", ""))

    }

    def getCertInfo(String file) {
        exec.executeForOutput("x509 -in $file -text -noout")
    }

}
