package org.s4s0l.shathel.commons.docker

import groovy.json.JsonSlurper
import org.s4s0l.shathel.commons.utils.ExecWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Marcin Wielgus
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

    static Object fixPsOutToJson(String out) {
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
            String inspect = exec.executeForOutput("inspect ${it}")
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

    /**
     * gets labels of services
     * Adds extra labels "shathel.service.ratio" "shathel.service.mode" "shathel.service.name"
     * "shathel.service.replicas", "shathel.service.count", "shathel.service.expectedCount"
     * @param filter
     * @return
     */
    List<Map<String, String>> servicesLabels(String filter) {
        def output = exec.executeForOutput("service ls -f $filter")
        def parsed = parseServiceLsOutput(output);

        parsed.collect {
            String inspect = exec.executeForOutput("service inspect ${it.key}")
            def val = new JsonSlurper().parseText(inspect);
            def ret = [:]
            ret << val[0].Spec.Labels
            ret << [
                    "shathel.service.name"         : val[0].Spec.Name,
                    "shathel.service.ratio"        : it.value.ratio,
                    "shathel.service.mode"         : it.value.mode,
                    "shathel.service.replicas"     : it.value.replicas,
                    "shathel.service.count"        : it.value.count,
                    "shathel.service.expectedCount": it.value.expectedCount,
            ]

        }
    }

    Map serviceInspect(String serviceName){
        String inspect = exec.executeForOutput("service inspect ${serviceName}")
        def val = new JsonSlurper().parseText(inspect);
        return val[0]
    }
    /**
     * eg aoutput:
     *  [{
     *                 name:'service1'
     *                 mode:'global'
     *                 replicas: '1/2'
     *                 ratio: '0.5'
     *                 expectedCount: '2'
     *                 count: '1'
     *    },{...}]
     * @param output
     * @return
     */
    List<Map<String, String>> servicesStatus(String filter){
        def output = exec.executeForOutput("service ls -f $filter")
        return parseServiceLsOutput(output).collect {it.value}
    }

    boolean serviceRunning(String containerName) {
        return "" != exec.executeForOutput("service ls -q -f name=$containerName")
    }

    /**
     * returns container names for service
     * @param containerName
     * @return
     */
    List<String> serviceContainers(String serviceName) {
        exec.executeForOutput("service ps $serviceName --no-trunc").readLines().collect {
            it.split("\\s+")
        }
        .findAll { it[0] != "ID" && it[4] == "Running" }
                .collect { it[1] + "." + it[0] }
    }

    void serviceCreate(String params) {
        exec.executeForOutput("service create $params")
    }

    void serviceRemove(String params) {
        exec.executeForOutput("service rm $params")
    }

    /**
     * returns json representation for docker info
     * @return
     */
    Map daemonInfo() {
        def output = exec.executeForOutput("info", "-f", "{{ json . }}")
        return new JsonSlurper().parseText(output);
    }

    /**
     * eg aoutput:
     *  ['service1': [
     *                 name:'service1'
     *                 mode:'global'
     *                 replicas: '1/2'
     *                 ratio: '0.5'
     *                 expectedCount: '2'
     *                 count: '1'
     *              ],
     *   'service2': [
     *                  ...
     *              ]
     * @param output
     * @return
     */
    Map<String, String> parseServiceLsOutput(String output) {
        output.readLines()
                .collect { it.split("\\s+") }
                .findAll { it[0] != "ID" }
                .collectEntries {
            def x = it[3] =~ /([0-9]+)/
            def count = Integer.parseInt(x[0][1]);
            def expectedCount = Integer.parseInt(x[1][1])
            def ratio = expectedCount == 0  ? 0 : count / expectedCount
            [(it[1]): [
                    name         : it[1],
                    mode         : it[2],
                    replicas     : it[3],
                    ratio        : "$ratio".toString(),
                    expectedCount: "$expectedCount".toString(),
                    count        : "$count".toString(),
            ]]
        }
    }

    float serviceRunningRatio(String serviceName) {
        def output = exec.executeForOutput("service ls -f name $serviceName")
        def parsed = parseServiceLsOutput(output)
        if (parsed[serviceName] == null) {
            return 0f
        } else {
            Float.parseFloat(parsed[serviceName].ratio)
        }
    }

    void stackDeploy(File composeFile, String deploymentName, Map<String, String> environment = [:]) {
        LOGGER.info("docker: deploying stack $deploymentName from ${composeFile.absolutePath}")
        exec.executeForOutput(composeFile.getParentFile(), environment, "stack deploy --compose-file ${composeFile.absolutePath} $deploymentName")
    }

    void stackUnDeploy(File composeFile, String deploymentName, Map<String, String> environment = [:]) {
        LOGGER.info("docker: undeploying stack $deploymentName from ${composeFile.absolutePath}")
        try {
            exec.executeForOutput(composeFile.getParentFile(), environment, "stack rm $deploymentName")

        } catch (ExecWrapper.ExecWrapperException e) {
            if (e.getOutput().contains("has active endpoints")) {
                LOGGER.trace("Unable to undeploy stack, sometimes this means docker is stupid and cannot remove network right away, will try again in 5s",e)
                LOGGER.warn("Unable to undeploy stack, sometimes this means docker is stupid and cannot remove network right away, will try again in 5s")
                Thread.sleep(5000)
                exec.executeForOutput(composeFile.getParentFile(), environment, "stack rm $deploymentName")
            }
        }
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

    void swarmInit() {
        exec.executeForOutput("swarm init")
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

    void buildAndTag(File file, String dockerfile, Map<String, String> args, String tag) {
        LOGGER.info("docker: building $tag")
        def a = args.collect { "--build-arg $it.key=$it.value" }.join(" ")
        exec.executeForOutput(file, [:], "build -t $tag -f $dockerfile $a ${file.getAbsolutePath()}")
    }

    void push(String tag){
        exec.executeForOutput("push $tag")
    }

    void pull(String tag) {
        LOGGER.info("docker: pulling $tag")
        exec.executeForOutput("pull $tag")
    }

    void swarmNodeSetLabels(String nodeName, Map<String, String> labels) {
        exec.executeForOutput("node update ${labels.collect { "--label-add ${it.key}=${it.value}" }.join(" ")} $nodeName")
    }

    boolean secretExists(String name) {
        return exec.executeForExitValue("secret inspect $name") == 0
    }

    void secretCreate(String name, byte[] secret) {
        exec.executeForOutput(secret, new File("."), [:], "secret", "create", "$name", "-")
    }

    void secretRemove(String name) {
        if (secretExists(name))
            exec.executeForOutput("secret rm $name")
    }
}
