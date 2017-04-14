package org.s4s0l.shathel.commons;

import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.s4s0l.shathel.commons.core.Parameters;
import org.s4s0l.shathel.commons.core.Solution;
import org.s4s0l.shathel.commons.core.model.DefaultSolutiuonFileProvider;
import org.s4s0l.shathel.commons.core.storage.Storage;
import org.s4s0l.shathel.commons.filestorage.FileStorage;
import org.s4s0l.shathel.commons.utils.ExtensionContext;

import java.io.File;
import java.util.Set;

/**
 * @author Marcin Wielgus
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


    public Storage initStorage(File f, boolean failIfExists) {
        try {
            FileStorage fileStorage = createFileStorage(f);
            File configuration = fileStorage.getConfiguration();
            if (configuration.exists()) {
                if (failIfExists)
                    throw new RuntimeException("Configuration already present! ( " + configuration.getAbsolutePath());
                return getStorage(f);
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

    private String getDefaultConfig(String solutionName) {
        return new DefaultSolutiuonFileProvider().getDefaultConfig(solutionName, params);
    }

    private FileStorage createFileStorage(File f) {
        if (new File(f, ".git").exists()) {
            throw new RuntimeException("git storage unsupported?");
        }
        return new FileStorage(f);
    }



    public Solution getSolution(Storage storage) {
        storage.verify();
        return new Solution(context, params, storage);
    }
}
