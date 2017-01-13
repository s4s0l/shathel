package org.s4s0l.shathel.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author Matcin Wielgus
 */
class ShathelPackagerPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create('shathel', ShathelPackagerExtension, project)
    }
}
