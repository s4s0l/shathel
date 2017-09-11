package org.s4s0l.shathel.gradle

import org.gradle.api.Project
import org.gradle.api.Task
import org.s4s0l.shathel.commons.core.CommonParams
import org.s4s0l.shathel.commons.core.Parameters

/**
 * @author Marcin Wielgus
 */
class ShathelExtension {
    final Project project
    Map<String, ShathelDockerTaskSettings> images = [:]
    String sourceRoot = "src/main/shathel"
    List<? extends Task> runAroundTasks = []
    List<? extends Task> prepareForTasks = []
    Map<String,String> shathelParams = [:]
    Map<String,String> tokens = [:]

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


    protected Parameters getShathelParameters() {
        ShathelExtension extension = this
        def paramsWithDefault = extension.shathelParams.collectEntries {
            [(Parameters.getNormalizedParameterName(it.key.toString())): it.value.toString()]
        }
        paramsWithDefault.putIfAbsent(CommonParams.SHATHEL_ENV, "local")
        String envName = paramsWithDefault[CommonParams.SHATHEL_ENV]
        paramsWithDefault.putIfAbsent("shathel.env.${envName}.dependenciesDir".toString(), getDependenciesDir().absolutePath)
        paramsWithDefault.putIfAbsent(CommonParams.SHATHEL_DIR, new File(project.rootProject.buildDir, ".shathel").absolutePath)
        paramsWithDefault[CommonParams.SHATHEL_DIR] = absolutize(paramsWithDefault.get(CommonParams.SHATHEL_DIR))
        if (!new File("${paramsWithDefault[CommonParams.SHATHEL_DIR]}/shathel-solution.yml").exists()) {
            paramsWithDefault['shathel.solution.name'] = "shathel-gradle-${project.getName()}".toString()
        }
        Parameters.fromMapWithSysPropAndEnv(paramsWithDefault)
    }

    File getDependenciesDir() {
        new File(project.rootProject.buildDir, "shathel-dependencies")
    }


    private String absolutize(String it) {
        if (new File(it).isAbsolute()) {
            return new File(it).absolutePath
        } else {
            return new File(project.projectDir, it).absolutePath
        }
    }

    File getShathelDir() {
        return new File(getShathelParameters().getParameter(CommonParams.SHATHEL_DIR).get())
    }

    File getShathelTargetDir() {
        return new File(project.rootProject.buildDir, "shathel-stacks")
    }

    String getShathelEnvironmentName() {
        return getShathelParameters().getParameter(CommonParams.SHATHEL_ENV).get()
    }

    File getShathelMappingsDir() {
        getShathelTargetDir()
    }

}
