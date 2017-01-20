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

    void copy(String from, String to) {
        exec.executeForOutput(new File("."),
                "scp $from $to");
    }

    String sudo(String node, String command) {
        return exec.executeForOutput(new File("."),
                "ssh $node sudo $command");
    }

    def restart(String node) {
        exec.executeForOutput(new File("."),
                "restart $node ");
    }

    def regenerateCerts(String node) {
        exec.executeForOutput(new File("."),
                "regenerate-certs -f  $node ");
    }

    def ssh(String node, String command) {
        exec.executeForOutput(new File("."),
                "ssh $node $command");
    }

    def create(String string) {
        exec.executeForOutput(new File("."),
                "create $string");
    }

    String getIp(String node) {
        exec.executeForOutput(new File("."),
                "ip $node")
    }

    List<String> getMachinesByName(String machineNamePattern) {
        exec.executeForOutput("ls -q --filter name=${machineNamePattern}")
                .split("\n")
                .findAll { it != "" }
    }

    def remove(String machineName) {
        exec.executeForOutput("rm -f $machineName")
    }

    def stop(String machineName) {
        if ("Running" == exec.executeForOutput("status $machineName").trim())
            exec.executeForOutput("stop $machineName")
    }

    def start(String machineName) {
        if ("Running" != exec.executeForOutput("status $machineName").trim())
            exec.executeForOutput("start $machineName")
    }
}
