package org.s4s0l.shathel.gradle


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip

/**
 * @author Matcin Wielgus
 */
class ShathelPackagerPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create('shathel', ShathelPackagerExtension)
        project.with {
            apply plugin: 'maven'

            task([type: Zip], 'xxx') {
                classifier = 'sht'
                from 'src/main/shathel'
                destinationDir file('build/libs')
            }
            assemble.dependsOn xxx
            artifacts {
                archives xxx
            }
        }
    }
}
