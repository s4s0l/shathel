package org.s4s0l.shathel.commons.core.environment;

import org.apache.commons.lang.NotImplementedException;
import org.s4s0l.shathel.commons.core.Stack;
import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.scripts.Executable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin Wielgus
 */
public abstract class EnricherExecutable implements Executable {

    @Override
    public final Object execute(Map<String, Object> context) {

        return executeProvidingProvisioner(new EnricherExecutableParams(context));
    }

    protected List<Executable> executeProvidingProvisioner(EnricherExecutableParams enricherExecutableParams) {
        execute(enricherExecutableParams);
        return Collections.emptyList();
    }

    protected void execute(EnricherExecutableParams enricherExecutableParams) {
        throw new NotImplementedException("wtf??");
    }
}
