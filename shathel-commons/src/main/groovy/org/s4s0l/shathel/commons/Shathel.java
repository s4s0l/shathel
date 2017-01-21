package org.s4s0l.shathel.commons;

import com.google.common.collect.ImmutableMap;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.s4s0l.shathel.commons.core.Parameters;
import org.s4s0l.shathel.commons.core.Solution;
import org.s4s0l.shathel.commons.core.storage.Storage;
import org.s4s0l.shathel.commons.filestorage.FileStorage;
import org.s4s0l.shathel.commons.utils.ExtensionContext;
import org.s4s0l.shathel.commons.utils.TemplateUtils;

import java.io.File;

/**
 * @author Matcin Wielgus
 */
public class Shathel {
    private final Parameters params;
    private final ExtensionContext context;

    public Shathel(Parameters params) {
        this(params, DefaultExtensionContext.create(params));
    }

    public Shathel(Parameters params, ExtensionContext context) {
        this.params = params;
        this.context = context;
    }

    public void verify() {
        //todo check if all required software is installed
    }

    public Storage getStorage(File f) {
        FileStorage fileStorage = createFileStorage(f);
        fileStorage.verify();
        return fileStorage;
    }

    public Storage initStorage(File f) {
        try {
            FileStorage fileStorage = createFileStorage(f);
            File configuration = fileStorage.getConfiguration();
            if (configuration.exists()) {
                throw new RuntimeException("Configuration already exists!");
            }
            String solutionName = params.getParameter("shathel.solution.name")
                    .orElse(f.getName());
            String defaultConfig = getDefaultConfig(solutionName);
            ResourceGroovyMethods.setText(configuration, defaultConfig);
            return fileStorage;
        } catch (Exception e) {
            throw new RuntimeException("Unable to init storage", e);
        }
    }

    private FileStorage createFileStorage(File f) {
        if (new File(f, ".git").exists()) {
            throw new RuntimeException("git storage unsupported?");
        }
        return new FileStorage(f, params);
    }

    private String getDefaultConfig(String projectName) {
        return TemplateUtils.generateTemplate(
                this.getClass().getResource("/default-shathel-solution.yml"),
                ImmutableMap.of("SOLLUTION_NAME", projectName));
    }

    public Solution getSolution(Storage storage) {
        storage.verify();
        return new Solution(context, params, storage);
    }
}
