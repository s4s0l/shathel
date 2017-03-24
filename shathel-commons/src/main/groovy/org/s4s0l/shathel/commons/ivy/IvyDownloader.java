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
import org.apache.ivy.util.AbstractMessageLogger;
import org.apache.ivy.util.DefaultMessageLogger;
import org.apache.ivy.util.Message;
import org.s4s0l.shathel.commons.core.Parameters;
import org.s4s0l.shathel.commons.core.dependencies.DependencyDownloader;
import org.s4s0l.shathel.commons.core.dependencies.ReferenceResolver;
import org.s4s0l.shathel.commons.core.dependencies.StackLocator;
import org.s4s0l.shathel.commons.core.stack.StackReference;
import org.s4s0l.shathel.commons.utils.IoUtils;
import org.s4s0l.shathel.commons.utils.Utils;
import org.slf4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Marcin Wielgus
 */
public class IvyDownloader implements DependencyDownloader {
    private static final Logger LOGGER = getLogger(IvyDownloader.class);
    public static final String SHATHEL_IVY_DEFAULT_VERSION = "shathel.solution.ivy_default_version";
    public static final String SHATHEL_IVY_DEFAULT_GROUP = "shathel.solution_ivy_default_group";
    public static final String SHATHEL_IVY_SETTINGS = "shathel.solution.ivy_settings";

    public static final String SHATHEL_IVY_REPOS_ID = "shathel.solution.ivy_repo_id";
    public static final String SHATHEL_IVY_REPOS = "shathel.solution.ivy_repos";
    public static final String DEFAULT_GROUP = "org.s4s0l.shathel";
    public static final String DEFAULT_REPOS = "http://repo1.maven.org/maven2/,https://dl.bintray.com/sasol-oss/maven/";
    public static final String PATTERN_SUFFIX = "[organisation]/[module]/[revision]/[artifact]-[revision](-[classifier]).[ext]";
    final Parameters parameters;

    public IvyDownloader(Parameters parameters) {
        this.parameters = parameters;
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

        //todo: replace it with sth that does not do println...
        Message.setDefaultLogger(new IvyMessageLogger());
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
        String group = parameters.getParameter(SHATHEL_IVY_DEFAULT_GROUP).orElse(DEFAULT_GROUP);
        String version = parameters.getParameter(SHATHEL_IVY_DEFAULT_VERSION).orElseGet(() -> Utils.getShathelVersion());
        return new ReferenceResolver(group, version).resolve(locator);
    }


    private static class IvyMessageLogger extends AbstractMessageLogger {
        private static final Logger LOGGER = getLogger(IvyMessageLogger.class);

        @Override
        protected void doProgress() {
            LOGGER.debug("Ivy is working....");
        }

        @Override
        protected void doEndProgress(String msg) {
            LOGGER.debug("Finished: {}", msg);
        }

        @Override
        public void log(String msg, int level) {
            LOGGER.debug(msg);
        }

        @Override
        public void rawlog(String msg, int level) {
            log(msg, level);
        }
    }
}
