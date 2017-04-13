package org.s4s0l.shathel.commons.core.stack;

import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.s4s0l.shathel.commons.scripts.TypedScript;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * @author Marcin Wielgus
 */
public class StackScriptDefinition implements TypedScript {
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
        return Optional.of(new File(getBaseDirectory(), getName() + "." + getType()));
    }
}
