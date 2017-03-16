package org.s4s0l.shathel.commons.core.dependencies;

import org.apache.commons.io.FileUtils;
import org.s4s0l.shathel.commons.core.SolutionDescription;
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionProvider;
import org.s4s0l.shathel.commons.core.model.StackFileModel;
import org.s4s0l.shathel.commons.core.stack.*;
import org.s4s0l.shathel.commons.utils.IoUtils;
import org.s4s0l.shathel.commons.utils.VersionComparator;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Marcin Wielgus
 */
public class DependencyManager {

    public interface VersionOverrider {
        String overridenVersion(StackReference desc);
    }

    private final File dependenciesDir;
    private final DependencyDownloaderRegistry downloader;
    private final SolutionDescription solutionDescription;
    private final boolean forcefull;

    public DependencyManager(File dependenciesDir, DependencyDownloaderRegistry downloader,
                             SolutionDescription solutionDescription, boolean forcefull) {
        this.dependenciesDir = dependenciesDir;
        this.downloader = downloader;
        this.solutionDescription = solutionDescription;
        this.forcefull = forcefull;

    }

    private VersionOverrider getOverrider(StackIntrospectionProvider.StackIntrospections stackIntrospections) {
        return desc ->
                stackIntrospections.getIntrospection(desc)
                        .filter(x -> new VersionComparator().compare(x.getReference().getVersion(), desc.getVersion()) > 0)
                        .map(x -> x.getReference().getVersion())
                        .orElse(desc.getVersion());
    }

    public StackTreeDescription downloadDependencies(StackLocator root, StackIntrospectionProvider.StackIntrospections stackIntrospections, boolean withOptional) {
        StackDescription desc = getStackDescription(dependenciesDir, root);
        StackTreeDescription.Builder builder = StackTreeDescription.builder(desc);
        addDependencies(desc, builder, stackIntrospections, withOptional);
        return builder.build();
    }

    public List<StackDescription> getSidekicks(StackTreeDescription tree, StackIntrospectionProvider.StackIntrospections stackIntrospections) {
        return stackIntrospections.getStacks().stream().map(x -> x.getReference())
                .filter(x -> !tree.contains(x))
                .map(x -> getStackDescription(dependenciesDir, x, desc1 -> desc1.getVersion()))
                .collect(Collectors.toList());
    }

    private void addDependencies(StackDescription parent, StackTreeDescription.Builder builder,
                                 StackIntrospectionProvider.StackIntrospections stackIntrospections, boolean withOptional) {
        List<StackDependency> dependencies = parent.getDependencies();
        for (StackDependency dependency : dependencies) {
            if (withOptional || !dependency.isOptional()) {
                StackDescription depDesc = getStackDescription(dependenciesDir, dependency.getStackReference(), getOverrider(stackIntrospections));
                builder.addNode(parent.getReference(), depDesc);
                addDependencies(depDesc, builder, stackIntrospections, withOptional);
            }
        }
    }

    private StackDescription getStackDescription(File dependenciesDir, StackReference ref, VersionOverrider overrider) {
        StackReference stackReference = new StackReference(ref.getGroup(), ref.getName(), overrider.overridenVersion(ref));
        return getStackDescription(dependenciesDir, new StackLocator(stackReference));
    }

    private StackDescription getStackDescription(File dependenciesDir, StackLocator ref) {
        Optional<File> first = downloader.getDownloaders()
                .map(it -> it.download(ref, dependenciesDir, forcefull))
                .filter(it -> it.isPresent())
                .map(it -> it.get())
                .findFirst();
        return first
                .map(it -> {
                    File stackFile = new File(it, "shthl-stack.yml");
                    StackFileModel load = StackFileModel.load(stackFile);
                    StackReference stackReference = new StackReference(load.getGav());
                    return new StackDescriptionImpl(load, new StackResources(it), solutionDescription.getSolutionStackDescription(stackReference));
                }).orElseThrow(() -> new RuntimeException("Unable to locate stack " + ref.getLocation()));
    }


}
