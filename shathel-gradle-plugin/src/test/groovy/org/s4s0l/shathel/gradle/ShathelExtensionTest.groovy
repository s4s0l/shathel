package org.s4s0l.shathel.gradle

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * @author Marcin Wielgus
 */
class ShathelExtensionTest extends Specification {
    def "Dls should work"() {
        given:
        def p = ProjectBuilder.builder().withName("pName").build().with {
            version = "1.1.1"
            group = "test.group"
            project
        }
        def x = {
            image {
                context {
                    from(jar) {
                        into 'app'
                    }
                }
                push = true
            }

            image("dummyExtra") {
                contexts = [
                        { from(jar) { into 'app' } },
                        { from('src/main/docker2') }
                ]
                token = [PORT: '9090']
                push = false
            }
        }

        when:
        def ext = new ShathelExtension(p).configure(x)

        then:
        ext.images.size() == 2
        ext.images.containsKey('pName')
        ext.images.containsKey('dummyExtra')

    }
}
