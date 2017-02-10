package org.s4s0l.shathel.commons.core.environment;

import org.apache.commons.lang.NotImplementedException;
import org.s4s0l.shathel.commons.core.environment.EnvironmentApiFacade;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext;
import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.scripts.Executor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Matcin Wielgus
 */
public abstract class EnricherExecutor implements Executor {

    @Override
    public final Object execute(Map<String, Object> context) {
        ComposeFileModel model = (ComposeFileModel) context.get("compose");
        StackDescription stack = (StackDescription) context.get("stack");
        EnvironmentApiFacade apiFacade = (EnvironmentApiFacade) context.get("env");
        EnvironmentContext environmentContext = (EnvironmentContext) context.get("context");
        return executeProvidingProvisioner(environmentContext, apiFacade, stack, model);
    }

    protected List<Executor> executeProvidingProvisioner(EnvironmentContext environmentContext,
                                                         EnvironmentApiFacade apiFacade,
                                                         StackDescription stack,
                                                         ComposeFileModel model) {
        execute(environmentContext, apiFacade, stack, model);
        return Collections.emptyList();
    }

    protected void execute(EnvironmentContext environmentContext,
                           EnvironmentApiFacade apiFacade,
                           StackDescription stack,
                           ComposeFileModel model) {
        throw new NotImplementedException("wtf??");
    }
}
