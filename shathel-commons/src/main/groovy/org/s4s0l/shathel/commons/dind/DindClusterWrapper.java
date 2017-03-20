package org.s4s0l.shathel.commons.dind;

import org.s4s0l.shathel.commons.core.environment.EnvironmentContext;
import org.s4s0l.shathel.commons.docker.DockerInfoWrapper;
import org.s4s0l.shathel.commons.docker.DockerWrapper;
import org.s4s0l.shathel.commons.machine.vbox.NetworkSettings;
import org.s4s0l.shathel.commons.swarm.SwarmClusterWrapper;
import org.s4s0l.shathel.commons.swarm.SwarmNodeCreator;
import org.s4s0l.shathel.commons.utils.ExecWrapper;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Marcin Wielgus
 */
public class DindClusterWrapper implements SwarmClusterWrapper, SwarmNodeCreator {
    private static final Logger LOGGER = getLogger(DindClusterWrapper.class);

    private final EnvironmentContext context;

    public DindClusterWrapper(EnvironmentContext context) {
        this.context = context;
    }

    DockerWrapper getLocalWrapper() {
        return new DockerWrapper();
    }

    @Override
    public EnvironmentContext getEnvironmentContext() {
        return context;
    }

    @Override
    public void refreshCaches() {

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
    public Map<String, Node> getAllNodes() {
        List<Map<String, String>> maps = getLocalWrapper().containerBasicInfoByFilter("label=org.shathel.env.dind=" + getNetworkName());
        List<String> nodeNames = maps.stream().map(x -> x.get("Names")).sorted().collect(Collectors.toList());
        return nodeNames.stream().map(nodeName -> extractNodeDetails(nodeName))
                .collect(Collectors.toMap(Node::getName, Function.identity()));
    }

    private Node extractNodeDetails(String nodeName) {
        Node names = getLocalWrapper().containerBasicInfoByFilter("name=" + nodeName)
                .stream()
                .filter(x -> nodeName.equals(x.get("Names")))
                .findFirst()
                .map(x -> extractNodeDetaails(nodeName, x))
                .orElseThrow(() -> new RuntimeException("Node " + nodeName + " not found"));
        return names;
    }

    private Node extractNodeDetaails(String nodeName, Map<String, String> x) {
        boolean started = x.getOrDefault("Status", "").startsWith("Up");
        String ip = started ? getLocalWrapper().containerGetIpInNetwork(nodeName, getNetworkName()) : "";
        DockerWrapper wrapper = started ? new DockerWrapper(new ExecWrapper(LOGGER, "docker --host " + ip)) : null;
        DockerInfoWrapper dockerInfoWrapper = started ? new DockerInfoWrapper(wrapper.daemonInfo(), nodeName) : null;
        return new Node(
                nodeName,
                started,
                wrapper,
                dockerInfoWrapper,
                ip,
                started ? getDockerEnvironments(ip) : Collections.EMPTY_MAP
                );
    }


    @Override
    public void setKernelParam(String param) {
        LOGGER.warn("!Set parameter like: sudo sysctl -w " + param + " or set it in file /etc/sysctl.conf");
    }

    @Override
    public String getNonRootUser() {
        return "root";
    }


    private String getNetworkName() {
        return ("shathel-" + context.getContextName()).toLowerCase();
    }

    @Override
    public SwarmNodeCreator.CreationResult createNodeIfNotExists(String machineName, NetworkSettings ns, int expectedIp, String registryMirrorHost) {
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
        return new SwarmNodeCreator.CreationResult(getIp(machineName), modified);
    }


    private static Map<String, String> getDockerEnvironments(String ip) {
        HashMap<String, String> ret = new HashMap<>();
        ret.put("DOCKER_CERT_PATH", "");
        ret.put("DOCKER_HOST", "tcp://" + ip + ":2375");
        ret.put("DOCKER_TLS_VERIFY", "");
        ret.put("DOCKER_MACHINE_NAME", "");
        ret.put("DOCKER_API_VERSION", "");
        return ret;
    }

}
