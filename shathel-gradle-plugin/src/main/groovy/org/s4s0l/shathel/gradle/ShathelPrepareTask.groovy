package org.s4s0l.shathel.gradle

import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import org.s4s0l.shathel.commons.core.model.ComposeFileModel
import org.s4s0l.shathel.commons.core.model.StackFileModel
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.yaml.snakeyaml.Yaml

/**
 * @author Marcin Wielgus
 */
class ShathelPrepareTask extends DefaultTask {
    def dockerTargetRegexp = /\s*project\(((:[^:\)]*)+)\)((:[^:]+)*)\s*/
    def shathelTargetRegexp = /\s*project\(((:[^:\)]*)+)\)\s*/
    private ShathelPrepareTaskSettings settings;


    void settings(Closure cls) {
        settings = new ShathelPrepareTaskSettings(project)
        settings.configure(cls)
    }

    void finalizeConfiguration() {
        if (settings == null) {
            settings = new ShathelPrepareTaskSettings(project)
        }
        finalizeAddDependenciesFromStack()
        finalizeAddDependenciesFromCompose()
        inputs.dir(settings.from)
        outputs.dir(settings.to)


    }

    private void finalizeAddDependenciesFromCompose() {
        Task selfTask = this
        ComposeFileModel cfm = ComposeFileModel.load(new File(settings.from, "stack/docker-compose.yml"))
        cfm.mapImages { imageFromFile ->
            def dMatch = imageFromFile =~ dockerTargetRegexp
            if (dMatch.matches()) {
                String projectLocator = dMatch[0][1]
                String image = dMatch[0][3]
                def tasks = project.project(projectLocator).tasks.withType(ShathelDockerTask)
                if (image == "") {
                    if (tasks.size() == 1) {
                        def oneTask = tasks.iterator().next()
                        selfTask.dependsOn oneTask
                        inputs.files(oneTask.outputs.files)
                    } else {
                        throw new RuntimeException("$imageFromFile has no ShathelDockerTask name specified but multiple found in project $projectLocator")
                    }
                } else {
                    image = image.substring(1)
                    def oneTask = tasks.find { it.settings.imageName == image }
                    if (oneTask == null) {
                        throw new RuntimeException("Unable to find ShathelDockerTask for image name $image in project $projectLocator ")
                    }
                    selfTask.dependsOn oneTask
                    inputs.files(oneTask.outputs.files)

                }
            }
            return imageFromFile
        }
    }

    private void finalizeAddDependenciesFromStack() {
        Task selfTask = this
        StackFileModel sfm = StackFileModel.load(new File(settings.from, "shthl-stack.yml"))
        sfm.getDependencies().collect {
            it.gav =~ shathelTargetRegexp
        }.findAll {
            it.matches()
        }.collect {
            it[0][1]
        }.each {
            //todo zdaje sie ze raczej powinno sie poiterowac po taskach i znalesc wyeksportowany
            def otherTask = project.project(it).tasks.getByName('shathelPrepare');
            selfTask.dependsOn otherTask
        }
    }

    ShathelPrepareTaskSettings getSettings() {
        return settings
    }

    def prepareFor(Task task){
        def startTask = project.task("shathelNotify-${task.name}", dependsOn: this, type: ShathelNotifyingTask) {
            tasksToNotify = [task]
        }
        task.dependsOn startTask
    }

    def runAround(Task task) {
        def startTask = project.task("shathelStart-${task.name}", dependsOn: this, type: ShathelStartTask) {
            tasksToNotify = [task]
        }
        task.dependsOn startTask
        def stopTask = project.task("shathelStop-${task.name}", dependsOn: this, type: ShathelStopTask)
        task.finalizedBy stopTask
    }

    @TaskAction
    void build() {
        project.copy {
            from getSettings().from
            into getSettings().to
            from(new File(getSettings().from, "shthl-stack.yml")) {
                filter(ReplaceTokens, tokens: getSettings().tokens)
            }
        }
        buildFixImages()
        if (settings.exported) {
            saveMappingForProject(project)
        }
        buildFixDependencies()

    }

    private void buildFixDependencies() {
        def sfmFile = new File(settings.to, "shthl-stack.yml")
        def parsedYml = new Yaml().load(sfmFile.text)
        parsedYml['shathel-stack'].dependencies = parsedYml['shathel-stack'].dependencies?.collectEntries {
            def mm = it.key =~ shathelTargetRegexp
            if (mm.matches()) {
                def projectDep = mm[0][1]
                def theProject = project.project(projectDep)
                def gav = saveMappingForProject(theProject)
                [(gav): it.value]
            } else {
                return [(it.key): it.value]
            }
        }
        sfmFile.text = new Yaml().dump(parsedYml)
    }

    /**
     *
     * @param project
     * @return gav ov project
     */
    static String saveMappingForProject(Project theProject) {
        def stackMappingsDir = new File(theProject.rootProject.buildDir, "shathel-mappings")
        stackMappingsDir.mkdirs()
        def thePlugin = theProject.getTasks().withType(ShathelPrepareTask).findAll {
            it.settings.exported
        }.head()
        def reference = new StackReference(theProject.group, theProject.name, theProject.version)
        def gav = reference.getGav().replaceAll("[^a-zA-Z0-9]", "_")
        new File(stackMappingsDir, gav).text = thePlugin.settings.to.absolutePath
        return reference.getGav()
    }

    private void buildFixImages() {
        def cfmFile = new File(settings.to, "stack/docker-compose.yml")
        ComposeFileModel cfm = ComposeFileModel.load(cfmFile)
        cfm.mapImages { imageFromFile ->
            def dMatch = imageFromFile =~ dockerTargetRegexp
            if (dMatch.matches()) {
                String projectLocator = dMatch[0][1]
                String image = dMatch[0][3]
                def theProject = project.project(projectLocator)
                def tasks = theProject.tasks.withType(ShathelDockerTask)
                ShathelDockerTask theTask
                if (image == "") {
                    if (tasks.size() == 1) {
                        theTask = tasks.head()
                    } else {
                        throw new RuntimeException("$imageFromFile has no ShathelDockerTask name specified but multiple found in project $projectLocator")
                    }
                } else {
                    theTask = tasks.find {
                        it.settings.imageName == image.substring(1)
                    }
                }
                if (theTask.isPushingSomewhere()) {
                    return theTask.settings.getCombinedTags().head().toString()
                } else {
                    def dockerContextFile = theProject.file(theTask.settings.targetDir)
                    def dockerfileName = theTask.settings.dockerFile
                    def dockerBuildArgs = theTask.settings.args
                    def localDockerContextPath = "./dockerfiles/" + dockerContextFile.getName()
                    project.copy {
                        from dockerContextFile
                        into new File(settings.to, "stack/${localDockerContextPath}")
                    }
                    return [
                            context   : localDockerContextPath,
                            dockerfile: dockerfileName,
                            args      : dockerBuildArgs
                    ]
                }
            }
            return imageFromFile
        }
        cfmFile.text = new Yaml().dump(cfm.parsedYml)
    }
}


class ShathelPrepareTaskSettings {
    final Object project
    File from
    File to
    Map<String, String> tokens
    // czy ma byc uzywany w innych projektach jako import ten stack
    boolean exported = false


    ShathelPrepareTaskSettings configure(Closure cls) {
        cls.delegate = this
        cls.call()
        this
    }

    ShathelPrepareTaskSettings(Project project) {
        this.project = project
        ShathelExtension extension = project.extensions.findByName("shathel") ?: new ShathelExtension(project)
        this.from = project.file(extension.sourceRoot)
        this.to = new File(project.buildDir, "shathel-stack")
        this.tokens = [VERSION: project.version]
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
}