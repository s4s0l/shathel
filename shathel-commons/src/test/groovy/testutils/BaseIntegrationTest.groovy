package testutils

import org.apache.commons.io.FileUtils
import org.s4s0l.shathel.commons.core.MapParameters
import org.s4s0l.shathel.commons.core.Parameters
import org.s4s0l.shathel.commons.core.environment.Environment
import org.s4s0l.shathel.commons.utils.IoUtils
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Marcin Wielgus
 */
abstract class BaseIntegrationTest extends Specification {
    @Shared
    String environmentName
    @Shared
    String network
    @Shared
    String solutionDescription
    @Shared
    File dependenciesDir

    def setupSpec() {
        setupEnvironment()
        cleanupEnvironment()
        FileUtils.deleteDirectory(getRootDir())

    }

//    def cleanupSpec(){
//        cleanupEnvironment()
//    }

    def onEnd() {
        cleanupEnvironment()
        true
    }

    abstract cleanupEnvironment()

    abstract setupEnvironment()

    String getRootDirName() {
        "build/Test${getClass().getSimpleName()}"
    }

    File getRootDir() {
        return new File(getRootDirName())
    }


    Parameters prepare(Map additionalParams = [:], String sourceDir = "sampleDependencies") {
        additionalParams = additionalParams.collectEntries {[(it.key.toString()):it.value.toString()]}
        File src = new File("src/test/$sourceDir")
        if (dependenciesDir == null)
            dependenciesDir = new File(getRootDir(), ".dependency-cache")
        dependenciesDir.mkdirs()
        Parameters parameters = MapParameters.builder()
                .parameter("shathel.env.${environmentName}.safePassword", "MySecretPassword")
                .parameter("shathel.env.${environmentName}.dependenciesDir", dependenciesDir.absolutePath)
                .parameter("shathel.env.${environmentName}.net", network)
                .parameter("shathel.solution.name", getClass().getSimpleName())
                .parameters(additionalParams)
                .build().hiddenBySystemProperties()
        src.listFiles()
                .findAll { it.isDirectory() }
                .findAll {
            !new File(dependenciesDir, "${it.getName()}.zip").exists()
        }
        .each {
            IoUtils.zipIt(it,
                    new File(dependenciesDir, "${it.getName()}.zip"))
        }
        if(solutionDescription!=null){
            new File(getRootDir(), "shathel-solution.yml").text = solutionDescription
        }
        return parameters
    }


    boolean waitForService(Environment e, String serviceName) {
        def node = e.getEnvironmentApiFacade().getDockerForManagementNode()
        int i = 0;
        while (i < 25 && node.serviceRunningRatio(serviceName) < 0.9999f) {
            Thread.sleep(1000)
            i++;
        }
        return i < 25;
    }

    String execInAnyTask(Environment e, String serviceName, String command) {
        def node = e.getEnvironmentApiFacade().getDockerForManagementNode()
        def id = node.serviceContainers(serviceName)[0]
        node.containerExec(id, command)
    }

    List<String> execInAllTasks(Environment e, String serviceName, String command) {
        def node = e.getEnvironmentApiFacade().getDockerForManagementNode()
        node.serviceContainers(serviceName).collect {
            node.containerExec(it, command)
        }
    }

}


