package org.s4s0l.shathel.commons.dind;

import org.s4s0l.shathel.commons.core.environment.EnvironmentContext;
import org.s4s0l.shathel.commons.docker.DockerInfoWrapper;
import org.s4s0l.shathel.commons.swarm.SwarmClusterWrapper;
import org.s4s0l.shathel.commons.docker.DockerWrapper;
import org.s4s0l.shathel.commons.machine.vbox.NetworkSettings;
import org.s4s0l.shathel.commons.swarm.SwarmEnvironmentDescription;
import org.s4s0l.shathel.commons.utils.ExecWrapper;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Matcin Wielgus
 */
public class DindClusterWrapper implements SwarmClusterWrapper {
    private static final Logger LOGGER = getLogger(DindClusterWrapper.class);

    private final EnvironmentContext context;

    public DindClusterWrapper(EnvironmentContext context) {
        this.context = context;
    }

    DockerWrapper getLocalWrapper() {
        return new DockerWrapper();
    }

    @Override
    public List<String> getNodeNames() {
        List<Map<String, String>> maps = getLocalWrapper().containerBasicInfoByFilter("label=org.shathel.env.dind=" + getNetworkName());
        return maps.stream().map(x -> x.get("Names")).sorted().collect(Collectors.toList());
    }

    @Override
    public String getDataDirectory() {
        return "/shathel-data";
    }

    @Override
    public void start(String node) {
        getLocalWrapper().containerStart(node);
    }

    @Override
    public void stop(String node) {
        getLocalWrapper().containerStop(node);
    }

    @Override
    public String ssh(String node, String command) {
        return getLocalWrapper().containerExec(node, command);
    }

    @Override
    public String sudo(String node, String command) {
        return ssh(node, command);
    }

    @Override
    public void scp(String from, String to) {
        getLocalWrapper().containerScp(from, to);
    }



    @Override
    public void destroy() {
        getNodeNames().forEach(it ->
                getLocalWrapper().containerRemoveIfPresent(it)
        );
        getLocalWrapper().networkRemove(getNetworkName());
    }

    private boolean isReachable(String node) {
        try {
            return "alive".equals(getLocalWrapper().containerExec(node, "echo alive"));
        } catch (Exception e) {
            LOGGER.trace("Ignored exception during reachability testing", e);
            return false;
        }
    }

    @Override
    public Node getNode(String nodeName) {
        return getLocalWrapper().containerBasicInfoByFilter("name=" + nodeName)
                .stream()
                .filter(x -> nodeName.equals(x.get("Names")))
                .findFirst()
                .map(x -> new Node(
                        nodeName,
                        x.getOrDefault("Status", "").startsWith("Up"),
                        isReachable(nodeName)))
                .orElseThrow(() -> new RuntimeException("Node " + nodeName + " not found"));
    }

    @Override
    public DockerWrapper getDocker(String node) {
        String ip = getIp(node);
        return new DockerWrapper(new ExecWrapper(LOGGER, "docker --host " + ip));
    }

    @Override
    public void setKernelParam(String param) {
        LOGGER.warn("!Set parameter like: sudo sysctl -w " + param + " or set it in file /etc/sysctl.conf");
    }

    @Override
    public String getNonRootUser() {
        return "root";
    }


    @Override
    public String getIp(String node) {
        return getLocalWrapper().containerGetIpInNetwork(node, getNetworkName());
    }


    private String getNetworkName() {
        return ("shathel-" + context.getContextName()).toLowerCase();
    }

    @Override
    public CreationResult createNodeIfNotExists(String machineName, NetworkSettings ns, int expectedIp, String registryMirrorHost) {
        boolean modified = false;
        if (!getLocalWrapper().networkExistsByFilter("name=" + getNetworkName())) {
            getLocalWrapper().networkCreate(getNetworkName(), ns.getCidr(0));
            modified = true;
        }

        if (!getLocalWrapper().containerExists(machineName)) {
            getLocalWrapper().containerCreate("--privileged --label org.shathel.env.dind=" + getNetworkName() + " "
                    + " --hostname " + machineName
                    + " -v " + machineName.toLowerCase() + "-data:/shathel-data "
                    + " -v " + machineName.toLowerCase() + "-image:/var/lib/docker/image "
                    + " -v " + machineName.toLowerCase() + "-vfs:/var/lib/docker/vfs "
                    + " --name " + machineName + " --net " + getNetworkName() + " --ip " + ns.getAddress(expectedIp) + " -d docker:1.13.1-dind "
                    + " --registry-mirror " + registryMirrorHost);
            modified = true;
        }
        getLocalWrapper().containerStart(machineName);
        return new CreationResult(getIp(machineName), modified);
    }

    @Override
    public Map<String, String> getDockerEnvs(String node) {
        HashMap<String, String> ret = new HashMap<>();
        ret.put("DOCKER_CERT_PATH", "");
        ret.put("DOCKER_HOST", "tcp://" + getIp(node) + ":2375");
        ret.put("DOCKER_TLS_VERIFY", "");
        ret.put("DOCKER_MACHINE_NAME", "");
        ret.put("DOCKER_API_VERSION", "");
        return ret;
    }

    @Override
    public int getExpectedNodeCount() {
        return SwarmEnvironmentDescription.getNodesCount(context);
    }

    @Override
    public int getExpectedManagerNodeCount() {
        return SwarmEnvironmentDescription.getManagersCount(context);
    }
}
