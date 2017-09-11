package org.s4s0l.shathel.deployer.itg

import java.io.File

import org.s4s0l.shathel.commons.utils.Utils

/**
  * @author Marcin Wielgus
  */
class IntegrationTests extends AbstractShellIntegrationTest {


  //  feature("Ssh") {
  //    scenario("sshing") {
  //      ssh cmd --nodeName manager-1 --cmd echo asasas
  //      ssh tunnel --name manager-1 --host www.google.pl --port adasd
  //    }
  //  }

  //
  // its just a spec for how should it work
  // feature("Nodes") {
  //    scenario("listing and labelling nodes") {
  //      assertCmdSuccess("environment init")
  //      assertCmdSuccess("node label --name localhost --labels --force")
  //      assertCmdYmlResult("node ls",
  //        """
  //          |- name: integrationtests-local-manager-1
  //          |  role: manager
  //          |  publicIp: 127.0.0.1
  //          |  privateIp: 127.0.0.1
  //          |  labels: {}
  //        """)
  //      assertCmdYmlResult("node inspect --name integrationtests-local-manager-1",
  //        """
  //          |name: integrationtests-local-manager-1
  //          |role: manager
  //          |publicIp: 127.0.0.1
  //          |privateIp: 127.0.0.1
  //          |labels: {}
  //        """)
  //      assertCmdYmlResult("node label --name 1 --labels aaa=aaa",
  //        """aaa: aaa
  //        """)
  //      assertCmdYmlResult("node label --name 1",
  //        """aaa: aaa
  //        """)
  //      assertCmdYmlResult("node label --name manager-1 --labels aaa=bbb,bbb=BBB",
  //        """aaa: bbb
  //          |bbb: BBB
  //        """)
  //      assertCmdYmlResult("node label --name manager-1 --labels aaa=ccc",
  //        """aaa: ccc
  //          |bbb: BBB
  //        """)
  //      assertCmdYmlResult("node label --name manager-1 --labels aaa=,bbb=CCC",
  //        """bbb: CCC""")
  //      assertCmdResult("node label manager-1 --labels bbb=", "{}")
  //      assertCmdResult("node label --name localhost --labels --force", "{}")
  //      assertCmdYmlResult("node label --name 1 --labels aaa=aaa", """aaa: aaa""")
  //      assertCmdYmlResult("node label --name 1 --labels ccc=ccc --force", "ccc: ccc")
  //      assertCmdYmlResult("node label --name 1 --labels --force", "")
  //    }
  //  }
  //
  //
  //  feature("Volumes") {
  //    scenario("listing and deleting volumes") {
  //      assertCmdSuccess("environment init")
  //      assertCmdSuccess("stack purge")
  //      assertCmdSuccess("stack start org.s4s0l.shathel:volume1:1.0")
  //      //      Print warning if has different labels
  //      assertCmdYmlResult("volume ls",
  //        """volumeName:
  //          |   - node: nodeName
  //          |     driver: local
  //          |     labels:
  //          |       a: b
  //          |       c: b
  //        """)
  //      assertCmdYmlResult("volume ls --stack volume1:1.0 --node nodeName",
  //        """volumeName:
  //          |   - node: nodeName
  //          |     stack: org.s4s0l.shathel:volume1:1.0
  //          |     driver: local
  //          |     labels:
  //          |       a: b
  //          |       c: b
  //        """)
  //      assertCmdSuccess("stack purge")
  //      assertCmdYmlResult("volume ls --stack volume1:1.0 --node nodeName",
  //        """volumeName:
  //          |   - node: nodeName
  //          |     stack: org.s4s0l.shathel:volume1:1.0
  //          |     driver: local
  //          |     labels:
  //          |       a: b
  //          |       c: b
  //        """)
  //      assertCmdYmlResult("volume rm --stack volume1:1.0 --node nodeName", "status: ok")
  //      assertCmdYmlResult("volume ls --stack volume1:1.0 --node nodeName", "")
  //      assertCmdSuccess("stack start org.s4s0l.shathel:volume1:1.0")
  //      assertCmdYmlResult("volume purge", "status: ok")
  //    }
  //  }


  feature("Stacks") {
    scenario("stacks introspection") {
      assertCmdSuccess("environment init")
      assertCmdSuccess("stack purge")
      assertCmdYmlResult("stack start org.s4s0l.shathel:inspect1:1.0 --inspect 1",
        """- compose: <hidden>
          |  gav: org.s4s0l.shathel:inspect2:1.0
          |  pre-provisioners: []
          |  post-provisioners: []
          |  type: START
          |  enriching-provisioners: []
          |- compose: <hidden>
          |  gav: org.s4s0l.shathel:inspect1:1.0
          |  pre-provisioners: []
          |  post-provisioners: []
          |  type: START
          |  enriching-provisioners: []""")
      assertCmdYmlResult("stack start org.s4s0l.shathel:inspect1:1.0 --inspect 1 --inspect-compose 1",
        """- compose:
          |    version: '3.1'
          |    services:
          |      service:
          |        image: tutum/hello-world
          |        labels:
          |          org.shathel.stack.gav: org.s4s0l.shathel:inspect2:1.0
          |          org.shathel.stack.deployName: inspect2
          |          org.shathel.stack.ga: org.s4s0l.shathel:inspect2
          |          org.shathel.stack.marker: 'true'
          |          org.shathel.deployer.version: Unknown Version
          |          org.shathel.stack.dependency.optional.0: org.s4s0l.shathel:inspect3
          |        deploy:
          |          labels:
          |            org.shathel.stack.gav: org.s4s0l.shathel:inspect2:1.0
          |            org.shathel.stack.deployName: inspect2
          |            org.shathel.stack.ga: org.s4s0l.shathel:inspect2
          |            org.shathel.stack.marker: 'true'
          |            org.shathel.deployer.version: Unknown Version
          |            org.shathel.stack.dependency.optional.0: org.s4s0l.shathel:inspect3
          |  gav: org.s4s0l.shathel:inspect2:1.0
          |  pre-provisioners: []
          |  post-provisioners: []
          |  type: START
          |  enriching-provisioners: []
          |- compose:
          |    version: '3.1'
          |    services:
          |      service:
          |        image: tutum/hello-world
          |        labels:
          |          org.shathel.stack.gav: org.s4s0l.shathel:inspect1:1.0
          |          org.shathel.stack.deployName: inspect1
          |          org.shathel.stack.ga: org.s4s0l.shathel:inspect1
          |          org.shathel.stack.marker: 'true'
          |          org.shathel.deployer.version: Unknown Version
          |          org.shathel.stack.dependency.1: org.s4s0l.shathel:inspect2
          |          org.shathel.stack.dependency.optional.0: org.s4s0l.shathel:inspect3
          |        deploy:
          |          labels:
          |            org.shathel.stack.gav: org.s4s0l.shathel:inspect1:1.0
          |            org.shathel.stack.deployName: inspect1
          |            org.shathel.stack.ga: org.s4s0l.shathel:inspect1
          |            org.shathel.stack.marker: 'true'
          |            org.shathel.deployer.version: Unknown Version
          |            org.shathel.stack.dependency.1: org.s4s0l.shathel:inspect2
          |            org.shathel.stack.dependency.optional.0: org.s4s0l.shathel:inspect3
          |  gav: org.s4s0l.shathel:inspect1:1.0
          |  pre-provisioners: []
          |  post-provisioners: []
          |  type: START
          |  enriching-provisioners: []""")
      assertCmdYmlResult("stack start org.s4s0l.shathel:inspect1:1.0 --inspect 1 --with-optional 1",
        """- compose: <hidden>
          |  gav: org.s4s0l.shathel:inspect3:1.0
          |  pre-provisioners: []
          |  post-provisioners: []
          |  type: START
          |  enriching-provisioners: []
          |- compose: <hidden>
          |  gav: org.s4s0l.shathel:inspect2:1.0
          |  pre-provisioners: []
          |  post-provisioners: []
          |  type: START
          |  enriching-provisioners: []
          |- compose: <hidden>
          |  gav: org.s4s0l.shathel:inspect1:1.0
          |  pre-provisioners: []
          |  post-provisioners: []
          |  type: START
          |  enriching-provisioners: []""")
      assertCmdSuccess("stack start org.s4s0l.shathel:inspect1:1.0 --inspect 0 --with-optional 0")
      assertCmdYmlResult("stack ls",
        """org.s4s0l.shathel:inspect2:1.0:
          |  service: 1/1
          |org.s4s0l.shathel:inspect1:1.0:
          |  service: 1/1""")
      assertCmdYmlResult("stack stop org.s4s0l.shathel:inspect1:1.0 --inspect 1 --with-optional 1",
        """- compose: <hidden>
          |  gav: org.s4s0l.shathel:inspect1:1.0
          |  pre-provisioners: []
          |  post-provisioners: []
          |  type: STOP
          |  enriching-provisioners: []""")
      assertCmdYmlResult("stack stop org.s4s0l.shathel:inspect1:1.0 --inspect 1 --with-dependencies 1 --with-optional 0",
        """- compose: <hidden>
          |  gav: org.s4s0l.shathel:inspect1:1.0
          |  pre-provisioners: []
          |  post-provisioners: []
          |  type: STOP
          |  enriching-provisioners: []
          |- compose: <hidden>
          |  gav: org.s4s0l.shathel:inspect2:1.0
          |  pre-provisioners: []
          |  post-provisioners: []
          |  type: STOP
          |  enriching-provisioners: []""")
      assertCmdYmlResult("stack stop org.s4s0l.shathel:inspect1:1.0 --inspect 1 --with-dependencies 1 --with-optional 1",
        """- compose: <hidden>
          |  gav: org.s4s0l.shathel:inspect1:1.0
          |  pre-provisioners: []
          |  post-provisioners: []
          |  type: STOP
          |  enriching-provisioners: []
          |- compose: <hidden>
          |  gav: org.s4s0l.shathel:inspect2:1.0
          |  pre-provisioners: []
          |  post-provisioners: []
          |  type: STOP
          |  enriching-provisioners: []
          |- compose: <hidden>
          |  gav: org.s4s0l.shathel:inspect3:1.0
          |  pre-provisioners: []
          |  post-provisioners: []
          |  type: STOP
          |  enriching-provisioners: []""")
      assertCmdYmlResult("stack purge --inspect 1",
        """- compose: <hidden>
          |  gav: org.s4s0l.shathel:inspect1:1.0
          |  pre-provisioners: []
          |  post-provisioners: []
          |  type: STOP
          |  enriching-provisioners: []
          |- compose: <hidden>
          |  gav: org.s4s0l.shathel:inspect2:1.0
          |  pre-provisioners: []
          |  post-provisioners: []
          |  type: STOP
          |  enriching-provisioners: []
          |- compose: <hidden>
          |  gav: org.s4s0l.shathel:inspect3:1.0
          |  pre-provisioners: []
          |  post-provisioners: []
          |  type: STOP
          |  enriching-provisioners: []""")
      assertCmdYmlResult("stack ls",
        """org.s4s0l.shathel:inspect2:1.0:
          |  service: 1/1
          |org.s4s0l.shathel:inspect1:1.0:
          |  service: 1/1""")
      assertCmdSuccess("stack purge")
    }


    scenario("stacks manipulation") {
      assertCmdSuccess("environment init")
      assertCmdSuccess("stack purge")
      assertCmdSuccess("stack start org.s4s0l.shathel:dependency:1.0")
      assertCmdYmlResult("stack ls",
        """org.s4s0l.shathel:dependency:1.0:
          |  service: 1/1
          |org.s4s0l.shathel:dependency3:1.0:
          |  service: 1/1
          |org.s4s0l.shathel:dependency2:1.0:
          |  service: 1/1""")
      assertCmdSuccess("stack start org.s4s0l.shathel:dependency:1.0 --with-optional 1")
      assertCmdYmlResult("stack ls",
        """org.s4s0l.shathel:dependency:1.0:
          |  service: 1/1
          |org.s4s0l.shathel:dependency3:1.0:
          |  service: 1/1
          |org.s4s0l.shathel:dependency4:1.0:
          |  service: 1/1
          |org.s4s0l.shathel:dependency2:1.0:
          |  service: 1/1
          |org.s4s0l.shathel:dependency1:1.0:
          |  service: 1/1""")
      assertCmdSuccess("stack stop org.s4s0l.shathel:dependency:1.0")
      assertCmdYmlResult("stack ls",
        """org.s4s0l.shathel:dependency3:1.0:
          |  service: 1/1
          |org.s4s0l.shathel:dependency4:1.0:
          |  service: 1/1
          |org.s4s0l.shathel:dependency2:1.0:
          |  service: 1/1
          |org.s4s0l.shathel:dependency1:1.0:
          |  service: 1/1""")
      assertCmdSuccess("stack start org.s4s0l.shathel:dependency:1.0")
      assertCmdSuccess("stack stop org.s4s0l.shathel:dependency:1.0 --with-dependencies 1")
      assertCmdYmlResult("stack ls",
        """org.s4s0l.shathel:dependency4:1.0:
          |  service: 1/1
          |org.s4s0l.shathel:dependency1:1.0:
          |  service: 1/1""")
      assertCmdSuccess("stack start org.s4s0l.shathel:dependency:1.0")
      assertCmdSuccess("stack stop org.s4s0l.shathel:dependency:1.0 --with-dependencies 1 --with-optional 1")
      assertCmdResult("stack ls", "{}")
    }
  }


  feature("Environments") {

    scenario("environment operations") {
      assertCmdSuccess("environment destroy")
      assertCmdYmlResult("environment use local",
        """initialized: false
          |started: true
          |verified: false""")
      assertCmdResult("environment init", "status: ok")
      assertCmdYmlResult("environment inspect",
        """initialized: true
          |started: true
          |verified: true"""
      )
      assertCmdResult("environment stop", "status: ok")
      assertCmdResult("environment save", "status: ok")
      assertFileExists("./local/safe/machines")
      assertFileExists("./local/safe/.iv")
      assertCmdResult("environment load", "status: ok")
      assertCmdResult("environment start", "status: ok")
      assertCmdResult("environment destroy", "status: ok")
      assertFileNotExists("./local/settings/ansible-inventory")
      assertCmdYmlResult("environment inspect",
        """initialized: false
          |started: true
          |verified: false"""
      )
      assertCmdResult("environment init", "status: ok")
      assertCmdYmlResult("environment inspect",
        """initialized: true
          |started: true
          |verified: true"""
      )
      assertCmdResult("storage verify", "OK")
    }


    scenario("docker interaction") {
      assertCmdSuccess("environment use local")
      assert(assertCmdSuccess("docker info").contains("Total Memory:"))
    }


    scenario("Shathel tools") {
      assertCmdResult("version", Utils.getShathelVersion)
      assertCmdResult("~ echo SomeText", "SomeText")
      val originalDir = assertCmdSuccess("~ pwd")
      assertCmdSuccess("cd build")
      val buildDir = assertCmdSuccess("~ pwd")
      assert(originalDir != buildDir && buildDir.startsWith(originalDir))
      assertCmdSuccess("cd ..")
      assert(originalDir == assertCmdSuccess("~ pwd"))

    }


    scenario("Parameter operations") {
      assertCmdResult("parameters list",
        s"""shathel.dir=${rootDir.getAbsolutePath}
           |shathel.env.local.safePassword=samplePassword
           |shathel.env=local
        """)
      assertCmdSuccess("parameters add --params shathel.env.local.forceful=true,shathel.solution.name=Test")
      assertCmdResult("parameters list",
        s"""shathel.dir=${rootDir.getAbsolutePath}
           |shathel.env.local.forceful=true
           |shathel.env.local.safePassword=samplePassword
           |shathel.env=local
           |shathel.solution.name=Test
        """)
      assert(assertCmdSuccess("parameters list --not-set 1").split("\n").length > 10)
    }

    scenario("Encrypting Decrypting") {
      var encoded: String = null
      assertCmd(s"encrypt --file ${new File("src/test/resources/filetoencrypt.txt").getAbsolutePath}") { out =>
        encoded = out
      }
      assertCmdResult(s"decrypt --value ${encoded}", "HiddenData")
    }


  }

}
