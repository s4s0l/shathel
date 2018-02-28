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

    public StackTreeDescription downloadDependencies(List<StackLocator> rootLocators,
                                                     StackIntrospectionProvider.StackIntrospections stackIntrospections) {

        StackTreeDescription.Builder builder = StackTreeDescription.builder(stackIntrospections);
        for (StackLocator rootLocator : rootLocators) {
            StackDescription desc = getStackDescription(dependenciesDir, rootLocator);
            builder.addRootNode(desc, true);
            addDependencies(desc, builder, stackIntrospections);
        }

        stackIntrospections.getStacks().stream().map(StackIntrospection::getReference)
                .map(x -> getStackDescription(dependenciesDir, x, StackReference::getVersion))
                .forEach(x -> {
                    builder.addRootNode(x, false);
                    addDependencies(x, builder, stackIntrospections);
                });

        return builder.build();
    }

    private void addDependencies(StackDescription parent, StackTreeDescription.Builder builder,
                                 StackIntrospectionProvider.StackIntrospections stackIntrospections) {
        List<StackDependency> dependencies = parent.getDependencies();
        for (StackDependency dependency : dependencies) {
            StackDescription depDesc = getStackDescription(dependenciesDir, dependency.getStackReference(), getOverrider(stackIntrospections));
            builder.addNode(depDesc);
            addDependencies(depDesc, builder, stackIntrospections);
        }
    }

    private StackDescription getStackDescription(File dependenciesDir, StackReference ref, VersionOverrider overrider) {
        StackReference stackReference = new StackReference(ref.getGroup(), ref.getName(), overrider.overridenVersion(ref));
        return getStackDescription(dependenciesDir, new StackLocator(stackReference));
    }

    private StackDescription getStackDescription(File dependenciesDir, StackLocator ref) {
        Optional<File> first = downloader.getDownloaders()
                .map(it -> it.download(ref, dependenciesDir, forcefull))
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
