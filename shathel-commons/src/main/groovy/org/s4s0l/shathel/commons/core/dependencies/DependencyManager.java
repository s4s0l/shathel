package org.s4s0l.shathel.commons.core.dependencies;

import org.apache.commons.io.FileUtils;
import org.s4s0l.shathel.commons.core.model.StackFileModel;
import org.s4s0l.shathel.commons.core.stack.*;
import org.s4s0l.shathel.commons.utils.IoUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Matcin Wielgus
 */
public class DependencyManager {

    public interface VersionOverrider {
        String overridenVersion(StackReference desc);
    }

    private final File dependenciesDir;
    private final DependencyDownloader downloader;
    private final VersionOverrider overrider;
    private final boolean forcefull;


    public DependencyManager(File dependenciesDir, DependencyDownloader downloader, VersionOverrider overrider, boolean forcefull) {
        this.dependenciesDir = dependenciesDir;
        this.downloader = downloader;
        this.overrider = overrider;
        this.forcefull = forcefull;
    }

    public StackTreeDescription downloadDependencies(StackReference root) {
        StackDescription desc = getStackDescription(dependenciesDir, root, desc1 -> desc1.getVersion());
        StackTreeDescription.Builder builder = StackTreeDescription.builder(desc);
        addDependencies(desc, builder);
        return builder.build();
    }

    private void addDependencies(StackDescription parent, StackTreeDescription.Builder builder) {
        List<StackReference> dependencies = parent.getDependencies();
        for (StackReference dependency : dependencies) {
            StackDescription depDesc = getStackDescription(dependenciesDir, dependency, overrider);
            builder.addNode(parent.getReference(), depDesc);
            addDependencies(depDesc, builder);
        }
    }

    private StackDescription getStackDescription(File dependenciesDir, StackReference ref, VersionOverrider overrider) {
        StackReference stackReference = new StackReference(ref.getGroup(), ref.getName(), overrider.overridenVersion(ref));

        File destFile = new File(dependenciesDir, (stackReference.getStackFileName()));
        boolean downloaded = false;
        System.out.println("FOOOOOORCE" + forcefull);
        if (forcefull || !destFile.exists()) {
            downloader.download(stackReference, dependenciesDir);
            downloaded = true;
        }
        if (!destFile.exists()) {
            throw new RuntimeException("File download failed??");
        }
        File destDirectory = new File(dependenciesDir, (stackReference.getStackDirecctoryName()));
        if (downloaded || forcefull || !destDirectory.exists()) {
            if (destDirectory.exists()) {
                try {
                    FileUtils.deleteDirectory(destDirectory);
                } catch (IOException e) {
                    throw new RuntimeException("Unable to delete directory " + destDirectory.getAbsolutePath(), e);
                }
            }
            IoUtils.unZipIt(destFile, destDirectory);

        }
        File stackFile = new File(destDirectory, "shthl-stack.yml");
        if (!stackFile.exists()) {
            throw new RuntimeException("Stack package with no shthl-stack.yml file");
        }

        return new StackDescriptionImpl(StackFileModel.load(stackFile), new StackResources(destDirectory));
    }


}
