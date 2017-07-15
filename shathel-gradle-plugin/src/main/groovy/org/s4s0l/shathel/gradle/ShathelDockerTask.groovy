package org.s4s0l.shathel.gradle

import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskAction
import org.s4s0l.shathel.commons.docker.DockerWrapper

/**
 * @author Marcin Wielgus
 */


class ShathelDockerTask extends DefaultTask {

    ShathelDockerTaskSettings settings

    static String getDefaultDockerImageName(Project project) {
        "${project.name}"
    }

    void image(String name, Closure cls) {
        def sets = new ShathelDockerTaskSettings(name, project)
        sets.configure { cls }
        setSettings(sets)
    }

    void image(Closure cls) {
        image(getDefaultDockerImageName(project), cls)
    }

    void finalizeConfiguration() {
        if (settings == null) {
            settings = new ShathelDockerTaskSettings(getDefaultDockerImageName(project), project)
        }
        def config = settings
        def prepareTask = project.task("shathelDockerPrepare-${config.imageName}", type: Copy) {
            config.contexts.each {
                with it
            }
            into config.targetDir
            doLast {
                def file = new File(config.targetDir, config.dockerFile)
                def contents = file.text
                config.tokens.each {
                    contents = contents.replace("@${it.key}@", it.value)
                }
                file.text = contents
            }
        }
        settings.dependsOnTasks.each {
            prepareTask.dependsOn it
        }
        this.dependsOn prepareTask

        this.inputs.dir(config.targetDir)
        this.outputs.dir(config.targetDir)
        this.outputs.file(project.buildDir.path + "/shathel-dockers/${config.imageName}.file")
    }

    boolean isPushingSomewhere() {
        settings.push && !settings.repos.isEmpty()
    }

    @TaskAction
    void build() {
        def config = settings
        def contextPath = project.file(config.targetDir)
        if (config.build) {
            getWrapper().buildAndTag(contextPath, config.dockerFile, config.args, config.combinedTags)
            if (isPushingSomewhere()) {
                config.combinedTags.each {
                    getWrapper().push(it)
                }
            }
        }
        project.file(project.buildDir.path + "/shathel-dockers/${config.imageName}.file").text = UUID.randomUUID().toString()
    }

    DockerWrapper getWrapper() {
        new DockerWrapper()
    }

}


class ShathelDockerTaskSettings {
    final Object project
    final String imageName
    List<String> tags
    String version
    List<String> repos
    String dockerFile
    List<Object> contexts
    boolean push
    boolean build
    String targetDir
    Map<String, String> tokens
    Map<String, String> args
    List<? extends Task> dependsOnTasks = []


    ShathelDockerTaskSettings configure(Closure cls) {
        cls.delegate = this
        cls.call()
        this
    }

    ShathelDockerTaskSettings(String imageName, Project project) {
        this.project = project
        this.imageName = imageName
        this.tags = [project.version]
        this.version = project.version
        this.repos = []
        this.dockerFile = 'Dockerfile'
        this.contexts = [{
                             from 'src/main/docker'
                         }]
        this.push = false
        this.build = true
        this.targetDir = project.buildDir.path + "/shathel-dockers/$imageName"
        this.tokens = [VERSION: project.version]
        this.args = [:]
    }

    def propertyMissing(String name) {
        if (hasProperty("${name}s")) {
            return getProperty("${name}s").isEmpty() ? null : getProperty("${name}s").last()
        }
        throw new RuntimeException("No such property: ${name}")
    }

    def propertyMissing(String name, def arg) {
        if (hasProperty("${name}s")) {
            return methodMissing(name, [arg])
        }
        throw new RuntimeException("No such property: ${name}")
    }

    def methodMissing(String name, def args) {
        if (hasProperty("${name}s")) {
            setProperty("${name}s", getProperty("${name}s") << args[0])
            return
        }
        throw new RuntimeException("No such method: ${name}")
    }

    List<Object> getContexts() {
        return contexts.collect {
            if (it instanceof String) {
                return {
                    from it
                }
            } else {
                return it
            }
        }
    }

    List<String> getCombinedTags() {
        def allRepos = repos.isEmpty() ? [""] : repos.collect { "$it/" }
        return allRepos.collect { repo ->
            tags.collect { tag ->
                "${repo}${imageName.toLowerCase()}:${tag}"
            }
        }.flatten()
    }

}


