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

        project.afterEvaluate {
            createDockerTasks(project)
            finalizeDockerTasks(project)
            ShathelPrepareTask exportedTask = finalizePrepareTasks(project)
            createDockerAssembleTasks(project, exportedTask)
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
        project.tasks.matching {it.name == "bootRepackage"}.each {
            Set<Object> deps =  it.taskDependencies.values
            def toBeeRemoved = deps.find {
                it.class.name.startsWith("org.gradle.api.internal.artifacts.DefaultPublishArtifactSet")
            }
            deps.removeAll(toBeeRemoved)
        }
    }

    private Map<String, ShathelDockerTaskSettings> createDockerTasks(Project project) {
        ShathelExtension extension = project.extensions.findByName("shathel")
        extension.images.each {
            ShathelDockerTask task = project.task("shathelDockerBuild-${it.key}", type: ShathelDockerTask)
            task.setSettings(it.value)
        }
    }

    private ShathelPrepareTask finalizePrepareTasks(Project project) {
        ShathelExtension extension = project.extensions.findByName("shathel")
        def prepareTasks = project.tasks.withType(ShathelPrepareTask).collect {
            it
        }
        if (prepareTasks.isEmpty()) {
            ShathelPrepareTask shathelPrepareTask = project.task("shathelPrepare", type: ShathelPrepareTask) {
                settings {
                    exported = true
                }
            }
            extension.runAroundTasks.each {
                shathelPrepareTask.runAround(it)
            }
            prepareTasks << shathelPrepareTask
        }
        prepareTasks.each {
            it.finalizeConfiguration()
        }

        def exportedTasks = prepareTasks.findAll { it.settings.exported }
        if (exportedTasks.size() != 1) {
            throw new RuntimeException("Multiple exported ShathelPrepareTasks! Not allowed must be exactly one.")
        }
        def exportedTask = exportedTasks.head()
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
