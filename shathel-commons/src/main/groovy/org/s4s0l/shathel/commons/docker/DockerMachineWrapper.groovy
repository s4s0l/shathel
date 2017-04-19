package org.s4s0l.shathel.commons.docker

import org.apache.commons.lang.StringUtils
import org.s4s0l.shathel.commons.utils.ExecWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Marcin Wielgus
 */
@Deprecated
class DockerMachineWrapper {
    private static
    final Logger LOGGER = LoggerFactory.getLogger(DockerMachineWrapper.class);

    final ExecWrapper exec;

    DockerMachineWrapper(File storageDir) {
        exec = new ExecWrapper(LOGGER, "docker-machine -s ${storageDir.absolutePath}")
    }

    DockerMachineWrapper() {
        exec = new ExecWrapper(LOGGER, "docker-machine")
    }


    DockerWrapper getDockerWrapperOn(String machineName) {
        def envs = getMachineEnvs(machineName)
        return new DockerWrapper(new ExecWrapper(LOGGER, 'docker', envs))
    }

    void copy(String from, String to) {
        LOGGER.info("machine: copy $from $to")
        exec.executeForOutput(new File("."),
                "scp $from $to");
    }

    Map<String, String> getMachineEnvironments(String machineName) {
        def output = exec.executeForOutput("env ${machineName}")
        [
                DOCKER_CERT_PATH   : (output =~ /[^\s]+ DOCKER_CERT_PATH="(.+)"/),
                DOCKER_HOST        : (output =~ /[^\s]+ DOCKER_HOST="(.+)"/),
                DOCKER_TLS_VERIFY  : (output =~ /[^\s]+ DOCKER_TLS_VERIFY="(.+)"/),
                DOCKER_MACHINE_NAME: (output =~ /[^\s]+ DOCKER_MACHINE_NAME="(.+)"/),
                DOCKER_API_VERSION : (output =~ /[^\s]+ DOCKER_API_VERSION="(.+)"/),
        ].collectEntries {
            [
                    (it.key):
                            it.value.size() == 1 ?
                                    (it.value[0].size() == 2 ? it.value[0][1] : "")
                                    : ""
            ]
        }.findAll { it.value != null }
    }

    String sudo(String node, String command) {
        return exec.executeForOutput(new File("."),
                "ssh $node sudo $command");
    }

    void restart(String node) {
        LOGGER.info("machine: restarting $node")
        exec.executeForOutput(new File("."),
                "restart $node ");
    }

    void regenerateCerts(String node) {
        LOGGER.info("machine: regenerating certs for $node")
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

    /**
     * Forcibly removes machine
     * @param machineName
     */
    void remove(String machineName) {
        LOGGER.info("machine: removing $machineName")
        exec.executeForOutput("rm -f $machineName")
    }

    /**
     * Stops machine if was running
     * @param machineName
     */
    void stop(String machineName) {
        if ("Running" == exec.executeForOutput("status $machineName").trim()) {
            LOGGER.info("machine: stopping $machineName")
            exec.executeForOutput("stop $machineName")
        }
    }

    /**
     * Starts machine if was not already running
     * @returns true if it was actually started, false when was running bbefore
     * @param machineName
     */
    boolean start(String machineName) {
        if ("Running" != exec.executeForOutput("status $machineName").trim()) {
            LOGGER.info("machine: starting $machineName")
            exec.executeForOutput("start $machineName")
            return true;
        }
        return false;
    }

    /**
     * returns docker-machine ls statuses
     * name -> name|driver|state|url|envs|ip
     * @return
     */
    Map<String, DockerMachineNode> getMachines() {
        def output = exec.executeForOutput("ls", "--format",
                """{ "name":"{{.Name}}","driver":"{{.DriverName}}", "state":"{{.State}}", "url":"{{.URL}}","version":"{{.DockerVersion}}" }""")
        def json = DockerWrapper.fixPsOutToJson(output)
        def resolveIpFromUrl = {
            String url ->
                if (StringUtils.isEmpty(url)) {
                    return ""
                }
                def a = url =~ /(\d+\.){3}\d+/
                if (a.find()) {
                    return a[0][0]
                }
                return ""
        }
        json.collectEntries {
            def running = "Running" == it.state
            def envs = running ? getMachineEnvironments(it.name) : [:]
            def dockerWrapper = running ? new DockerWrapper(new ExecWrapper(LOGGER, 'docker', envs)) : null
            def info = running ? new DockerInfoWrapper(dockerWrapper.daemonInfo(), it.name) : null
            [(it.name): new DockerMachineNode(
                    it.name,
                    it.driver,
                    it.state,
                    it.url,
                    it.version,
                    envs,
                    resolveIpFromUrl(it.url),
                    info,
                    dockerWrapper
            )]
        }
    }


}
