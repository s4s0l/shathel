package org.s4s0l.shathel.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import org.gradle.process.JavaForkOptions

/**
 * @author Matcin Wielgus
 */
class ShathelPackagerTask extends DefaultTask {


    ShathelPackagerTask() {
        group = 'shathel'
        description = 'Creates package for shathel deployer'
    }

    @TaskAction
    void go() {

    }

}