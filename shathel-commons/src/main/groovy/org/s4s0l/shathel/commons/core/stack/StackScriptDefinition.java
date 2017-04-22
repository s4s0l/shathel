package org.s4s0l.shathel.commons.core.stack;

import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.s4s0l.shathel.commons.scripts.TypedScript;
import org.slf4j.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Marcin Wielgus
 */
public class StackScriptDefinition implements TypedScript {
    private static final Logger LOGGER = getLogger(StackScriptDefinition.class);
    private final StackDescription origin;
    private final String category;
    private final String name;
    private final String inline;
    private final String type;

    @Override
    public String getScriptName() {
        return getType() + ":" + origin.getReference().getGav() + "/" + getName();
    }

    @Override
    public File getBaseDirectory() {
        return new File(origin.getStackResources().getStackDirectory(), category);
    }

    public StackScriptDefinition(StackDescription origin, String category, String name, String inline, String type) {
        this.origin = origin;
        this.category = category;
        this.name = name;
        this.inline = inline;
        this.type = type;
    }

    public StackDescription getOrigin() {
        return origin;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getScriptContents() {
        String script;
        try {
            script = inline != null ? inline :
                    ResourceGroovyMethods.getText(getScriptFileLocation().get());
        } catch (IOException e) {
            throw new RuntimeException("Unable to find Enricher script", e);
        }
        return script;
    }

    @Override
    public Optional<File> getScriptFileLocation() {
        if (inline != null) {
            return Optional.empty();
        }
        Optional<File> ret = Stream.of(
                new File(getBaseDirectory(), getName() + "." + getType()),
                new File(getBaseDirectory(), getName()))
                .filter(it -> it.exists())
                .findFirst();
        if (ret.isPresent()) {
            return ret;
        }
        File[] list = getBaseDirectory().listFiles((dir, name) -> {
            int dotIdx = name.lastIndexOf(".");
            return dotIdx > 0 && name.substring(0, dotIdx).equals(getName());
        });
        if (list.length == 1) {
            return Optional.of(list[0]);
        }
        if (list.length > 1) {
            LOGGER.warn("Multiple files named {} found in directory in directory {}. Provide extension in script name?", getName(), getBaseDirectory().getAbsolutePath());
        }
        if (list.length != 1) {
            LOGGER.warn("Unable to found script file named {} in directory {}", getName(), getBaseDirectory().getAbsolutePath());
        }
        return Optional.empty();

    }
}
