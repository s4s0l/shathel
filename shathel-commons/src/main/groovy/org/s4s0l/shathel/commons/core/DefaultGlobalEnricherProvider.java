package org.s4s0l.shathel.commons.core;

import org.s4s0l.shathel.commons.core.enricher.Enricher;
import org.s4s0l.shathel.commons.core.enricher.GlobalEnricherProvider;
import org.s4s0l.shathel.commons.core.enricher.GroovishEnricher;
import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.core.stack.StackProvisionerDefinition;
import org.s4s0l.shathel.commons.core.stack.StackReference;

import java.util.Collections;
import java.util.List;

/**
 * @author Matcin Wielgus
 */
public class DefaultGlobalEnricherProvider implements GlobalEnricherProvider {
    @Override
    public List<Enricher> getGlobalEnrichers() {
        return Collections.singletonList((stack, shathelStackFileModel) -> {

            shathelStackFileModel.addLabelToServices("org.shathel.stack.gav", stack.getGav());
            shathelStackFileModel.addLabelToServices("org.shathel.stack.deployName", stack.getDeployName());
            shathelStackFileModel.addLabelToServices("org.shathel.stack.ga", stack.getGroup() + ":" + stack.getName());
            shathelStackFileModel.addLabelToServices("org.shathel.deployer.version", "1.0.0");
            List<StackReference> dependencies = stack.getDependencies();
            int i = 0;
            for (StackReference dependency : dependencies) {
                shathelStackFileModel.addLabelToServices("org.shathel.stack.dependency." + i, dependency.getGav());
                i++;
            }
            return Collections.EMPTY_LIST;

        });
    }
}
