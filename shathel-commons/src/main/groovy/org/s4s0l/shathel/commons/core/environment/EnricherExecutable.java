package org.s4s0l.shathel.commons.core.environment;

import org.apache.commons.lang.NotImplementedException;
import org.s4s0l.shathel.commons.scripts.NamedExecutable;

import java.util.Map;

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


}
