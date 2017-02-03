package org.s4s0l.shathel.commons.machine.vbox

import org.s4s0l.shathel.commons.docker.DockerMachineWrapper
import org.s4s0l.shathel.commons.docker.OpenSslWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Matcin Wielgus
 */
class VBoxSwarmCluster {
    private final DockerMachineWrapper machine;
    private final String CLUSTER_NAME
    private final int numberOfManagers
    private final int numberOfWorkers
    private final String net
    private final NetworkSettings ns
    private final String tmpDir
    private final String MACHINE_OPTS

    VBoxSwarmCluster(File workDir, String clusterName, int numberOfManagers, int numberOfWorkers, String net) {
        this.CLUSTER_NAME = clusterName
        this.numberOfManagers = numberOfManagers
        this.numberOfWorkers = numberOfWorkers
        this.net = net
        ns = new NetworkSettings(net)
        tmpDir = workDir. with {
            mkdirs()
            absolutePath
        }
        machine = new DockerMachineWrapper(workDir);
        MACHINE_OPTS = "--virtualbox-hostonly-cidr=${ns.getCidr(254)} " +
                "--virtualbox-boot2docker-url https://github.com/boot2docker/boot2docker/releases/download/v1.13.1-rc1/boot2docker.iso"
    }


    private static
    final Logger LOGGER = LoggerFactory.getLogger(VBoxSwarmCluster.class);
    boolean modified = false;

    synchronized boolean createMachines() {
        modified = false;
        int currentIp = 99
        String password = "qwerty";

        String MANAGER_IP = createMachineIfNotExists("${CLUSTER_NAME}-manager-1", currentIp--, "${CLUSTER_NAME}-manager-1")

        startRegistries("${CLUSTER_NAME}-manager-1", MANAGER_IP)

        initSwarm("${CLUSTER_NAME}-manager-1", MANAGER_IP)

        log "Saving tokens"
        String manager_token = machine.getJoinTokenForManager("${CLUSTER_NAME}-manager-1")
        String worker_token = machine.getJoinTokenForWorker("${CLUSTER_NAME}-manager-1")
        log "Manager token:${manager_token}"
        log "Worker token:${worker_token}"

        (numberOfManagers < 2 ? [] : 2..numberOfManagers).each { n ->
            String nodeName = "${CLUSTER_NAME}-manager-${n}"
            def ip = createMachineIfNotExists(nodeName, currentIp--, MANAGER_IP)
            distributeKeys(nodeName, MANAGER_IP)
            joinSwarm(nodeName, ip, manager_token, MANAGER_IP)
        }

        (numberOfWorkers < 1 ? [] : 1..numberOfWorkers).each { n ->
            String nodeName = "${CLUSTER_NAME}-worker-${n}"
            def ip = createMachineIfNotExists(nodeName, currentIp--, MANAGER_IP)
            distributeKeys(nodeName, MANAGER_IP)
            joinSwarm(nodeName, ip, worker_token, MANAGER_IP)

        }





        new PortainerCustomizer().with {
            log "Launching Portainer"
            if (!machine.isServiceRunning("${CLUSTER_NAME}-manager-1", "portainer")) {
                machine.ssh("${CLUSTER_NAME}-manager-1",
                        """docker service create 
                --name portainer 
                --publish 9000:9000 
                --constraint 'node.role==manager' 
                --mount type=bind,src=/var/run/docker.sock,dst=/var/run/docker.sock 
                portainer/portainer 
                -H unix:///var/run/docker.sock""".replace("\n", ""))
            }
            log "Initiating portainer configuration"
            customizePortainer(CLUSTER_NAME, password, 9000, MANAGER_IP, machine)
        }

        return modified;
    }


    private void log(String msg) {
        LOGGER.info(msg)
    }

    /**
     * Sets machine ip, if already static does nothing and returns it
     * @param machineName
     * @param ipNum
     * @return
     */
    private String staticIp(String machineName, int ipNum) {
        machine.sudo(machineName, "touch /var/lib/boot2docker/bootsync.sh")
        String sudo = machine.sudo(machineName, "cat /var/lib/boot2docker/bootsync.sh")
        if (sudo.contains("#SHATHELIP_START")) {
            return (sudo =~ /#IP=([0-9\.]+)#/)[0][1]
        }
        def address = ns.getAddress(ipNum)
        log "Fixing ip address for ${machineName} to be ${address}"
        String fixationCommand = """
        #SHATHELIP_START
        #IP=${address}#
        kill `more /var/run/udhcpc.eth1.pid` && 
        ifconfig eth1 ${address} netmask ${ns.getMask()} broadcast ${ns.getBcast()} up
        #SHATELIP_END
        """
        def file = new File(tmpDir, "fix");
        try{
            file.text = fixationCommand
            machine.copy(file.absolutePath, "$machineName:/tmp/fixation")
            machine.sudo(machineName, "cp -f /tmp/fixation /var/lib/boot2docker/bootsync.sh")
            machine.restart(machineName)
            machine.regenerateCerts(machineName)
            markModified()
            machine.restart(machineName)
        }finally {
            file.delete()
        }

        return address
    }

    private markModified(){
        modified = true
    }
/**
 *
 * @return ip of machine created
 */
    private String createMachineIfNotExists(String machineName, int expectedIp, String registryMirrorHost) {
        log "Create node named $machineName"
        if (machine.getMachinesByName(machineName).isEmpty()) {
            machine.create("-d virtualbox --engine-registry-mirror https://${registryMirrorHost}:4001 ${MACHINE_OPTS} $machineName")
            markModified()
        }
        return staticIp(machineName, expectedIp)
    }

    private void distributeKeys(String to, String repositoriesIp) {
        machine.copy("$tmpDir/registries/mirrorcerts/ca.crt",
                "$to:/tmp/mirror-ca.crt")
        machine.sudo(to, "mkdir -p /etc/docker/certs.d/$repositoriesIp:4001/")
        machine.sudo(to, "cp /tmp/mirror-ca.crt /etc/docker/certs.d/$repositoriesIp:4001/ca.crt")

        machine.copy("$tmpDir/registries/certs/ca.crt",
                "$to:/tmp/repo-ca.crt")
        machine.sudo(to, "mkdir -p /etc/docker/certs.d/$repositoriesIp:4000/")
        machine.sudo(to, "cp /tmp/repo-ca.crt /etc/docker/certs.d/$repositoriesIp:4000/ca.crt")

    }


    private void joinSwarm(String nodeName, String advertiseIp, String manager_token, String MANAGER_IP) {
        log "Swarm Manager Join"
        if (machine.isSwarmActive(nodeName)) {
            log "Swarm already present"
        } else {
            machine.swarmJoin(nodeName, advertiseIp,manager_token, MANAGER_IP)
        }
    }

    private void initSwarm(String nodeName, String advertiseIp) {
        log "Swarm Init"
        //todo enclose in docker isSwarmActive()
        if (machine.ssh(nodeName, "docker info") =~ /Swarm: active/) {
            log "Swarm already present"
        } else {
            machine.ssh(nodeName,
                    "docker swarm init --listen-addr ${advertiseIp} --advertise-addr ${advertiseIp}")
        }
    }

    private void startRegistries(String nodeName, String MANAGER_IP) {
        generateRegistryCertificates(MANAGER_IP)

        distributeKeys(nodeName, MANAGER_IP)
        distributeKeys(nodeName, nodeName)


        log "Prepare ${nodeName} for hosting registry and mirror containers"

        machine.sudo("${nodeName}", "mkdir -p /registry/certs")
        machine.sudo("${nodeName}", "mkdir -p /registry/mirrorcerts")
        machine.sudo("${nodeName}", "chown -R docker /registry")
        machine.copy("${tmpDir}/registries/mirrorcerts/ca.crt", "${nodeName}:/registry/mirrorcerts/ca.crt")
        machine.copy("${tmpDir}/registries/mirrorcerts/domain.key", "${nodeName}:/registry/mirrorcerts/domain.key")
        machine.copy("${tmpDir}/registries/certs/ca.crt", "${nodeName}:/registry/certs/ca.crt")
        machine.copy("${tmpDir}/registries/certs/domain.key", "${nodeName}:/registry/certs/domain.key")

        log "Run mirror repository container"
        removeContainerIfRunning(nodeName, "shathel-mirror-registry")
        machine.ssh(nodeName, """
docker run -d --restart=always -p 4001:5000 --name shathel-mirror-registry 
 -v /registry/mirrordata:/var/lib/registry 
 -v /registry/mirrorcerts:/certs 
 -e REGISTRY_HTTP_TLS_CERTIFICATE=/certs/ca.crt 
 -e REGISTRY_HTTP_TLS_KEY=/certs/domain.key 
 -e REGISTRY_PROXY_REMOTEURL=https://registry-1.docker.io 
        registry:2.5
""".replace("\n", ""))

        log "Run repository container"
        removeContainerIfRunning(nodeName, "shathel-registry")
        machine.ssh(nodeName,
                """docker run -d --restart=always -p 4000:5000 --name shathel-registry 
 -v /registry/data:/var/lib/registry 
 -v /registry/certs:/certs 
 -e REGISTRY_HTTP_TLS_CERTIFICATE=/certs/ca.crt 
 -e REGISTRY_HTTP_TLS_KEY=/certs/domain.key 
  registry:2.5""".replace("\n", ""))
    }

    private void removeContainerIfRunning(String machineName, String containerName) {
        //todo enclose in docker isContainerRunning()
        def runningMirrorId = machine.ssh(machineName, "docker ps -q -f name=${containerName}").trim()
        if (runningMirrorId != "") {
            machine.ssh(machineName, "docker rm -f -v ${runningMirrorId}")
        }
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

