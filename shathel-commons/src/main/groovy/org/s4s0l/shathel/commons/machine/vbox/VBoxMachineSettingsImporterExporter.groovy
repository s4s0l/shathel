package org.s4s0l.shathel.commons.machine.vbox

import org.s4s0l.shathel.commons.docker.DockerMachineWrapper
import org.s4s0l.shathel.commons.docker.VBoxManageWrapper
import org.s4s0l.shathel.commons.machine.MachineSettingsImporterExporter

/**
 * @author Marcin Wielgus
 */
@Deprecated
class VBoxMachineSettingsImporterExporter extends MachineSettingsImporterExporter {
    private final DockerMachineWrapper machineWrapper;

    VBoxMachineSettingsImporterExporter(File tmpDirToUse,DockerMachineWrapper machineWrapper) {
        super(tmpDirToUse)
        this.machineWrapper = machineWrapper;
    }
    private VBoxManageWrapper vbox = new VBoxManageWrapper()

    @Override
    protected void afterLoad(File restoredMachineDir) {
        vbox.registervm(new File(restoredMachineDir, "${restoredMachineDir.name}/${restoredMachineDir.name}.vbox"))
    }

    @Override
    protected void beforeLoad(File restoredMachineDir) {
        if (vbox.isVmPresent(restoredMachineDir.name)) {
            machineWrapper.stop(restoredMachineDir.name)
            vbox.removeVm(restoredMachineDir.name, false)
        }
    }

    @Override
    protected void beforeSave(File vmFolder) {
        machineWrapper.stop(vmFolder.name)
        vbox.removeVm(vmFolder.name, false)
    }

    @Override
    protected void afterSave(File vmFolder) {
        vbox.registervm(new File(vmFolder, "${vmFolder.name}/${vmFolder.name}.vbox"))
        machineWrapper.start(vmFolder.name)
    }
}
