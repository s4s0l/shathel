package org.s4s0l.shathel.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.process.JavaForkOptions
import org.s4s0l.shathel.commons.DefaultExtensionContext
import org.s4s0l.shathel.commons.Shathel
import org.s4s0l.shathel.commons.core.CommonParams
import org.s4s0l.shathel.commons.core.Parameters
import org.s4s0l.shathel.commons.core.Solution
import org.s4s0l.shathel.commons.core.Stack
import org.s4s0l.shathel.commons.core.dependencies.LocalOverriderDownloader
import org.s4s0l.shathel.commons.core.dependencies.StackLocator
import org.s4s0l.shathel.commons.core.stack.StackReference

/**
 * @author Marcin Wielgus
 */

class ShathelOperationTask extends DefaultTask {
    Map<String, String> shathelParams = [:]

    String getShathelEnvironmentName() {
        return getCombinedParams().getOrDefault(CommonParams.SHATHEL_ENV, "local")
    }

    File getShathelDir() {
        return getShathelParameters().getParameter(CommonParams.SHATHEL_DIR)
                .map {
            if (new File(it).isAbsolute()) {
                return new File(it)
            } else {
                return new File(project.projectDir, it)
            }
        }.orElse(new File(project.buildDir, ".shathel"))
    }


    boolean isShathelInitEnabled() {
        return getShathelParameters().getParameterAsBoolean("shathel.env.${shathelEnvironmentName}.init")
                .orElse(true)
    }

    File getShathelMappingsDir() {
        new File(project.rootProject.buildDir, "shathel-mappings")
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
        Map paramsWithDefault = getCombinedParams()
        fillDefault(paramsWithDefault, "shathel.env.${getShathelEnvironmentName()}.dependenciesDir", getDependenciesDir().absolutePath)
        return paramsWithDefault
    }

    File getDependenciesDir() {
        new File(project.rootProject.buildDir, "shathel-dependencies")
    }

    private Map getCombinedParams() {
        ShathelExtension extension = getExtension()
        def paramsWithDefault = [:] << extension.shathelParams << shathelParams
        paramsWithDefault
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
        return solution.openStack(environment, new StackLocator(LocalOverriderDownloader.CURRENT_PROJECT_LOCATION))
    }

    private Parameters getShathelParameters() {
        Parameters.fromMapWithSysPropAndEnv(getParamsWithDefaults())
    }

}

class ShathelNotifyingTask extends ShathelOperationTask {
    List<JavaForkOptions> tasksToNotify = []

    protected void notifyTasks(Stack stack) {
         def propsToleaveForOthers = [
                (CommonParams.SHATHEL_ENV)                                  : shathelEnvironmentName,
                "shathel.plugin.local.override.mappings"                    : getShathelMappingsDir().getAbsolutePath(),
                "shathel.plugin.local.override.current"                     : getShathelCurrentStackDir().getAbsolutePath(),
                "shathel.plugin.current.gav"                                : new StackReference(project.group, project.name, project.version).gav,
                "shathel.plugin.current"                                    : LocalOverriderDownloader.CURRENT_PROJECT_LOCATION,
                "shathel.env.${getShathelEnvironmentName()}.dependenciesDir": getDependenciesDir().absolutePath,
                "shathel.env.${getShathelEnvironmentName()}.init"           : "true",
                (CommonParams.SHATHEL_DIR)                                  : getShathelDir()
        ]

        stack.environment.introspectionProvider.allStacks.stacks.each { stk ->
            stk.services.each { service ->
                def keyPrefix = "shathel.plugin.${stk.reference.name}.${service.serviceName}"
                service.getPortMapping().each {
                    propsToleaveForOthers << ["${keyPrefix}.${it.key}": "${it.value}"]
                }

            }
        }
        tasksToNotify.findAll { it instanceof JavaForkOptions }.each { task ->
            propsToleaveForOthers.each {
                task.systemProperties.put(it.key.toString(), it.value.toString())
            }
        }
    }

    @TaskAction
    void build() {
        def stack = getShathelCurrentStack()
        notifyTasks(stack)

    }

}

class ShathelStartTask extends ShathelNotifyingTask {

    boolean withOptionalDependencies = false


    @TaskAction
    @Override
    void build() {
        def stack = getShathelCurrentStack()
        def command = stack.createStartCommand(withOptionalDependencies)
        stack.run(command)
        notifyTasks(stack)

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