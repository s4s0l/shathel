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


    protected Parameters getShathelParameters() {
        ShathelExtension extension = getExtension()
        def paramsWithDefault = extension.shathelParams.collectEntries {
            [(Parameters.getNormalizedParameterName(it.key.toString())): it.value.toString()]
        }
        paramsWithDefault.putIfAbsent(CommonParams.SHATHEL_ENV, "local")
        String envName = paramsWithDefault[CommonParams.SHATHEL_ENV]
        paramsWithDefault.putIfAbsent("shathel.env.${envName}.dependenciesDir".toString(), getDependenciesDir().absolutePath)
        paramsWithDefault.putIfAbsent(CommonParams.SHATHEL_DIR, new File(project.buildDir, ".shathel").absolutePath)
        paramsWithDefault[CommonParams.SHATHEL_DIR] = absolutize(paramsWithDefault.get(CommonParams.SHATHEL_DIR))
        if (!new File("${paramsWithDefault[CommonParams.SHATHEL_DIR]}/shathel-solution.yml").exists()) {
            paramsWithDefault['shathel.solution.name'] = "shathel-gradle-${project.getName()}".toString()
        }
        Parameters.fromMapWithSysPropAndEnv(paramsWithDefault)


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

    String getShathelEnvironmentName() {
        return getShathelParameters().getParameter(CommonParams.SHATHEL_ENV).get()
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

    File getDependenciesDir() {
        new File(project.rootProject.buildDir, "shathel-dependencies")
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
        if (!environment.isInitialized()) {
            environment.initialize()
        }
        return solution.openStack(environment, new StackLocator(LocalOverriderDownloader.CURRENT_PROJECT_LOCATION))
    }


    Map<String, String> getDefaultPropsToLeave() {
        [
                "shathel.plugin.local.override.mappings"                    : getShathelMappingsDir().getAbsolutePath(),
                "shathel.plugin.local.override.current"                     : getShathelCurrentStackDir().getAbsolutePath(),
                "shathel.plugin.current.gav"                                : new StackReference(project.group, project.name, project.version).gav,
                "shathel.plugin.current"                                    : LocalOverriderDownloader.CURRENT_PROJECT_LOCATION,
                (CommonParams.SHATHEL_ENV)                                  : shathelEnvironmentName,
                "shathel.env.${getShathelEnvironmentName()}.dependenciesDir": getDependenciesDir().absolutePath,
                "shathel.env.${getShathelEnvironmentName()}.init"           : "true",
                (CommonParams.SHATHEL_DIR)                                  : getShathelDir()
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
        try {
            def command = stack.createStopCommand(withDependencies, withOptionalDependencies)
            stack.run(command)
        } finally {
            SshTunelManagerImpl.globalCloseAll()
        }


    }
}