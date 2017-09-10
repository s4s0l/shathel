package org.s4s0l.shathel.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Dependency
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.tasks.bundling.Zip

/**
 * @author Marcin Wielgus
 */
class ShathelPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        initRequirements(project)

        preprocessShathelPrepareTasks(project)
        project.afterEvaluate {
            createDockerTasks(project)
            finalizeDockerTasks(project)
            ShathelPrepareTask exportedTask = finalizePrepareTasks(project)
            if (exportedTask != null) {
                createDockerAssembleTasks(project, exportedTask)
                createShathelDestroyTask(project, exportedTask)
            }
        }

    }

    private void initRequirements(Project project) {
        project.with {
            apply plugin: 'maven'
            configurations.create('shathel')
        }
        project.extensions.create('shathel', ShathelExtension, project)
    }

    private void createDockerAssembleTasks(Project project, ShathelPrepareTask exportedTask) {
        def shathelAssemble = project.task("shathelAssemble", type: Zip, dependsOn: exportedTask) {
            from exportedTask.outputs
            classifier = 'shathel'
            destinationDir new File(project.buildDir, 'libs')
            eachFile { FileCopyDetails details ->
            }
        }
        project.artifacts {
            shathel shathelAssemble
        }
        project.tasks.matching { it.name == "bootRepackage" }.each {
            Set<Object> deps = it.taskDependencies.values
            def toBeeRemoved = deps.find {
                it.class.name.startsWith("org.gradle.api.internal.artifacts.DefaultPublishArtifactSet")
            }
            deps.removeAll(toBeeRemoved)
        }
    }

    private void createShathelDestroyTask(Project project, ShathelPrepareTask exportedTask) {
        ShathelDestroyTask destroy = project.task("shathelDestroy", type: ShathelDestroyTask)
        destroy.dependsOn exportedTask
    }

    private void createDockerTasks(Project project) {
        ShathelExtension extension = project.extensions.findByName("shathel")
        extension.images.each {
            ShathelDockerTask task = project.task("shathelDockerBuild-${it.key}", type: ShathelDockerTask)
            task.setSettings(it.value)
        }
        if (extension.images.isEmpty() && project.file("src/main/docker").exists()) {
            project.task("shathelDockerBuild-${ShathelDockerTask.getDefaultDockerImageName(project)}", type: ShathelDockerTask)
        }
    }

    private void preprocessShathelPrepareTasks(Project project) {
        ShathelExtension extension = project.extensions.findByName("shathel")
        if (new File(project.file(extension.sourceRoot), "shthl-stack.yml").exists()) {
            ShathelPrepareTaskSettings shathelPrepareTask = new ShathelPrepareTaskSettings(project)
            List<String> otherProjectDeps = shathelPrepareTask.getOtherProjectDependencies()
            otherProjectDeps
                    .findAll { pp -> pp != project.path }
                    .unique()
                    .each { xx ->
//                println("${project.name} Evaluation depends on $xx")
                project.evaluationDependsOn(xx)
            }
        }
    }

    private ShathelPrepareTask finalizePrepareTasks(Project project) {
        ShathelExtension extension = project.extensions.findByName("shathel")
        def prepareTasks = project.tasks.withType(ShathelPrepareTask).collect {
            it
        }
        if (prepareTasks.find { it.settings.exported } == null) {
            if (new File(project.file(extension.sourceRoot), "shthl-stack.yml").exists()) {
//                println("Prepare a/dded in project ${project.name}")
                ShathelPrepareTask shathelPrepareTask = project.task("shathelPrepare", type: ShathelPrepareTask) {
                    settings {
                        exported = true
                    }
                }
                extension.runAroundTasks.each {
                    shathelPrepareTask.runAround(it)

                }
                extension.prepareForTasks.each {
                    shathelPrepareTask.prepareFor(it)
                }
                prepareTasks << shathelPrepareTask
            }
        }

        prepareTasks.each {
            it.finalizeConfiguration()
        }

        def exportedTasks = prepareTasks.findAll { it.settings.exported }
        if (exportedTasks.size() > 1) {
            throw new RuntimeException("Multiple exported ShathelPrepareTasks! Not allowed must be most one.")
        }
        if (exportedTasks.size() == 0) {
            return null
        }
        def exportedTask = exportedTasks.head()


        project.task("shathelStart", dependsOn: exportedTask, type: ShathelStartTask)
        project.task("shathelStop", dependsOn: exportedTask, type: ShathelStopTask)


        exportedTask
    }

    private void finalizeDockerTasks(Project project) {
        //otherwise CME
        def dockerTasks = project.tasks.withType(ShathelDockerTask).collect {
            it
        }
        dockerTasks.each {
            it.finalizeConfiguration()
        }
    }
}
