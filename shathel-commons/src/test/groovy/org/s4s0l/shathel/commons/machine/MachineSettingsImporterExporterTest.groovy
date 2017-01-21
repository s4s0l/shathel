package org.s4s0l.shathel.commons.machine

import org.apache.commons.io.FileUtils
import org.s4s0l.shathel.commons.docker.DockerMachineWrapper
import org.s4s0l.shathel.commons.docker.VBoxManageWrapper
import spock.lang.IgnoreIf
import spock.lang.Specification

/**
 * @author Matcin Wielgus
 */
class MachineSettingsImporterExporterTest extends Specification {

    def setupSpec() {
        cleanupSpec()
    }

    // Run after all the tests, even after failures:
    def cleanupSpec() {
        new VBoxManageWrapper().with {
            poweroff("machineMovingTest")
            unregister("machineMovingTest", true)
        }
        FileUtils.deleteDirectory(new File("build/machineStorageTest"));
    }

    @IgnoreIf({ Boolean.valueOf(env['IGNORE_MACHINE_TESTS']) })
    def "Saving and restoring to different location should not break VBox nor Docker-Machine"() {
        given:
        def srcDir = new File("build/machineStorageTest/sourceCfg");
        def targetDir = new File("build/machineStorageTest/targetCfg");
        srcDir.mkdirs()


        new DockerMachineWrapper(srcDir).create("-d virtualbox machineMovingTest")
        MachineSettingsImporterExporter ie = new MachineSettingsImporterExporter(new File("build/machineStorageTest/tmp"))
        File saved = new File("build/machineStorageTest/saved.zip")
        saved.createNewFile()

        when:
        ie.saveSettings(srcDir, new FileOutputStream(saved))
        ie.loadSettings(new FileInputStream(saved), targetDir)


        then:
        new DockerMachineWrapper(targetDir).start("machineMovingTest")
                .contains("may have new IP addresses")

    }
}
