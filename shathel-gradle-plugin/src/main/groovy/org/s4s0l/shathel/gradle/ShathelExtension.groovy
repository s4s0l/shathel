package org.s4s0l.shathel.gradle

import org.gradle.api.Project
import org.gradle.api.Task

/**
 * @author Marcin Wielgus
 */
class ShathelExtension {
    final Project project
    Map<String, ShathelDockerTaskSettings> images = [:]
    String sourceRoot = "src/main/shathel"
    List<? extends Task> runAroundTasks = []
    Map<String,String> shathelParams = [:]

    ShathelExtension(Project project) {
        this.project = project
    }

    def image(String name, Closure c) {
        images << [(name): new ShathelDockerTaskSettings(name, project).configure(c)]
    }

    def image(Closure c) {
        image(ShathelDockerTask.getDefaultDockerImageName(project), c)
    }

    ShathelExtension configure(Closure cls) {
        cls.delegate = this
        cls.call()
        this
    }

}
