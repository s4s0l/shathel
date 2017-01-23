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

    /**
     * removes given vm
     * @param vmName
     * @param withDelete - if true deletes all files otherwise just unregisters
     */
    void removeVm(String vmName, boolean withDelete) {
        if (isVmPresent(vmName)) {
            def uuid = getDvdUuid(vmName, "boot2docker.iso")
            exec.executeForOutput("unregistervm $vmName ${withDelete ? "--delete" : ""}")
            if (uuid) {
                removeDvd(uuid)
            }
            waitUntilNotExists(vmName)
        }
    }

    private boolean waitUntilNotExists(String vmName) {
        (1..20).find {
            if (!isVmPresent(vmName)) {
                return true
            } else {
                sleep(1000)
                return false
            }
        }
    }

    /**
     * checks if vm exists
     * @param vmName
     * @return
     */
    boolean isVmPresent(String vmName) {
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

    /**
     * returns uuid of dvd which lies inside given directory (or exact file)
     * @param d
     * @return
     */
    String getUuidOfDvdInDirectory(File d) {
        def dvds = exec.executeForOutput("list dvds")
        String path = d.absolutePath.replaceAll("\\/", "\\\\/")
        Matcher a = dvds =~ /UUID:\s*([a-f0-9-]+)\n.*\n.*\nLocation:\s+$path/
        if (a) {
            return a[0][1]
        } else {
            return null;
        }
    }
    /**
     *
     * Closes given dvd medium,
     * @param uuid
     */
    void removeDvd(String uuid) {
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

    /**
     * Registers vm from given file
     * @param file
     */
    void registervm(File file) {
        exec.executeForOutput("registervm ${file.absolutePath}")
    }

    /**
     * Powers off the machine
     * @param machineName
     */
    void poweroff(String machineName) {
        if (exec.executeForOutput("list runningvms").contains("\"$machineName\"")) {
            exec.executeForOutput("controlvm $machineName poweroff")
        }
    }
}
