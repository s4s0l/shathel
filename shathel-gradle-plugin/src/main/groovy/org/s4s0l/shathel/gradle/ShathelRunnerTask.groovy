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
import org.s4s0l.shathel.commons.core.environment.StackIntrospection
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.commons.ssh.SshTunelManagerImpl

/**
 * @author Marcin Wielgus
 */

class ShathelOperationTask extends DefaultTask {


    File getShathelCurrentStackDir() {
        def dependencies = taskDependencies.getDependencies(this)
        def prepareTasks = dependencies.findAll { it instanceof ShathelPrepareTask }
        assert prepareTasks.size() == 1: "Operation tasks can be dependant only on one prepare task!"
        ShathelPrepareTask prepareTask = prepareTasks.head()
        return prepareTask.settings.to
    }


    private ShathelExtension getExtension() {
        ShathelExtension extension = project.extensions.findByName("shathel") ?: new ShathelExtension(project)
        extension
    }

    Solution getShathelSolution() {
        def params = getExtension().getShathelParameters()
        def extensions = DefaultExtensionContext.create(params, [
                new LocalOverriderDownloader(getExtension().getShathelMappingsDir(), getShathelCurrentStackDir())
        ])
        def shathel = new Shathel(params, extensions)
        def storage = shathel.initStorage(getExtension().shathelDir, false)
        return shathel.getSolution(storage)
    }


    Stack getShathelCurrentStack(Solution solution) {
        def environment = solution.getEnvironment(getExtension().getShathelEnvironmentName())
        if (!environment.isInitialized()) {
            environment.initialize()
        }
        return solution.openStack(environment, new StackLocator(LocalOverriderDownloader.CURRENT_PROJECT_LOCATION))
    }


    Map<String, String> getDefaultPropsToLeave() {
        [
                "shathel.plugin.local.override.mappings"                                   : getExtension().getShathelMappingsDir().getAbsolutePath(),
                "shathel.plugin.local.override.current"                                    : getShathelCurrentStackDir().getAbsolutePath(),
                "shathel.plugin.current.gav"                                               : new StackReference(project.group, project.name, project.version).gav,
                "shathel.plugin.current"                                                   : LocalOverriderDownloader.CURRENT_PROJECT_LOCATION,
                (CommonParams.SHATHEL_ENV)                                                 : getExtension().shathelEnvironmentName,
                "shathel.env.${getExtension().getShathelEnvironmentName()}.dependenciesDir": getExtension().getDependenciesDir().absolutePath,
                "shathel.env.${getExtension().getShathelEnvironmentName()}.init"           : "true",
                (CommonParams.SHATHEL_DIR)                                                 : getExtension().getShathelDir()
        ]
    }

}

class ShathelNotifyingTask extends ShathelOperationTask {
    List<JavaForkOptions> tasksToNotify = []


    @TaskAction
    void build() {
        def propsToleaveForOthers = [:]
        def parameters = shathelParameters
        parameters.allParameters.each {
            propsToleaveForOthers << [(it): parameters.getParameter(it).get()]
        }
        propsToleaveForOthers << defaultPropsToLeave
        tasksToNotify.findAll { it instanceof JavaForkOptions }.each { task ->
            propsToleaveForOthers.each {
                task.systemProperties.put(it.key.toString(), it.value.toString())
            }
        }

    }

}

class ShathelStartTask extends ShathelOperationTask {
    List<JavaForkOptions> tasksToNotify = []
    boolean withOptionalDependencies = false


    protected void notifyTasks(Stack stack) {
        def propsToleaveForOthers = [:]
        def allStacks = stack.environment.introspectionProvider.allStacks.stacks
        for (StackIntrospection allStack : allStacks) {
            for (StackIntrospection.Service service : allStack.getServices()) {
                String servicePrefix = service.getServiceName().replaceAll("[^0-9a-zA-Z]", "_").toUpperCase() + "_"
                String fullNamePrefix = service.getFullServiceName().replaceAll("[^0-9a-zA-Z]", "_").toUpperCase() + "_"
                for (Map.Entry<Integer, Integer> entry : service.getPortMapping().entrySet()) {
                    String tunneledPort = stack.environment.environmentApiFacade.openPublishedPort(entry.getValue())
                    def shortKey = Parameters.getNormalizedParameterName("shathel.plugin." + servicePrefix + entry.getKey())
                    def longKey = Parameters.getNormalizedParameterName("shathel.plugin." + fullNamePrefix + entry.getKey())
                    propsToleaveForOthers.put(shortKey, tunneledPort)
                    propsToleaveForOthers.put(longKey, tunneledPort)
                }
            }
        }
        propsToleaveForOthers << defaultPropsToLeave
        tasksToNotify.findAll { it instanceof JavaForkOptions }.each { task ->
            propsToleaveForOthers.each {
                task.systemProperties.put(it.key.toString(), it.value.toString())
            }
        }
    }


    @TaskAction
    void build() {
        def solution = getShathelSolution()
        def stack = getShathelCurrentStack(solution)
        def command = stack.createStartCommand(withOptionalDependencies)
        solution.run(command)
        notifyTasks(stack)

    }


}

class ShathelStopTask extends ShathelOperationTask {

    boolean withOptionalDependencies = false
    boolean withDependencies = true


    @TaskAction
    void build() {
        def solution = getShathelSolution()
        def stack = getShathelCurrentStack(solution)
        try {
            def command = stack.createStopCommand(withDependencies, withOptionalDependencies)
            solution.run(command)
        } finally {
            SshTunelManagerImpl.globalCloseAll()
        }
    }
}


class ShathelDestroyTask extends ShathelOperationTask {


    @TaskAction
    void build() {
        def solution = getShathelSolution()
        def environment = solution.getEnvironment(getExtension().getShathelEnvironmentName())
        environment.destroy()
    }
}