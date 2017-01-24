package org.s4s0l.shathel.deployer.shell.customization;

import org.springframework.shell.plugin.support.DefaultPromptProvider;
import org.springframework.stereotype.Component;

/**
 * @author Matcin Wielgus
 */
@Component
public class CustomPrompt extends DefaultPromptProvider {
    /**
     * Getter for the Prompt.
     *
     * @return String
     */
    @Override
    public final String getPrompt() {
        return "$shathel>";
    }

    /**
     * Getter for the Providername.
     *
     * @return String
     */
    @Override
    public final String getProviderName() {
        return "Custom prompt provider";
    }

}
