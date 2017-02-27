package org.s4s0l.shathel.commons.core.dependencies;

import org.apache.commons.io.FileUtils;
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionProvider;
import org.s4s0l.shathel.commons.core.model.StackFileModel;
import org.s4s0l.shathel.commons.core.stack.*;
import org.s4s0l.shathel.commons.utils.IoUtils;
import org.s4s0l.shathel.commons.utils.VersionComparator;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Matcin Wielgus
 */
public class DependencyManager {

    public interface VersionOverrider {
        String overridenVersion(StackReference desc);
    }

    private final File dependenciesDir;
    private final DependencyDownloader downloader;
    private final StackIntrospectionProvider introspectionProvider;

    private final boolean forcefull;

    public DependencyManager(File dependenciesDir, DependencyDownloader downloader, StackIntrospectionProvider introspectionProvider, boolean forcefull) {
        this.dependenciesDir = dependenciesDir;
        this.downloader = downloader;
        this.introspectionProvider = introspectionProvider;
        this.forcefull = forcefull;

    }

    private VersionOverrider getOverrider() {
        return desc ->
                introspectionProvider.getIntrospection(desc)
                        .filter(x -> new VersionComparator().compare(x.getReference().getVersion(), desc.getVersion()) > 0)
                        .map(x -> x.getReference().getVersion())
                        .orElse(desc.getVersion());
    }

    public StackTreeDescription downloadDependencies(StackReference root) {
        StackDescription desc = getStackDescription(dependenciesDir, root, desc1 -> desc1.getVersion());
        StackTreeDescription.Builder builder = StackTreeDescription.builder(desc);
        addDependencies(desc, builder);
        return builder.build();
    }

    public List<StackDescription> getSidekicks(StackTreeDescription tree) {
        return introspectionProvider.getAllStacks().stream().map(x -> x.getReference())
                .filter(x -> !tree.contains(x))
                .map(x -> getStackDescription(dependenciesDir, x, desc1 -> desc1.getVersion()))
                .collect(Collectors.toList());
    }

    private void addDependencies(StackDescription parent, StackTreeDescription.Builder builder) {
        List<StackReference> dependencies = parent.getDependencies();
        for (StackReference dependency : dependencies) {
            StackDescription depDesc = getStackDescription(dependenciesDir, dependency, getOverrider());
            builder.addNode(parent.getReference(), depDesc);
            addDependencies(depDesc, builder);
        }
    }

    private StackDescription getStackDescription(File dependenciesDir, StackReference ref, VersionOverrider overrider) {
        StackReference stackReference = new StackReference(ref.getGroup(), ref.getName(), overrider.overridenVersion(ref));
        File destFile = new File(dependenciesDir, (stackReference.getStackFileName()));
        boolean downloaded = false;
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
