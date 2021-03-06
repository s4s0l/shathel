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


    def "Ssh Tunel manager can copy file"() {
        given:
        getRootDir().mkdirs()
        def knownHosts = new File(getRootDir(), "known")
        knownHosts.delete()
        SshKeys keys = new SshKeyProvider(getRootDir(), "someone@somewhere").keys
        def tunelManager = new SshTunelManagerImpl(getRootDir(), "root", 3333, knownHosts)
        def id = new DockerWrapper().containerCreate("-d --name TestSshTunelServer --rm -p 2224:22 -v ${keys.publicKey.absolutePath}:/root/.ssh/authorized_keys docker.io/panubo/sshd")
        Thread.sleep(5000)

        when:
        tunelManager.scp(keys.privateKey, "localhost:2224", keys.privateKey, "/testFile")

        then:
        tunelManager.exec(keys.privateKey, "localhost:2224", "cat /testFile").output.trim() == keys.privateKey.text.trim()
        tunelManager.sudo(keys.privateKey, "localhost:2224", "cat /testFile").output.trim() == "ash: sudo: not found"

        cleanup:
        tunelManager?.closeAll()
        new DockerWrapper().containerRemove("TestSshTunelServer")

    }


    def "Ssh Tunel manager opens tunel"() {
        given:
        getRootDir().mkdirs()
        def knownHosts = new File(getRootDir(), "known")
        knownHosts.delete()
        SshKeys keys = new SshKeyProvider(getRootDir(), "someone@somewhere").keys
        def tunelManager = new SshTunelManagerImpl(getRootDir(), "root", 3333, knownHosts)
        def id = new DockerWrapper().containerCreate("-d --name TestSshTunelServer --rm -p 2224:22 -v ${keys.publicKey.absolutePath}:/root/.ssh/authorized_keys docker.io/panubo/sshd")
        Thread.sleep(5000)

        when:
        def port = tunelManager.openTunnel(keys.privateKey, "localhost:2224", "github.com:80")

        then:
        "curl -s http://127.0.0.1:${port}".execute().waitFor() == 0

        cleanup:

        tunelManager?.closeAll()
        new DockerWrapper().containerRemove("TestSshTunelServer")

    }


    def "Multiple Ssh tunnel managers share state"() {
        given:
        getRootDir().mkdirs()
        def knownHosts = new File(getRootDir(), "known")
        knownHosts.delete()
        SshKeys keys = new SshKeyProvider(getRootDir(), "someone@somewhere").keys
        def tunelManager = new SshTunelManagerImpl(getRootDir(), "root", 3333, knownHosts)
        def tunelManager2 = new SshTunelManagerImpl(getRootDir(), "root", 3333, knownHosts)

        def id = new DockerWrapper().containerCreate("-d --name TestSshTunelServer --rm -p 2224:22 -v ${keys.publicKey.absolutePath}:/root/.ssh/authorized_keys docker.io/panubo/sshd")
        Thread.sleep(5000)

        when:
        def port = tunelManager.openTunnel(keys.privateKey, "localhost:2224", "github.com:80")
        def port2 = tunelManager2.openTunnel(keys.privateKey, "localhost:2224", "github.com:80")


        then:
        "curl -s http://127.0.0.1:${port}".execute().waitFor() == 0
        port == port2

        cleanup:
        tunelManager.closeAll()
        new DockerWrapper().containerRemove("TestSshTunelServer")

    }

}
