package org.s4s0l.shathel.commons.core.environment;

import org.apache.commons.lang.NotImplementedException;
import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.scripts.Executable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Matcin Wielgus
 */
public abstract class EnricherExecutable implements Executable {

    @Override
    public final Object execute(Map<String, Object> context) {
        ComposeFileModel model = (ComposeFileModel) context.get("compose");
        StackDescription stack = (StackDescription) context.get("stack");
        ExecutableApiFacade apiFacade = (ExecutableApiFacade) context.get("env");
        EnvironmentContext environmentContext = (EnvironmentContext) context.get("context");
        return executeProvidingProvisioner(environmentContext, apiFacade, stack, model);
    }

    protected List<Executable> executeProvidingProvisioner(EnvironmentContext environmentContext,
                                                           ExecutableApiFacade apiFacade,
                                                           StackDescription stack,
                                                           ComposeFileModel model) {
        execute(environmentContext, apiFacade, stack, model);
        return Collections.emptyList();
    }

    protected void execute(EnvironmentContext environmentContext,
                           ExecutableApiFacade apiFacade,
                           StackDescription stack,
                           ComposeFileModel model) {
        throw new NotImplementedException("wtf??");
    }
}
