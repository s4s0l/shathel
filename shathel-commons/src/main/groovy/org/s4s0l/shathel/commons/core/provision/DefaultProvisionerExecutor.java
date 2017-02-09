package org.s4s0l.shathel.commons.core.provision;

import org.apache.commons.collections.map.HashedMap;
import org.s4s0l.shathel.commons.core.environment.Environment;
import org.s4s0l.shathel.commons.core.stack.StackProvisionerDefinition;
import org.s4s0l.shathel.commons.scripts.Executor;
import org.s4s0l.shathel.commons.scripts.ScriptExecutorProvider;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author Matcin Wielgus
 */
public class DefaultProvisionerExecutor implements EnvironmentProvisionExecutor, EnvironmentProvisionExecutorContext {

    private final Environment environment;

    public DefaultProvisionerExecutor(Environment environment) {
        this.environment = environment;
    }

    @Override
    public EnvironmentProvisionExecutorContext createContext() {
        return this;
    }

    @Override
    public void executePreProvisioners(File dstStackDir, StackCommand stackCommand) {
        List<StackProvisionerDefinition> preProvisioners = stackCommand.getDescription().getPreProvisioners();
        executeProvisioners(dstStackDir,stackCommand, preProvisioners);
        executeProvisioners(dstStackDir,stackCommand, stackCommand.getEnricherPreProvisioners());
    }

    @Override
    public void executePostProvisioners(File dstStackDir, StackCommand stackCommand) {
        List<StackProvisionerDefinition> postProvisioners = stackCommand.getDescription().getPostProvisioners();
        executeProvisioners(dstStackDir, stackCommand, postProvisioners);

    }

    private void executeProvisioners(File dstStackDir,StackCommand stackCommand, List<StackProvisionerDefinition> postProvisioners) {
        for (StackProvisionerDefinition postProvisioner : postProvisioners) {
            Executor executor = ScriptExecutorProvider
                    .findExecutor(environment.getEnvironmentContext().getExtensionContext(), postProvisioner)
                    .orElseThrow(() -> new RuntimeException("No executor fouind for " + postProvisioner));
            execute(dstStackDir,executor, stackCommand);
        }
    }

    private void execute(File dstStackDir,Executor executor, StackCommand stackCommand) {
        Map<String, Object> ctxt = new HashedMap();
        ctxt.put("context", environment.getEnvironmentContext());
        ctxt.put("env", environment.getEnvironmentApiFacade());
        ctxt.put("command", stackCommand);
        ctxt.put("dir", dstStackDir);
        executor.execute(ctxt);
    }

    @Override
    public void close() throws Exception {

    }
}
