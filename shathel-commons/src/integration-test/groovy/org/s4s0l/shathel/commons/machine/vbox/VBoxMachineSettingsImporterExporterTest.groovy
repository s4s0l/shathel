package org.s4s0l.shathel.commons.machine.vbox

import org.apache.commons.io.FileUtils
import org.s4s0l.shathel.commons.docker.DockerMachineWrapper
import org.s4s0l.shathel.commons.docker.VBoxManageWrapper
import org.s4s0l.shathel.commons.machine.MachineSettingsImporterExporter
import spock.lang.IgnoreIf
import spock.lang.Specification

/**
 * @author Matcin Wielgus
 */
class VBoxMachineSettingsImporterExporterTest extends Specification {

    def setupSpec() {
        cleanOnEnd()
        FileUtils.deleteDirectory(new File("${getRootDir()}"));
    }

    String getRootDir(){
        return "build/Test${getClass().getSimpleName()}"
    }
    
    // Run after all the tests, even after failures:
    def cleanOnEnd() {
        new VBoxManageWrapper().with {
            poweroff("machineMovingTest")
            removeVm("machineMovingTest", true)
        }
        true
    }

    @IgnoreIf({ Boolean.valueOf(env['IGNORE_MACHINE_TESTS']) })
    def "Saving and restoring to different location should not break VBox nor Docker-Machine"() {
        given:
        def srcDir = new File("${getRootDir()}/sourceCfg");
        def targetDir = new File("${getRootDir()}/targetCfg");
        srcDir.mkdirs()


        new DockerMachineWrapper(srcDir).create("-d virtualbox machineMovingTest")
        MachineSettingsImporterExporter ie = new VBoxMachineSettingsImporterExporter(new File("${getRootDir()}/tmp"))
        File saved = new File("${getRootDir()}/saved.zip")
        saved.createNewFile()

        when:
        ie.saveSettings(srcDir, new FileOutputStream(saved))
        ie.loadSettings(new FileInputStream(saved), targetDir)


        then:
        new DockerMachineWrapper(targetDir).start("machineMovingTest")



        cleanOnEnd()

    }
}
