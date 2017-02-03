package org.s4s0l.shathel.commons.dind;

import org.s4s0l.shathel.commons.core.environment.EnvironmentContext;
import org.s4s0l.shathel.commons.swarm.SwarmClusterWrapper;
import org.s4s0l.shathel.commons.docker.DockerWrapper;
import org.s4s0l.shathel.commons.machine.vbox.NetworkSettings;
import org.s4s0l.shathel.commons.utils.ExecWrapper;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public List<String> getAllNodeNames() {
        List<Map<String, String>> maps = getLocalWrapper().containerBasicInfoByFilter("label=org.shathel.env.dind=true");
        return maps.stream().map(x -> x.get("Names")).collect(Collectors.toList());
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
    public void destroy(String node) {
        getLocalWrapper().containerRemoveIfPresent(node);
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
    public DockerWrapper getWrapperForNode(String node) {
        String ip = getIp(node);
        return new DockerWrapper(new ExecWrapper(LOGGER, "docker --host " + ip));
    }

    @Override
    public String getNonRootUser() {
        return "root";
    }

    private String getIp(String node) {
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
            getLocalWrapper().containerCreate("--privileged --label org.shathel.env.dind=true --name "
                    + machineName + " --net " + getNetworkName() + " --ip " + ns.getAddress(expectedIp) + " -d docker:1.13.0-dind");
            modified = true;
        }
        getLocalWrapper().containerStart(machineName);
        return new CreationResult(getIp(machineName), modified);
    }

    @Override
    public Map<String, String> getMachineEnvs(String node) {
        HashMap<String, String> ret = new HashMap<>();
        ret.put("DOCKER_CERT_PATH", "");
        ret.put("DOCKER_HOST", getIp(node));
        ret.put("DOCKER_TLS_VERIFY", "");
        ret.put("DOCKER_MACHINE_NAME", "");
        ret.put("DOCKER_API_VERSION", "");
        return ret;
    }
}
