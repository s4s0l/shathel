package org.s4s0l.shathel.commons.docker

import org.s4s0l.shathel.commons.utils.ExecWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Matcin Wielgus
 */
class DockerMachineWrapper {
    private static
    final Logger LOGGER = LoggerFactory.getLogger(DockerMachineWrapper.class);

    private final ExecWrapper exec;

    DockerMachineWrapper(File staorageDir) {
        exec = new ExecWrapper(LOGGER, "docker-machine -s ${staorageDir.absolutePath}")
    }

    DockerMachineWrapper() {
        exec = new ExecWrapper(LOGGER, "docker-machine")
    }

    private DockerWrapper getDockerWraperOnHost(String machineName){
        return new DockerWrapper(new ExecWrapper(LOGGER, "docker-machine ssh $machineName docker"))
    }

    void copy(String from, String to) {
        exec.executeForOutput(new File("."),
                "scp $from $to");
    }

    Map<String,String> getMachineEnvs(String machineName){
        def output = exec.executeForOutput("env ${machineName}")
        [
                DOCKER_CERT_PATH:(output =~ /[^\s]+ DOCKER_CERT_PATH="(.+)"/)[0][1],
                DOCKER_HOST:(output =~ /[^\s]+ DOCKER_HOST="(.+)"/)[0][1],
                DOCKER_TLS_VERIFY:(output =~ /[^\s]+ DOCKER_TLS_VERIFY="(.+)"/)[0][1],
                DOCKER_MACHINE_NAME:(output =~ /[^\s]+ DOCKER_MACHINE_NAME="(.+)"/)[0][1],
                DOCKER_API_VERSION:(output =~ /[^\s]+ DOCKER_API_VERSION="(.+)"/)[0][1],
        ]
    }

    String sudo(String node, String command) {
        return exec.executeForOutput(new File("."),
                "ssh $node sudo $command");
    }

    void restart(String node) {
        exec.executeForOutput(new File("."),
                "restart $node ");
    }

    void regenerateCerts(String node) {
        exec.executeForOutput(new File("."),
                "regenerate-certs -f  $node ");
    }

    String ssh(String node, String command) {
        exec.executeForOutput(new File("."),
                "ssh $node $command");
    }

    void create(String string) {
        exec.executeForOutput(new File("."),
                "create $string");
    }

    String getIp(String node) {
        exec.executeForOutput(new File("."),
                "ip $node")
    }

    /**
     * gets all machine names for given name filter pattern
     * @param machineNamePattern
     * @return
     */
    List<String> getMachinesByName(String machineNamePattern) {
        exec.executeForOutput("ls -q --filter name=${machineNamePattern}")
                .split("\n")
                .findAll { it != "" }
    }
    /**
     * Forcibly removes machine
     * @param machineName
     */
    void remove(String machineName) {
        exec.executeForOutput("rm -f $machineName")
    }

    /**
     * Stops machine if was running
     * @param machineName
     */
    void stop(String machineName) {
        if ("Running" == exec.executeForOutput("status $machineName").trim())
            exec.executeForOutput("stop $machineName")
    }

    /**
     * Starts machine if was not already running
     * @returns true if it was actually started, false when was running bbefore
     * @param machineName
     */
    boolean start(String machineName) {
        if ("Running" != exec.executeForOutput("status $machineName").trim()){
            exec.executeForOutput("start $machineName")
            return true;
        }
        return false;
    }

    /**
     * call info on docker on machine
     * @see DockerWrapper#getInfo()
     * @param machineName
     * @return
     */
    Map getInfo(String machineName) {
        return getDockerWraperOnHost(machineName).getInfo()
    }

    /**
     * Runs node ls on docker on given machine
     * @see DockerWrapper#getNodes()
     * @param machineName
     * @return
     */
    Map getNodes(String machineName) {
        return getDockerWraperOnHost(machineName).getNodes();
    }

    /**
     * returns docker-machine ls statuses
     * @return
     */
    Map getMachines() {
        def output = exec.executeForOutput("ls")
        return output.readLines()
        .findAll {!it.startsWith("NAME")}
        .collect { it.split("\\s+")}
        .collectEntries {
            [
                    (it[0]):
                            [
                                    name:it[0],
                                    driver:it[2],
                                    state:it[3]
                            ]
            ]
        }
    }


    String getJoinTokenForManager(String machineName){
        return ssh(machineName, "docker swarm join-token -q manager")
    }

    String getJoinTokenForWorker(String machineName){
        return ssh(machineName, "docker swarm join-token -q worker")
    }

    boolean isServiceRunning(String machineName, String containerName){
        return "" != ssh(machineName, "docker service ls -q -f name=$containerName")
    }

    boolean isReachable(String machine) {
        ssh(machine, "docker ps")
    }


    boolean isSwarmManager(String machine){
        def info = getInfo(machine)
        def thisNodeInSwarm = info.Swarm.NodeID
        def managers = info.Swarm.RemoteManagers
        if (info.Swarm.LocalNodeState != "active"
                || info.Swarm.ControlAvailable != true
                || managers.find { it.NodeID == thisNodeInSwarm } == null) {
            return false
        }
        return true
    }

    boolean isSwarmWorker(String machine){
        def info = getInfo(machine)
        def managers = info.Swarm.RemoteManagers
        if (info.Swarm.LocalNodeState != "active"
                || info.Swarm.ControlAvailable != false
                || managers.isEmpty()) {
            return false
        }
        return true
    }

    boolean isSwarmActive(String machine){
        return ssh(machine, "docker info") =~ /Swarm: active/;
    }

    void swarmJoin(String machine, String advertiseIp, String token, String managerIp){
        ssh(machine,
                "docker swarm join --listen-addr ${advertiseIp} --advertise-addr ${advertiseIp} --token ${token} ${managerIp}:2377"
        )
    }
}
