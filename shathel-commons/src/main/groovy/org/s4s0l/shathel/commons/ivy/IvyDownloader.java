package org.s4s0l.shathel.commons.ivy;

import com.google.common.base.Functions;
import org.apache.commons.io.FileUtils;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.DefaultDependencyArtifactDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.core.settings.IvyVariableContainerImpl;
import org.apache.ivy.core.settings.XmlSettingsParser;
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorWriter;
import org.apache.ivy.plugins.resolver.ChainResolver;
import org.apache.ivy.plugins.resolver.URLResolver;
import org.s4s0l.shathel.commons.core.Parameters;
import org.s4s0l.shathel.commons.core.dependencies.DependencyDownloader;
import org.s4s0l.shathel.commons.core.dependencies.ReferenceResolver;
import org.s4s0l.shathel.commons.core.dependencies.StackLocator;
import org.s4s0l.shathel.commons.core.stack.StackReference;
import org.s4s0l.shathel.commons.utils.IoUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Marcin Wielgus
 */
public class IvyDownloader implements DependencyDownloader {
    public static final String SHATHEL_IVY_REPOS_ID = "shathel.ivy.reposId";
    public static final String SHATHEL_IVY_REPOS = "shathel.ivy.repos";
    public static final String DEFAULT_REPOS = "http://repo1.maven.org/maven2/,https://dl.bintray.com/sasol-oss/maven/";
    public static final String PATTERN_SUFFIX = "[organisation]/[module]/[revision]/[artifact]-[revision](-[classifier]).[ext]";
    public static final String SHATHEL_IVY_SETTINGS = "shathel.ivy.settings";
    final Parameters parameters;

    public IvyDownloader(Parameters parameters) {
        this.parameters = parameters;
    }

    public static String getShathelVersion() {
        Package pkg = IvyDownloader.class.getPackage();
        String version = null;
        if (pkg != null) {
            version = pkg.getImplementationVersion();
        }
        return (version != null ? version : "Unknown Version");
    }


    @Override
    public Optional<File> download(StackLocator locator, File directory, boolean forceful) {
        Optional<StackReference> reference = getReference(locator);
        if (!reference.isPresent()) {
            return Optional.empty();
        }

        File destDirectory = new File(directory, "ivy/" + reference.get().getStackDirecctoryName());
        if (!forceful && destDirectory.exists()) {
            return Optional.of(destDirectory);
        }

        try {
            return downloadZipFile(reference.get(), directory, forceful);
        } catch (Exception e) {
            throw new RuntimeException("Unable to download", e);
        }
    }

    private Optional<File> downloadZipFile(StackReference reference, File directory, boolean force) throws Exception {

        String groupId = reference.getGroup();
        String artifactId = reference.getName();
        String version = reference.getVersion();
        IvySettings ivySettings = getIvySettings();

        //creates an Ivy instance with settings
        Ivy ivy = Ivy.newInstance(ivySettings);


        DefaultModuleDescriptor md = getDefaultModuleDescriptor(groupId, artifactId, version, force);


        //creates an ivy configuration file
        File ivyFile = new File(directory, reference.getStackDirecctoryName() + "-ivy.xml");
        XmlModuleDescriptorWriter.write(md, ivyFile);

        String[] confs = new String[]{"default"};
        ResolveOptions resolveOptions = new ResolveOptions().setConfs(confs);

        //init resolve report
        ResolveReport report = ivy.resolve(ivyFile.toURL(), resolveOptions);
        if (report.getAllArtifactsReports().length == 0) {
            return Optional.empty();
        }
        //so you can get the jar library
        File jarArtifactFile = report.getAllArtifactsReports()[0].getLocalFile();
        File destDir = new File(directory, "ivy/" + reference.getStackDirecctoryName());
        if (destDir.exists()) {
            FileUtils.deleteDirectory(destDir);
        }
        IoUtils.unZipIt(jarArtifactFile, destDir);
        return Optional.of(destDir);
    }

    private DefaultModuleDescriptor getDefaultModuleDescriptor(String groupId, String artifactId, String version, boolean force) {
        DefaultModuleDescriptor md =
                DefaultModuleDescriptor.newDefaultInstance(ModuleRevisionId.newInstance(groupId,
                        artifactId + "-caller", "working"));
        md.addExtraAttributeNamespace("m", "http://ant.apache.org/ivy/extra");
        Map extraAttrs = new HashMap();
        extraAttrs.put("m:classifier", "shathel");

        DefaultDependencyDescriptor dd = new DefaultDependencyDescriptor(md,
                ModuleRevisionId.newInstance(groupId, artifactId, version), force, false, true);
        dd.addDependencyConfiguration("default", "default");
        dd.addDependencyArtifact("default", new DefaultDependencyArtifactDescriptor(dd, artifactId, "type", "zip", null, extraAttrs));
        md.addDependency(dd);
        return md;
    }

    private IvySettings getIvySettings() {
        return parameters.getParameter(SHATHEL_IVY_SETTINGS).map(it -> {
            File x = new File(it);
            try {
                IvySettings ivy = createEmptyIvySettings();
                XmlSettingsParser xmlSettingsParser = new XmlSettingsParser(ivy);
                xmlSettingsParser.parse(x.toURL());
                return ivy;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).orElseGet(() -> {
            IvySettings ivySettings = createEmptyIvySettings();
            ChainResolver cresolver = new ChainResolver();
            cresolver.setName(parameters.getParameter(SHATHEL_IVY_REPOS_ID).orElse("default"));
            String repos = parameters.getParameter(SHATHEL_IVY_REPOS)
                    .orElse(DEFAULT_REPOS);
            String[] split = repos.split(",");
            for (int j = 0; j < split.length; j++) {
                String repo = split[j].trim();
                String id = "repo-" + j;
                URLResolver resolver = getUrlResolver(id, repo);
                cresolver.add(resolver);
            }
            ivySettings.addResolver(cresolver);
            ivySettings.setDefaultResolver(cresolver.getName());
            return ivySettings;
        });


    }

    private IvySettings createEmptyIvySettings() {
        Map<String, String> ivyParams = parameters.getAllParameters().stream()
                .filter(it -> it.contains("ivy"))
                .collect(Collectors.toMap(Functions.identity(), it -> parameters.getParameter(it).get()));

        IvyVariableContainerImpl variableContainer = new IvyVariableContainerImpl(ivyParams);
        return new IvySettings(variableContainer);
    }

    private URLResolver getUrlResolver(String repoId, String pattern) {
        pattern = pattern.endsWith("/") ? pattern + "/" : pattern;
        URLResolver resolver = new URLResolver();
        resolver.setM2compatible(true);
        resolver.setName(repoId);
        resolver.addArtifactPattern(
                pattern + PATTERN_SUFFIX);
        return resolver;
    }

    private Optional<StackReference> getReference(StackLocator locator) {
        return new ReferenceResolver(parameters).resolve(locator);
    }


}
