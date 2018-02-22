package org.s4s0l.shathel.commons.core.environment;

import org.apache.commons.lang.NotImplementedException;
import org.s4s0l.shathel.commons.scripts.NamedExecutable;
import org.s4s0l.shathel.commons.scripts.TypedScript;

import java.io.File;
import java.util.Map;
import java.util.Optional;

/**
 * @author Marcin Wielgus
 */
public abstract class EnricherExecutable implements NamedExecutable {

    @Override
    public final void execute(Map<String, Object> context) {
        execute(new EnricherExecutableParams(context));
    }


    protected void execute(EnricherExecutableParams enricherExecutableParams) {
        throw new NotImplementedException("wtf??");
    }

    @Override
    public TypedScript getScript() {
        return new TypedScript() {
            @Override
            public String getType() {
                return "embedded";
            }

            @Override
            public String getScriptContents() {
                throw new RuntimeException("embedded executables do not have script contents available");
            }

            @Override
            public String getScriptName() {
                return getName();
            }

            @Override
            public Optional<File> getScriptFileLocation() {
                return Optional.empty();
            }
        };
    }
}
