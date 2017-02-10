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
     * gets networks ids for networks matching filter given
     * @param filter
     * @return
     */
    List<String> networkIdsByFilter(String filter) {
        exec.executeForOutput("network ls -f $filter -q").readLines()
    }

    /**
     * Sample output :{"Driver": "bridge",
     "ID": "d5f560c933cc",
     "IPv6": "false",
     "Internal": "false",
     "Labels": "",
     "Name": "shathel.env.dind",
     "Scope": "local"}* @param filter
     * @return
     */
    List<Map<String, String>> networkBasicsByFilter(String filter) {
        def output = exec.executeForOutput("network", "ls", "-f", "$filter", "--format", "{{ json . }}")
        return fixPsOutToJson(output)
    }

    boolean networkExistsByFilter(String filter) {
        networkBasicsByFilter(filter).size() == 1
    }

    /**
     * removes network
     * @param networkId
     * @return
     */
    void networkRemove(String networkId) {
        LOGGER.info("docker: removing network $networkId")
        exec.executeForOutput("network rm $networkId")
    }

    /**
     * gets container ids for containers matching given filter
     * @param filter
     * @return
     */
    List<String> containerIdsByFilter(String filter) {
        exec.executeForOutput("ps -a -f $filter -q").readLines()
    }

    /**
     * gets container info by filter.
     * sample output:
     *{"Command": "\"dockerd-entrypoin...\"",
     "CreatedAt": "2017-02-03 12:11:54 +0100 CET",
     "ID": "42514f183edd",
     "Image": "docker:1.13.0-dind",
     "Labels": "a=b,org.shathel.env.dind=true",
     "LocalVolumes": "1",
     "Mounts": "7830960a420e...",
     "Names": "x-dev-manager-3",
     "Networks": "bridge",
     "Ports": "2375/tcp",
     "RunningFor": "4 seconds",
     "Size": "0 B",
     "Status": "Up 2 seconds"}* @param filter
     * @return
     */
    List<Map<String, String>> containerBasicInfoByFilter(String filter) {
        def out = exec.executeForOutput("ps", "-a", "-f", "$filter", "-q", "--format", "{{ json . }}")
        return fixPsOutToJson(out)
    }

    private Object fixPsOutToJson(String out) {
        new JsonSlurper().parseText("[${out.replaceAll("\\}\\s+\\{", "},{")}]")
    }

    /**
     * removes container
     * @param containerId
     */
    void containerRemove(String containerId) {
        LOGGER.info("docker: removing container $containerId")
        exec.executeForOutput("rm -f -v $containerId")
    }

    /**
     * Gets labels map for container matching given filter.
     * @param filter
     * @return
     */
    List<Map<String, String>> containersLabelsByFilter(String filter) {
        String[] dockerIds = exec.executeForOutput("ps -f $filter -q").split("\\s")
        if (dockerIds.size() == 0 || "" == dockerIds[0]) {
            return []
        }
        dockerIds.collect {
            String inspect = exec.executeForOutput("inspect ${dockerIds[0]}")
            def val = new JsonSlurper().parseText(inspect);
            def ret = [:]
            ret << val[0].Config.Labels
            ret << [name: val[0].Name]
        }
    }

    boolean containerRunning(String containerName) {
        "" != exec.executeForOutput("ps -q -f name=${containerName}").trim()
    }

    /**
     *
     * @param containerName
     * @return true if removed
     */
    boolean containerRemoveIfPresent(String containerName) {
        if (containerExists(containerName)) {
            containerRemove(containerName)
            return true
        }
        return false

    }

    List<Map<String, String>> servicesOfContainersMatching(String filter) {
        String[] dockerIds = exec.executeForOutput("service ls -f $filter -q").split("\\s")
        if (dockerIds.size() == 0 || "" == dockerIds[0]) {
            return []
        }
        dockerIds.collect {
            String inspect = exec.executeForOutput("service inspect ${dockerIds[0]}")
            def val = new JsonSlurper().parseText(inspect);
            def ret = [:]
            ret << val[0].Spec.Labels
            ret << [name: val[0].Spec.Name]
        }
    }

    boolean serviceRunning(String containerName) {
        return "" != exec.executeForOutput("service ls -q -f name=$containerName")
    }

    void serviceCreate(String params) {
        exec.executeForOutput("service create $params")
    }

    /**
     * returns json representation for docker info
     * @return
     */
    Map daemonInfo() {
        exec.executeForOutput("version")
        def output = exec.executeForOutput("info", "-f", "{{ json . }}")
        return new JsonSlurper().parseText(output);
    }


    void stackDeploy(File composeFile, String deploymentName) {
        LOGGER.info("docker: deploying stack $deploymentName from ${composeFile.absolutePath}")
        exec.executeForOutput(composeFile.getParentFile(), "stack deploy --compose-file ${composeFile.absolutePath} $deploymentName");
    }

    void stackUnDeploy(File composeFile, String deploymentName) {
        LOGGER.info("docker: undeploying stack $deploymentName from ${composeFile.absolutePath}")
        exec.executeForOutput(composeFile.getParentFile(), "stack rm $deploymentName");
    }

    /**
     * Returns all swarm nodes
     * @return
     */
    Map<String, Map<String, String>> swarmNodes() {
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

    boolean swarmActive() {
        return new DockerInfoWrapper(daemonInfo(), null).isSwarmActive()
    }

    void swarmJoin(String advertiseIp, String token, String managerIp) {
        exec.executeForOutput("swarm join --listen-addr ${advertiseIp} --advertise-addr ${advertiseIp} --token ${token} ${managerIp}:2377")
    }

    void swarmInit(String advertiseIp) {
        exec.executeForOutput("swarm init --listen-addr ${advertiseIp} --advertise-addr ${advertiseIp}")
    }

    String swarmTokenForManager() {
        return exec.executeForOutput("swarm join-token -q manager")
    }

    String swarmTokenForWorker() {
        return exec.executeForOutput("swarm join-token -q worker")
    }


    boolean isReachable() {
        exec.executeForOutput("ps")
    }


    void containerStart(String containerName) {
        exec.executeForOutput("start $containerName")
    }

    void containerStop(String containerName) {
        exec.executeForOutput("stop $containerName")
    }

    String containerExec(String containerName, String command) {
        exec.executeForOutput("exec $containerName $command")
    }

    void containerScp(String from, String to) {
        exec.executeForOutput("cp $from $to")
    }

    Map containerInspect(String containerName) {
        new JsonSlurper().parseText(exec.executeForOutput("inspect $containerName"))[0]
    }

    String containerGetIpInNetwork(String containerName, String networkName) {
        def inspect = containerInspect(containerName)
        inspect.NetworkSettings.Networks[networkName].IPAddress
    }

    void networkCreate(String networkName, String subnet) {
        exec.executeForOutput("network create --subnet $subnet $networkName")
    }

    boolean containerExists(String containerName) {
        return "" != exec.executeForOutput("ps -a -q -f name=${containerName}").trim()
    }

    void containerCreate(String s) {
        exec.executeForOutput("run $s")
    }

    void buildAndPush(File file, String dockerfile, Map<String, String> args, String tag) {
        LOGGER.info("docker: building $tag")
        def a = args.collect { "--build-arg $it.key=$it.value" }.join(" ")
        exec.executeForOutput(file, [:], "build -t $tag -f $dockerfile $a ${file.getAbsolutePath()}")
        exec.executeForOutput("push $tag")
    }

    void pull(String tag) {
        LOGGER.info("docker: pulling $tag")
        exec.executeForOutput("pull $tag")
    }
}
