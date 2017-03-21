package org.s4s0l.shathel.gradle

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * @author Marcin Wielgus
 */
class ShathelDockerTaskSettingsTest extends Specification {

    def "Dls should work"() {
        given:
        def p = ProjectBuilder.builder().withName("pName").build().with {
            version = "1.1.1"
            group = "test.group"
            project
        }
        def x = {
            tags = ['3.3.3', project.version]
            version = project.version
            repos = ["hub.docker.com/s4s0l"]
            dockerFile = 'Dockerfile'
            contexts = [{
                            from(jar) {
                                into 'app'
                            }
                        }]
            push = true
            build = true
            targetDir = "build/$imageName"
            tokens = [XXX: "${project.version}"]

            token(a: 1)
            token = [b: 2]
            context "xxx"
            repo "dummyRepo"
            repo = "dummyRepo2"

            tag "X"
            tag = "latest"
        }

        when:
        def settings = new ShathelDockerTaskSettings("testImage", p).configure(x)

        then:
        settings.imageName == 'testImage'
        settings.tags == ['3.3.3', '1.1.1', 'X', 'latest']
        settings.version == '1.1.1'
        settings.repos == ["hub.docker.com/s4s0l", 'dummyRepo', 'dummyRepo2']
        settings.dockerFile == 'Dockerfile'
        settings.contexts.size() == 2
        settings.contexts.findAll { !it instanceof Closure }.size() == 0
        settings.push
        settings.build
        settings.targetDir == 'build/testImage'
        settings.tokens == [XXX: "1.1.1", a: 1, b: 2]
        settings.combinedTags == [
                "hub.docker.com/s4s0l/testimage:3.3.3",
                "hub.docker.com/s4s0l/testimage:1.1.1",
                "hub.docker.com/s4s0l/testimage:X",
                "hub.docker.com/s4s0l/testimage:latest",
                'dummyRepo/testimage:3.3.3',
                'dummyRepo/testimage:1.1.1',
                'dummyRepo/testimage:X',
                'dummyRepo/testimage:latest',
                'dummyRepo2/testimage:3.3.3',
                'dummyRepo2/testimage:1.1.1',
                'dummyRepo2/testimage:X',
                'dummyRepo2/testimage:latest',
        ]
    }
}
