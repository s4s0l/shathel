package testutils

import org.apache.commons.io.FileUtils
import org.s4s0l.shathel.commons.DefaultExtensionContext
import org.s4s0l.shathel.commons.Shathel
import org.s4s0l.shathel.commons.core.CommonParams
import org.s4s0l.shathel.commons.core.MapParameters
import org.s4s0l.shathel.commons.core.Parameters
import org.s4s0l.shathel.commons.core.dependencies.FileStackDependencyDownloader
import org.s4s0l.shathel.commons.core.environment.Environment
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Marcin Wielgus
 */
abstract class BaseIntegrationTest extends Specification {
    @Shared
    String environmentName
    @Shared
    String solutionDescription
    @Shared
    File dependenciesDir

    def setupSpec() {
        setupEnvironment()
        cleanupEnvironment()
        FileUtils.deleteDirectory(getRootDir())

    }

    def getTestClassName() {
        return getClass().getSimpleName()
    }


    def onEnd() {
        cleanupEnvironment()
        true
    }

    abstract cleanupEnvironment()

    abstract setupEnvironment()

    String getRootDirName() {
        "build/Test${getTestClassName()}"
    }

    File getRootDir() {
        return new File(getRootDirName())
    }


    Shathel shathel(Map additionalParams = [:], String sourceDir = "sampleDependencies") {
        new File(rootDir, "shathel-solution.yml").with {
            if (exists()) delete()
        }
        additionalParams = additionalParams.collectEntries {
            [(it.key.toString()): it.value.toString()]
        }
        if (dependenciesDir == null)
            dependenciesDir = new File(getRootDir(), ".dependency-cache")
        dependenciesDir.mkdirs()
        File src = new File("src/test/$sourceDir")
        Parameters parameters = MapParameters.builder()
                .parameter(CommonParams.SHATHEL_DIR, rootDir.absolutePath)
                .parameter("shathel.env.${environmentName}.safePassword", "MySecretPassword")
                .parameter("shathel.env.${environmentName}.dependenciesDir", dependenciesDir.absolutePath)
                .parameter(FileStackDependencyDownloader.SHATHEL_FILE_BASE_DIR, src.getAbsolutePath())
                .parameter("shathel.solution.name", getTestClassName())
                .parameters(additionalParams)
                .build().hiddenBySystemProperties()

        if (solutionDescription != null) {
            new File(getRootDir(), "shathel-solution.yml").text = solutionDescription
        }
        def extCtxt = DefaultExtensionContext.create()
        return new Shathel(parameters, extCtxt)
    }


    boolean waitForService(Environment e, String serviceName) {
        def node = e.getEnvironmentApiFacade().getManagerNodeWrapper()
        int i = 0
        while (i < 25 && node.serviceRunningRatio(serviceName) < 0.9999f) {
            Thread.sleep(1000)
            i++
        }
        return i < 25
    }

    String execInAnyTask(Environment e, String serviceName, String command) {
        def node = e.getEnvironmentApiFacade().getManagerNodeWrapper()
        def id = node.serviceContainers(serviceName)[0]
        node.containerExec(id, command)
    }

    List<String> execInAllTasks(Environment e, String serviceName, String command) {
        def node = e.getEnvironmentApiFacade().getManagerNodeWrapper()
        node.serviceContainers(serviceName).collect {
            node.containerExec(it, command)
        }
    }

    Map<String, String> getPrivateProperties() {
        Map<String, String> ret = [:]
        Properties properties = new Properties()
        properties.load(new FileInputStream("../private.properties"))
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key)
            ret.put(key, value)
        }
        return ret
    }

}


