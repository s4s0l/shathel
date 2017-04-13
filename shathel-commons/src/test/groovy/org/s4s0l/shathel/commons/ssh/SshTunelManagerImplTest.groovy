package org.s4s0l.shathel.commons.ssh

import org.s4s0l.shathel.commons.docker.DockerWrapper
import spock.lang.Specification

/**
 * @author Marcin Wielgus
 */
class SshTunelManagerImplTest extends Specification {
    String getRootDirName() {
        "build/TestSshTunel"
    }

    File getRootDir() {
        return new File(getRootDirName())
    }

    def setup() {
        new DockerWrapper().with {
            if (containerExists("TestSshTunelServer"))
                containerRemove("TestSshTunelServer")
        }
    }

    def "Ssh Tunel manager opens tunel"() {
        given:
        getRootDir().mkdirs()
        def knownHosts = new File(getRootDir(), "known")
        knownHosts.delete()
        SshKeys keys = new SshKeyProvider(getRootDir(), "someone@somewhere").keys
        def tunelManager = new SshTunelManagerImpl(getRootDir(), "root", 3333, knownHosts)
        def id = new DockerWrapper().containerCreate("-d --name TestSshTunelServer --rm -p 2224:22 -v ${keys.publicKey.absolutePath}:/root/.ssh/authorized_keys macropin/sshd")
        Thread.sleep(5000)
        when:
        def port = tunelManager.openTunnel(keys.privateKey, "localhost:2224", "github.com:80")

        then:
        "curl -s http://127.0.0.1:${port}".execute().waitFor() == 0

        cleanup:
        tunelManager.closeAll()
        new DockerWrapper().containerRemove("TestSshTunelServer")

    }

}
