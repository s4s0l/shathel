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
        swarmClusterWrapper.labelNode("${CLUSTER_NAME}-manager-1", [
                "shathel.node.main":"true",
                "shathel.node.name":"manager-1"
        ])

        log "Saving tokens"
        String manager_token = swarmClusterWrapper.getDocker("${CLUSTER_NAME}-manager-1").swarmTokenForManager()
        String worker_token = swarmClusterWrapper.getDocker("${CLUSTER_NAME}-manager-1").swarmTokenForWorker()


        (numberOfManagers < 2 ? [] : 2..numberOfManagers).each { n ->
            String nodeName = "${CLUSTER_NAME}-manager-${n}"
            cr = swarmClusterWrapper.createNodeIfNotExists(nodeName, ns, currentIp--, "https://${MANAGER_IP}:4001")

            def ip = cr.ip
            modified = modified || cr.modified
            distributeKeys(nodeName, MANAGER_IP)
            joinSwarm(nodeName, ip, manager_token, MANAGER_IP)
            swarmClusterWrapper.labelNode(nodeName, [
                    "shathel.node.main":"false",
                    "shathel.node.name":"manager-${n}"
            ])
        }

        (numberOfWorkers < 1 ? [] : 1..numberOfWorkers).each { n ->
            String nodeName = "${CLUSTER_NAME}-worker-${n}"
            cr = swarmClusterWrapper.createNodeIfNotExists(nodeName, ns, currentIp--, "https://${MANAGER_IP}:4001")

            def ip = cr.ip
            modified = modified || cr.modified
            distributeKeys(nodeName, MANAGER_IP)
            joinSwarm(nodeName, ip, worker_token, MANAGER_IP)
            swarmClusterWrapper.labelNode(nodeName, [
                    "shathel.node.main":"false",
                    "shathel.node.name":"worker-${n}"
            ])

        }





//        new PortainerCustomizer().with {
//            log "Launching Portainer"
//            if (!swarmClusterWrapper.getDocker("${CLUSTER_NAME}-manager-1").serviceRunning("portainer")) {
//                swarmClusterWrapper.getDocker("${CLUSTER_NAME}-manager-1").serviceCreate(
//                        """--name portainer
//                        --publish 9000:9000
//                        --constraint node.role==manager
//                        --mount type=bind,src=/var/run/docker.sock,dst=/var/run/docker.sock
//                        portainer/portainer
//                        -H unix:///var/run/docker.sock""".replace("\n", ""))
//            }
//            log "Initiating portainer configuration"
//            customizePortainer(9000, MANAGER_IP, swarmClusterWrapper)
//        }

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
        if (swarmClusterWrapper.getDocker(nodeName).swarmActive()) {
            log "Swarm already present"
        } else {
            log "Joining swarm on node $nodeName"
            swarmClusterWrapper.getDocker(nodeName).swarmJoin(advertiseIp, manager_token, MANAGER_IP)
        }
    }

    private void initSwarm(String nodeName, String advertiseIp) {
        log "Swarm Init"
        if (swarmClusterWrapper.getDocker(nodeName).swarmActive()) {
            log "Swarm already present"
        } else {
            log "Initializing swarm on node $nodeName"
            swarmClusterWrapper.getDocker(nodeName).swarmInit(advertiseIp)
        }
    }

    private void startRegistries(String nodeName, String MANAGER_IP) {
        generateRegistryCertificates(MANAGER_IP)

        distributeKeys(nodeName, MANAGER_IP)
        distributeKeys(nodeName, nodeName)


        log "Prepare ${nodeName} for hosting registry and mirror containers"

        swarmClusterWrapper.sudo("${nodeName}", "mkdir -p ${swarmClusterWrapper.getDataDirectory()}/certs/registry")
        swarmClusterWrapper.sudo("${nodeName}", "mkdir -p ${swarmClusterWrapper.getDataDirectory()}/certs/mirror")
        swarmClusterWrapper.sudo("${nodeName}", "chown -R ${swarmClusterWrapper.getNonRootUser()} ${swarmClusterWrapper.getDataDirectory()}/certs")
        swarmClusterWrapper.scp("${tmpDir}/registries/mirrorcerts/ca.crt", "${nodeName}:${swarmClusterWrapper.getDataDirectory()}/certs/mirror/ca.crt")
        swarmClusterWrapper.scp("${tmpDir}/registries/mirrorcerts/domain.key", "${nodeName}:${swarmClusterWrapper.getDataDirectory()}/certs/mirror/domain.key")
        swarmClusterWrapper.scp("${tmpDir}/registries/certs/ca.crt", "${nodeName}:${swarmClusterWrapper.getDataDirectory()}/certs/registry/ca.crt")
        swarmClusterWrapper.scp("${tmpDir}/registries/certs/domain.key", "${nodeName}:${swarmClusterWrapper.getDataDirectory()}/certs/registry/domain.key")

        log "Run mirror repository container"
        swarmClusterWrapper.getDocker(nodeName).containerRemoveIfPresent("shathel-mirror-registry")
        swarmClusterWrapper.getDocker(nodeName).exec.executeForOutput("""
          run -d --restart=always -p 4001:5000 --name shathel-mirror-registry 
         -v ${swarmClusterWrapper.getDataDirectory()}/mirror:/var/lib/registry 
         -v ${swarmClusterWrapper.getDataDirectory()}/certs/mirror:/certs 
         -e REGISTRY_HTTP_TLS_CERTIFICATE=/certs/ca.crt 
         -e REGISTRY_HTTP_TLS_KEY=/certs/domain.key 
         -e REGISTRY_PROXY_REMOTEURL=https://registry-1.docker.io 
        registry:2.5
        """.replace("\n", ""))

        log "Run repository container"
        swarmClusterWrapper.getDocker(nodeName).containerRemoveIfPresent("shathel-registry")
        swarmClusterWrapper.getDocker(nodeName).exec.executeForOutput("""
             run -d --restart=always -p 4000:5000 --name shathel-registry 
             -v ${swarmClusterWrapper.getDataDirectory()}/registry:/var/lib/registry 
             -v ${swarmClusterWrapper.getDataDirectory()}/certs/registry:/certs 
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

