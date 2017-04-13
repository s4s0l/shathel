package org.s4s0l.shathel.commons.ssh

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.utils.ExecWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class SshKeyProvider {
    private static
    final Logger LOGGER = LoggerFactory.getLogger(SshKeyProvider.class);
    private final File keyDirectory
    private final String email
    private final ExecWrapper exec = new ExecWrapper(LOGGER, "ssh-keygen", [:])

    SshKeyProvider(File keyDirectory, String email) {
        this.keyDirectory = keyDirectory
        this.email = email
    }

    private File getPublicKey() {
        return new File(keyDirectory, "id_rsa.pub")
    }

    private File getPrivateKey() {
        return new File(keyDirectory, "id_rsa")
    }

    SshKeys getKeys() {
        if (!(publicKey.exists() && privateKey.exists())) {
            exec.executeForOutput("-t", "rsa", "-C", "${email}", "-N", "", "-f", "${privateKey.absolutePath}")
        }
        return new SshKeys(publicKey, privateKey)
    }
}

@TypeChecked
@CompileStatic
class SshKeys {
    SshKeys(File publicKey, File privateKey) {
        this.publicKey = publicKey
        this.privateKey = privateKey
    }
    final File publicKey
    final File privateKey
}