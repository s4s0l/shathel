package org.s4s0l.shathel.commons.core.dependencies;

import org.s4s0l.shathel.commons.core.SolutionDescription;
import org.s4s0l.shathel.commons.core.environment.StackIntrospection;
import org.s4s0l.shathel.commons.core.environment.StackIntrospectionProvider;
import org.s4s0l.shathel.commons.core.model.StackFileModel;
import org.s4s0l.shathel.commons.core.stack.*;
import org.s4s0l.shathel.commons.utils.VersionComparator;

import java.io.File;
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
        return stackIntrospections.getStacks().stream().map(StackIntrospection::getReference)
                .filter(x -> !tree.contains(x))
                .map(x -> getStackDescription(dependenciesDir, x, StackReference::getVersion))
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
                .map(it -> it.download( ref, dependenciesDir, forcefull))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
        return first
                .map(it -> {
                    File stackFile = new File(it, "shthl-stack.yml");
                    StackFileModel load = StackFileModel.load(stackFile);
                    StackReference stackReference = new StackReference(load.getGav());
                    return new StackDescriptionImpl(load, new StackResources(it), solutionDescription.getSolutionStackEnvs(stackReference));
                }).orElseThrow(() -> new RuntimeException("Unable to locate stack " + ref.getLocation()));
    }


}
