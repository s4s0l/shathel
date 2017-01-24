package org.s4s0l.shathel.commons.machine

import org.apache.commons.io.FileUtils
import org.s4s0l.shathel.commons.docker.DockerMachineWrapper
import org.s4s0l.shathel.commons.docker.VBoxManageWrapper
import org.s4s0l.shathel.commons.utils.IoUtils

import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission

/**
 * @author Matcin Wielgus
 */
class MachineSettingsImporterExporter {

    private static final List<String> ignoredElements = ["Logs"]
    private static final String DIRECTORY_PLACEHOLDER = "SHATHEL_TMP_BASE_DIR"

    private final File tmpDirToUse;



    MachineSettingsImporterExporter(File tmpDirToUse) {
        this.tmpDirToUse = tmpDirToUse
    }



    void saveSettings(File dockerMachineSettingsFolder, OutputStream outputStream) {
        def wrkDir = new File(tmpDirToUse, dockerMachineSettingsFolder.getName());
        try {

            new File(dockerMachineSettingsFolder, "machines").listFiles()
                    .findAll { it.isDirectory() }
                    .each { beforeSave(it) }

            FileUtils.deleteDirectory(wrkDir);
            FileUtils.copyDirectory(dockerMachineSettingsFolder, wrkDir);


            new File(wrkDir, "machines").listFiles()
                    .findAll { it.isDirectory() }
                    .each { it ->
                new File(it, "config.json").with {
                    text = text.replaceAll(dockerMachineSettingsFolder.absolutePath, MachineSettingsImporterExporter.DIRECTORY_PLACEHOLDER)
                }
                new File(it, "${it.name}/${it.name}.vbox").with {
                    text = text.replaceAll(dockerMachineSettingsFolder.absolutePath, MachineSettingsImporterExporter.DIRECTORY_PLACEHOLDER)
                }
                new File(it, "${it.name}/${it.name}.vbox-prev").with {
                    text = text.replaceAll(dockerMachineSettingsFolder.absolutePath, MachineSettingsImporterExporter.DIRECTORY_PLACEHOLDER)
                }
            }
            IoUtils.zipIt(wrkDir, outputStream, { File f ->
                !ignoredElements.contains(f.getName())
            })
            new File(dockerMachineSettingsFolder, "machines").listFiles()
                    .findAll { it.isDirectory() }
                    .each { afterSave(it) }
        }finally {
            FileUtils.deleteDirectory(wrkDir);
        }
    }


    void loadSettings(InputStream is, File destinationDirectory) {
        IoUtils.unZipIt(is, destinationDirectory);
        new File(destinationDirectory, "machines").listFiles()
                .findAll { it.isDirectory() }
                .collect { it ->
            new File(it, "config.json").with {
                text = text.replaceAll(MachineSettingsImporterExporter.DIRECTORY_PLACEHOLDER, destinationDirectory.absolutePath)
            }
            new File(it, "${it.name}/${it.name}.vbox").with {
                text = text.replaceAll(MachineSettingsImporterExporter.DIRECTORY_PLACEHOLDER, destinationDirectory.absolutePath)
            }
            Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
            //add owners permission
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_WRITE);
            Files.setPosixFilePermissions(new File(it, "id_rsa").toPath(), perms)
            it
        }.each {
            File x = it
            afterLoad(x)
        }
    }
    protected void afterLoad(File restoredMachineDir) {

    }


    protected void beforeSave(File vmFolder) {

    }

    protected void afterSave(File vmFolder) {

    }
}
