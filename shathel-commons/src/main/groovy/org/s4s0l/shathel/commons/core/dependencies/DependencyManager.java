package org.s4s0l.shathel.commons.core.dependencies;

import org.s4s0l.shathel.commons.core.model.StackFileModel;
import org.s4s0l.shathel.commons.core.stack.*;
import org.s4s0l.shathel.commons.utils.IoUtils;

import java.io.File;
import java.util.List;

/**
 * @author Matcin Wielgus
 */
public class DependencyManager {

    public interface VersionOverrider{
        String overridenVersion(StackReference desc);
    }

    private final DependencyDownloader downloader;

    public DependencyManager(DependencyDownloader downloader) {
        this.downloader = downloader;
    }

    public StackTreeDescription downloadDependencies(File dependenciesDir, StackReference root, VersionOverrider overrider) {
        StackDescription desc = getStackDescription(dependenciesDir, root, desc1 -> desc1.getVersion());
        StackTreeDescription.Builder builder = StackTreeDescription.builder(desc);
        addDependencies(dependenciesDir, desc, builder, overrider);
        return builder.build();
    }

    private void addDependencies(File dependenciesDir, StackDescription parent, StackTreeDescription.Builder builder, VersionOverrider overrider) {
        List<StackReference> dependencies = parent.getDependencies();
        for (StackReference dependency : dependencies) {
            StackDescription depDesc = getStackDescription(dependenciesDir, dependency,overrider);
            builder.addNode(parent.getReference(), depDesc);
            addDependencies(dependenciesDir, depDesc, builder, overrider);
        }
    }

    private StackDescription getStackDescription(File dependenciesDir, StackReference ref,VersionOverrider overrider) {
        StackReference stackReference = new StackReference( ref.getGroup(),ref.getName(), overrider.overridenVersion(ref));

        File destFile = new File(dependenciesDir, (stackReference.getStackFileName()));
        if (!destFile.exists()) {
            downloader.download(stackReference, dependenciesDir);
        }
        if (!destFile.exists()) {
            throw new RuntimeException("File download failed??");
        }
        File destDirectory = new File(dependenciesDir, (stackReference.getStackDirecctoryName()));
        if (!destDirectory.exists()) {
            IoUtils.unZipIt(destFile, destDirectory);
        }
        File stackFile = new File(destDirectory, "shthl-stack.yml");
        if (!stackFile.exists()) {
            throw new RuntimeException("Stack package with no shthl-stack.yml file");
        }

        return new StackDescriptionImpl(StackFileModel.load(stackFile), new StackResources(destDirectory));
    }




}
