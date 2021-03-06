package org.s4s0l.shathel.commons.localswarm

import org.s4s0l.shathel.commons.Shathel
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.commons.docker.DockerWrapper
import testutils.BaseIntegrationTest

/**
 * @author Marcin Wielgus
 */
class LocalSwarmAnsibleProvisionerTest extends BaseIntegrationTest {

    @Override
    def setupEnvironment() {
        environmentName = "local"
    }


    def cleanupEnvironment() {
        [new File(getRootDir(), "out1.txt"),
         new File(getRootDir(), "out1-1.txt"),
         new File(getRootDir(), "out2.txt"),
         new File(getRootDir(), "out-g-file.txt"),
         new File(getRootDir(), "out-g-inline.txt"),
         new File(getRootDir(), "out-d-file.txt"),
         new File(getRootDir(), "out-d-inline.txt")].each {
            if (it.exists())
                it.delete()
        }
        new DockerWrapper().with {
            if (swarmActive()) {
                stackUnDeploy(new File("."), "ansible")
                stackUnDeploy(new File("."), "d-ansible")
            }
        }
    }

    def "Ansible provisionners should run"() {
        given:
        Shathel sht = shathel([
                ("shathel.env.${environmentName}.target.dir".toString())     : getRootDir().absolutePath,
                ("shathel.env.${environmentName}.ansible.enabled".toString()): "true",
                ("shathel.env.${environmentName}.xxx".toString())            : "xxx",
        ])
        def solution = sht.getSolution(sht.initStorage(getRootDir(), false))
        def environment = solution.getEnvironment(environmentName)
        if (!environment.isInitialized()) {
            environment.initialize()
        }


        when:
        def stack = solution.openStack(new StackReference("org.s4s0l.shathel:ansible:1.0"))
        def command = stack.createStartCommand(false, environment)
        solution.run(command)


        then:
        new File(getRootDir(), "out1.txt").text == "[\"127.0.0.1\"]"
        new File(getRootDir(), "out1-1.txt").text == "xxx=xxx"
        new File(getRootDir(), "out2.txt").text == "[\"127.0.0.1\"]"
        new File(getRootDir(), "out-g-file.txt").text == "Groovy 2 ansible"
        new File(getRootDir(), "out-g-inline.txt").text == "Groovy 2 ansible"
        new File(getRootDir(), "out-d-file.txt").text == "Groovy 2 ansible"
        new File(getRootDir(), "out-d-inline.txt").text == "Groovy 2 ansible"

        onEnd()

    }

    def "Ansible provisionners should not run by default on local swarm"() {
        given:
        Shathel sht = shathel([
                ("shathel.env.${environmentName}.target.dir".toString()): getRootDir().absolutePath,
        ])
        def solution = sht.getSolution(sht.initStorage(getRootDir(), false))
        def environment = solution.getEnvironment(environmentName)
        if (!environment.isInitialized()) {
            environment.initialize()
        }


        when:
        def stack = solution.openStack(new StackReference("org.s4s0l.shathel:ansible:1.0"))
        def command = stack.createStartCommand(false, environment)
        solution.run(command)


        then:
        !new File(getRootDir(), "out1.txt").exists()
        !new File(getRootDir(), "out2.txt").exists()
        !new File(getRootDir(), "out-g-file.txt").exists()
        !new File(getRootDir(), "out-g-inline.txt").exists()
        !new File(getRootDir(), "out-d-file.txt").exists()
        !new File(getRootDir(), "out-d-inline.txt").exists()

        onEnd()

    }

}
