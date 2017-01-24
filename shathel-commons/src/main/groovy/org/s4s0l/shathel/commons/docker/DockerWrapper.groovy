package org.s4s0l.shathel.commons.docker

import groovy.json.JsonSlurper
import org.s4s0l.shathel.commons.utils.ExecWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Matcin Wielgus
 */
class DockerWrapper {
    private static
    final Logger LOGGER = LoggerFactory.getLogger(DockerComposeWrapper.class);

    final ExecWrapper exec;

    DockerWrapper() {
        this(new ExecWrapper(LOGGER, 'docker'))
    }


    DockerWrapper(ExecWrapper execWrapper) {
        exec = execWrapper;
    }

    /**
     * gets container ids for containers matching given filter
     * @param filter
     * @return
     */
    List<String> getContainerIdsByFilter(String filter) {
        exec.executeForOutput("ps -a -f $filter -q").readLines()
    }

    /**
     * gets networks ids for networks matching filter given
     * @param filter
     * @return
     */
    List<String> getNetworkIdsByFilter(String filter) {
        exec.executeForOutput("network ls -f $filter -q").readLines()
    }

    /**
     * removes container
     * @param containerId
     */
    void removeContainer(String containerId) {
        exec.executeForOutput("rm -f -v $containerId")
    }

    /**
     * removes network
     * @param networkId
     * @return
     */
    void removeNetwork(String networkId) {
        exec.executeForOutput("network rm $networkId")
    }

    /**
     * Gets labels map for container matching given filter.
     * @param filter
     * @return
     */
    List<Map<String, String>> getLabelsOfContainersMatching(String filter) {
        String[] dockerIds = exec.executeForOutput("ps -f $filter -q").split("\\s")
        if (dockerIds.size() == 0 || "" == dockerIds[0]) {
            return []
        }
        dockerIds.collect{
            String inspect = exec.executeForOutput("inspect ${dockerIds[0]}")
            def val = new JsonSlurper().parseText(inspect);
            def ret = [:]
            ret << val[0].Config.Labels
            ret << [name:val[0].Name]
        }
    }

    List<Map<String, String>> getServicesOfContainersMatching(String filter) {
        String[] dockerIds = exec.executeForOutput("service ls -f $filter -q").split("\\s")
        if (dockerIds.size() == 0 || "" == dockerIds[0]) {
            return []
        }
        dockerIds.collect{
            String inspect = exec.executeForOutput("service inspect ${dockerIds[0]}")
            def val = new JsonSlurper().parseText(inspect);
            def ret = [:]
            ret << val[0].Spec.Labels
            ret << [name:val[0].Spec.Name]
        }
    }

    /**
     * returns json representation for docker info
     * @return
     */
    Map getInfo() {
        def output = exec.executeForOutput("info --format '{{ json . }}'")
        return new JsonSlurper().parseText(output);
    }

    /**
     * Returns all swarm nodes
     * @return
     */
    Map getNodes() {
        String output = exec.executeForOutput("node list")
        output.readLines()
                .findAll { !it.startsWith("ID") }
                .collect { it.replaceAll("(?<=[a-z0-9]+)\\s+\\*\\s", "   ") }
                .collect { it.split("\\s+") }
                .collectEntries {
            [(it[1]):
                     [id           : it[0],
                      hostName     : it[1],
                      status       : it[2],
                      availability : it[3],
                      managerStatus: it.size() == 5 ? it[4] : null
                     ]]
        }
    }

    void stackDeploy(File composeFile, String deploymentName) {
        exec.executeForOutput(composeFile.getParentFile(), "stack deploy --compose-file ${composeFile.absolutePath} $deploymentName");
    }

    void stackUnDeploy(File composeFile, String deploymentName) {
        exec.executeForOutput(composeFile.getParentFile(), "stack rm $deploymentName");
    }


}
