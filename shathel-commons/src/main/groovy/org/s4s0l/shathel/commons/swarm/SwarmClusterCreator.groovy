package org.s4s0l.shathel.commons.swarm

import org.s4s0l.shathel.commons.docker.OpenSslWrapper
import org.s4s0l.shathel.commons.machine.vbox.NetworkSettings
import org.s4s0l.shathel.commons.machine.vbox.PortainerCustomizer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Matcin Wielgus
 */
class SwarmClusterCreator {

    private final String CLUSTER_NAME
    private final int numberOfManagers
    private final int numberOfWorkers
    private final NetworkSettings ns
    private final String tmpDir
    private final SwarmClusterWrapper swarmClusterWrapper;

    SwarmClusterCreator(SwarmClusterWrapper swarmClusterWrapper, File workDir, String clusterName, int numberOfManagers, int numberOfWorkers, String net) {
        this.CLUSTER_NAME = clusterName
        this.swarmClusterWrapper = swarmClusterWrapper
        this.numberOfManagers = numberOfManagers
        this.numberOfWorkers = numberOfWorkers
        ns = new NetworkSettings(net)
        tmpDir = workDir.with {
            mkdirs()
            absolutePath
        }
    }


    private static
    final Logger LOGGER = LoggerFactory.getLogger(SwarmClusterCreator.class);
    boolean modified = false;

    synchronized boolean createMachines() {
        modified = false;
        int currentIp = 99

        SwarmClusterWrapper.CreationResult cr = swarmClusterWrapper.createNodeIfNotExists("${CLUSTER_NAME}-manager-1", ns, currentIp--, "https://${CLUSTER_NAME}-manager-1:4001")
        String MANAGER_IP = cr.ip
        modified = modified || cr.modified


        startRegistries("${CLUSTER_NAME}-manager-1", MANAGER_IP)

        initSwarm("${CLUSTER_NAME}-manager-1", MANAGER_IP)

        log "Saving tokens"
        String manager_token = swarmClusterWrapper.getWrapperForNode("${CLUSTER_NAME}-manager-1").swarmTokenForManager()
        String worker_token = swarmClusterWrapper.getWrapperForNode("${CLUSTER_NAME}-manager-1").swarmTokenForWorker()


        (numberOfManagers < 2 ? [] : 2..numberOfManagers).each { n ->
            String nodeName = "${CLUSTER_NAME}-manager-${n}"
            cr = swarmClusterWrapper.createNodeIfNotExists(nodeName, ns, currentIp--, "https://${MANAGER_IP}:4001")
            def ip = cr.ip
            modified = modified || cr.modified
            distributeKeys(nodeName, MANAGER_IP)
            joinSwarm(nodeName, ip, manager_token, MANAGER_IP)
        }

        (numberOfWorkers < 1 ? [] : 1..numberOfWorkers).each { n ->
            String nodeName = "${CLUSTER_NAME}-worker-${n}"
            cr = swarmClusterWrapper.createNodeIfNotExists(nodeName, ns, currentIp--, "https://${MANAGER_IP}:4001")
            def ip = cr.ip
            modified = modified || cr.modified
            distributeKeys(nodeName, MANAGER_IP)
            joinSwarm(nodeName, ip, worker_token, MANAGER_IP)

        }





        new PortainerCustomizer().with {
            log "Launching Portainer"
            if (!swarmClusterWrapper.getWrapperForNode("${CLUSTER_NAME}-manager-1").serviceRunning("portainer")) {
                swarmClusterWrapper.getWrapperForNode("${CLUSTER_NAME}-manager-1").serviceCreate(
                        """--name portainer 
                        --publish 9000:9000 
                        --constraint node.role==manager 
                        --mount type=bind,src=/var/run/docker.sock,dst=/var/run/docker.sock 
                        portainer/portainer 
                        -H unix:///var/run/docker.sock""".replace("\n", ""))
            }
            log "Initiating portainer configuration"
            customizePortainer(9000, MANAGER_IP, swarmClusterWrapper)
        }

        return modified;
    }


    private void log(String msg) {
        LOGGER.info(msg)
    }


    private markModified() {
        modified = true
    }


    private void distributeKeys(String to, String repositoriesIp) {
        swarmClusterWrapper.sudo(to, 'mkdir -p /shathel-tmp')
        swarmClusterWrapper.sudo(to, 'chmod a+rw /shathel-tmp')

        swarmClusterWrapper.scp("$tmpDir/registries/mirrorcerts/ca.crt",
                "$to:/shathel-tmp/mirror-ca.crt")
        swarmClusterWrapper.sudo(to, "mkdir -p /etc/docker/certs.d/$repositoriesIp:4001/")
        swarmClusterWrapper.sudo(to, "cp /shathel-tmp/mirror-ca.crt /etc/docker/certs.d/$repositoriesIp:4001/ca.crt")

        swarmClusterWrapper.scp("$tmpDir/registries/certs/ca.crt",
                "$to:/shathel-tmp/repo-ca.crt")
        swarmClusterWrapper.sudo(to, "mkdir -p /etc/docker/certs.d/$repositoriesIp:4000/")
        swarmClusterWrapper.sudo(to, "cp /shathel-tmp/repo-ca.crt /etc/docker/certs.d/$repositoriesIp:4000/ca.crt")


    }


    private void joinSwarm(String nodeName, String advertiseIp, String manager_token, String MANAGER_IP) {
        log "Swarm Manager Join"
        if (swarmClusterWrapper.getWrapperForNode(nodeName).swarmActive()) {
            log "Swarm already present"
        } else {
            log "Joining swarm on node $nodeName"
            swarmClusterWrapper.getWrapperForNode(nodeName).swarmJoin(advertiseIp, manager_token, MANAGER_IP)
        }
    }

    private void initSwarm(String nodeName, String advertiseIp) {
        log "Swarm Init"
        if (swarmClusterWrapper.getWrapperForNode(nodeName).swarmActive()) {
            log "Swarm already present"
        } else {
            log "Initializing swarm on node $nodeName"
            swarmClusterWrapper.getWrapperForNode(nodeName).swarmInit(advertiseIp)
        }
    }

    private void startRegistries(String nodeName, String MANAGER_IP) {
        generateRegistryCertificates(MANAGER_IP)

        distributeKeys(nodeName, MANAGER_IP)
        distributeKeys(nodeName, nodeName)


        log "Prepare ${nodeName} for hosting registry and mirror containers"

        swarmClusterWrapper.sudo("${nodeName}", "mkdir -p /registry/certs")
        swarmClusterWrapper.sudo("${nodeName}", "mkdir -p /registry/mirrorcerts")
        swarmClusterWrapper.sudo("${nodeName}", "chown -R ${swarmClusterWrapper.getNonRootUser()} /registry")
        swarmClusterWrapper.scp("${tmpDir}/registries/mirrorcerts/ca.crt", "${nodeName}:/registry/mirrorcerts/ca.crt")
        swarmClusterWrapper.scp("${tmpDir}/registries/mirrorcerts/domain.key", "${nodeName}:/registry/mirrorcerts/domain.key")
        swarmClusterWrapper.scp("${tmpDir}/registries/certs/ca.crt", "${nodeName}:/registry/certs/ca.crt")
        swarmClusterWrapper.scp("${tmpDir}/registries/certs/domain.key", "${nodeName}:/registry/certs/domain.key")

        log "Run mirror repository container"
        swarmClusterWrapper.getWrapperForNode(nodeName).containerRemoveIfPresent("shathel-mirror-registry")
        swarmClusterWrapper.getWrapperForNode(nodeName).exec.executeForOutput("""
          run -d --restart=always -p 4001:5000 --name shathel-mirror-registry 
         -v /registry/mirrordata:/var/lib/registry 
         -v /registry/mirrorcerts:/certs 
         -e REGISTRY_HTTP_TLS_CERTIFICATE=/certs/ca.crt 
         -e REGISTRY_HTTP_TLS_KEY=/certs/domain.key 
         -e REGISTRY_PROXY_REMOTEURL=https://registry-1.docker.io 
        registry:2.5
        """.replace("\n", ""))

        log "Run repository container"
        swarmClusterWrapper.getWrapperForNode(nodeName).containerRemoveIfPresent("shathel-registry")
        swarmClusterWrapper.getWrapperForNode(nodeName).exec.executeForOutput("""
             run -d --restart=always -p 4000:5000 --name shathel-registry 
             -v /registry/data:/var/lib/registry 
             -v /registry/certs:/certs 
             -e REGISTRY_HTTP_TLS_CERTIFICATE=/certs/ca.crt 
             -e REGISTRY_HTTP_TLS_KEY=/certs/domain.key 
            registry:2.5""".replace("\n", ""))
    }


    private void generateRegistryCertificates(String MANAGER_IP) {
        log "Generate Certificates for mirror and repository"

        def rootDir = new File("${tmpDir}/registries");

        def keyFile = new File(rootDir, "/mirrorcerts/domain.key")
        def crtFile = new File(rootDir, "/mirrorcerts/ca.crt")
        if (!keyFile.exists() || !crtFile.exists()) {
            generateKeyPair(MANAGER_IP, keyFile, crtFile)
        }
        keyFile = new File(rootDir, "/certs/domain.key")
        crtFile = new File(rootDir, "/certs/ca.crt")
        if (!keyFile.exists() || !crtFile.exists()) {
            generateKeyPair(MANAGER_IP, keyFile, crtFile)
        }
    }

    private String generateKeyPair(String MANAGER_IP, File keyFile, File crtFile) {
        markModified()
        new OpenSslWrapper().generateKeyPair(MANAGER_IP, [MANAGER_IP],
                ["${CLUSTER_NAME}-manager-1", "dregistry.${CLUSTER_NAME}"],
                "${keyFile.absolutePath}",
                "${crtFile.absolutePath}")
    }
}

