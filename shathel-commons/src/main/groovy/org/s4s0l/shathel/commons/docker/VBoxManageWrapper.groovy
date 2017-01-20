package org.s4s0l.shathel.commons.docker

import org.s4s0l.shathel.commons.utils.ExecWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.regex.Matcher

/**
 * @author Matcin Wielgus
 */
class VBoxManageWrapper {
    private static
    final Logger LOGGER = LoggerFactory.getLogger(DockerComposeWrapper.class);

    final ExecWrapper exec = new ExecWrapper(LOGGER, 'VBoxManage')

    def unregister(String vmName, boolean withDelete) {
        if (exists(vmName)) {
            def uuid = getDvdUuid(vmName, "boot2docker.iso")
            exec.executeForOutput("unregistervm $vmName ${withDelete ? "--delete" : ""}")
            if (uuid) {
                closeDvd(uuid)
            }
            waitUntilNotExists(vmName)
        }
    }

    private boolean waitUntilNotExists(String vmName) {
        (1..20).find {
            if (!exists(vmName)) {
                return true
            } else {
                sleep(1000)
                return false
            }
        }
    }

    boolean exists(String vmName) {
        exec.executeForOutput("list vms").contains("\"$vmName\"")
    }

    String getDvdUuid(String vmName, String file) {
        def output = exec.executeForOutput("showvminfo $vmName")
        Matcher a = output =~ /${file}\s+\(UUID: ([a-f0-9-]+)/
        if (a) {
            return a[0][1]
        } else {
            return null;
        }
    }


    def getUuidOfDvdInDirectory(File d) {
        def dvds = exec.executeForOutput("list dvds")
        String path = d.absolutePath.replaceAll("\\/", "\\\\/")
        Matcher a = dvds =~ /UUID:\s*([a-f0-9-]+)\n.*\n.*\nLocation:\s+$path/
        if (a) {
            return a[0][1]
        } else {
            return null;
        }
    }

    void closeDvd(String uuid) {
        exec.executeForOutput("closemedium dvd $uuid")
        (1..10).find {
            if (!exec.executeForOutput("list dvds").contains(uuid)) {
                return true
            } else {
                sleep(1000)
                return false
            }
        }
    }

    def registervm(File file) {
        exec.executeForOutput("registervm ${file.absolutePath}")
    }

    def poweroff(String machineName) {
        if (exec.executeForOutput("list runningvms").contains("\"$machineName\"")) {
            exec.executeForOutput("controlvm $machineName poweroff")
        }
    }
}
