package org.s4s0l.shathel.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.process.JavaForkOptions
import org.s4s0l.shathel.commons.DefaultExtensionContext
import org.s4s0l.shathel.commons.Shathel
import org.s4s0l.shathel.commons.core.Parameters
import org.s4s0l.shathel.commons.core.Solution
import org.s4s0l.shathel.commons.core.Stack
import org.s4s0l.shathel.commons.core.dependencies.FileDependencyDownloader
import org.s4s0l.shathel.commons.core.dependencies.StackLocator
import org.s4s0l.shathel.commons.git.GitDependencyDownloader
import org.s4s0l.shathel.commons.ivy.IvyDownloader


/**
 * @author Marcin Wielgus
 */

class ShathelOperationTask extends DefaultTask {
    Map<String, String> shathelParams = [:]

    String getShathelEnvironmentName() {
        return shathelParams.getOrDefault("shathel.env", "local")
    }

    File getShathelDir() {
        return getShathelParameters().getParameter("shathel.deployer.dir")
                .map {
            if (new File(it).isAbsolute()) {
                return new File(it)
            } else {
                return new File(project.projectDir, it)
            }
        }.orElse(new File(project.buildDir, "shathel"))
    }


    boolean isShathelInitEnabled() {
        return getShathelParameters().getParameterAsBoolean("shathel.deployer.init")
                .orElse(true)
    }

    File getShathelMappingsDir() {
        new File(project.buildDir, "shathel-mappings")
    }

    File getShathelCurrentStackDir() {
        def dependencies = taskDependencies.getDependencies(this)
        def prepareTasks = dependencies.findAll { it instanceof ShathelPrepareTask }
        assert prepareTasks.size() == 1: "Operation tasks can be dependant only on one prepare task!"
        ShathelPrepareTask prepareTask = prepareTasks.head()
        return prepareTask.settings.to
    }

    private void fillDefault(Map map, String key, String value) {
        if (map[key] == null) {
            map[key] = value
        }
    }

    Map<String, String> getParamsWithDefaults() {
        ShathelExtension extension = getExtension()
        def paramsWithDefault = [:] << extension.shathelParams << shathelParams
        fillDefault(paramsWithDefault, "shathel.env.${getShathelEnvironmentName()}.dependenciesDir", new File(project.rootProject.buildDir, "shathel-dependencies").absolutePath)
        return paramsWithDefault
    }

    private ShathelExtension getExtension() {
        ShathelExtension extension = project.extensions.findByName("shathel") ?: new ShathelExtension(project)
        extension
    }

    Solution getShathelSolution() {
        def params = getShathelParameters()
        def extensions = DefaultExtensionContext.create(params, [
                new LocalOverriderDownloader(getShathelMappingsDir(), getShathelCurrentStackDir())
        ])
        def shathel = new Shathel(params, extensions)
        def storage = shathel.initStorage(shathelDir, false)
        return shathel.getSolution(storage)
    }


    Stack getShathelCurrentStack() {
        def solution = getShathelSolution()
        def environment = solution.getEnvironment(getShathelEnvironmentName())
        if (isShathelInitEnabled() && !environment.isInitialized()) {
            environment.initialize()
        }
        return solution.openStack(environment, new StackLocator("--currentProject--"))
    }

    private Parameters getShathelParameters() {
        Parameters.fromMapWithSysPropAndEnv(getParamsWithDefaults())
    }

}


class ShathelStartTask extends ShathelOperationTask {

    boolean withOptionalDependencies = false
    List<JavaForkOptions> tasksToNotify = []

    @TaskAction
    void build() {
        def stack = getShathelCurrentStack()
        def command = stack.createStartCommand(withOptionalDependencies)
        stack.run(command)
        def ip = stack.environment.getEnvironmentApiFacade().getIpForManagementNode()

        tasksToNotify.each {
            it.systemProperties.put("shathel.plugin.ip", ip)
        }

    }
}

class ShathelStopTask extends ShathelOperationTask {

    boolean withOptionalDependencies = false
    boolean withDependencies = true

    @TaskAction
    void build() {
        def stack = getShathelCurrentStack()
        def command = stack.createStopCommand(withDependencies, withOptionalDependencies)
        stack.run(command)


    }
}