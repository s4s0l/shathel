package org.s4s0l.shathel.commons.cert

/**
 * @author Marcin Wielgus
 */
interface CertificateManager {
    File getRootCaKey()

    File getRootCaCert()


    KeyCertCa getClientCerts()

    Optional<KeyCert> getKeyAndCert(String tag)

    KeyCert generateKeyAndCert(String tag, String commonName,
                               List<String> dns,
                               List<String> ips,
                               List<String> extendsdKeyUsage)

    KeyCert generateKeyAndCert(String tag, String commonName,
                               List<String> dns,
                               List<String> ips)

    KeyCert generateKeyAndCert(String tag,
                               List<String> dns,
                               List<String> ips,
                               List<String> extendsdKeyUsage)

    KeyCert generateKeyAndCert(String tag,
                               List<String> dns,
                               List<String> ips)

    KeyCert generateKeyAndCert(String tag,
                               List<String> addresses)

    void afterEnvironmentDestroyed()
}

class KeyCert {
    KeyCert(File key, File cert) {
        this.key = key
        this.cert = cert
    }
    final File key
    final File cert
}

class KeyCertCa extends KeyCert {
    KeyCertCa(File key, File cert, File ca) {
        super(key, cert)
        this.ca = ca
    }
    final File ca
}