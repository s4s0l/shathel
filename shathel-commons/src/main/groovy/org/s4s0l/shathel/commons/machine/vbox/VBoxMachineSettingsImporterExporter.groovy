package org.s4s0l.shathel.commons.machine.vbox

import org.s4s0l.shathel.commons.docker.DockerMachineWrapper
import org.s4s0l.shathel.commons.docker.VBoxManageWrapper
import org.s4s0l.shathel.commons.machine.MachineSettingsImporterExporter

/**
 * @author Matcin Wielgus
 */
class VBoxMachineSettingsImporterExporter extends MachineSettingsImporterExporter {

    VBoxMachineSettingsImporterExporter(File tmpDirToUse) {
        super(tmpDirToUse)
    }
    private VBoxManageWrapper vbox = new VBoxManageWrapper()

    @Override
    protected void afterLoad(File restoredMachineDir) {
        if (vbox.isVmPresent(restoredMachineDir.name)) {
            new DockerMachineWrapper(restoredMachineDir.getParentFile().getParentFile()).stop(restoredMachineDir.name)
            vbox.removeVm(restoredMachineDir.name, false)
        }
        vbox.registervm(new File(restoredMachineDir, "${restoredMachineDir.name}/${restoredMachineDir.name}.vbox"))
    }

    @Override
    protected void beforeSave(File vmFolder) {
        new DockerMachineWrapper(vmFolder.getParentFile().getParentFile()).stop(vmFolder.name)
        vbox.removeVm(vmFolder.name, false)
    }

    @Override
    protected void afterSave(File vmFolder) {
        vbox.registervm(new File(vmFolder, "${vmFolder.name}/${vmFolder.name}.vbox"))
        new DockerMachineWrapper(vmFolder.getParentFile().getParentFile()).start(vmFolder.name)
    }
}
