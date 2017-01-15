package org.s4s0l.shathel.deployer

import org.springframework.shell.plugin.support.DefaultPromptProvider

/**
 * @author Matcin Wielgus
 */
class CustomPrompt extends DefaultPromptProvider {

    /**
     * Getter for the Prompt.
     *
     * @return String
     */
    @Override
    public final String getPrompt() {
        return "\$shathel>";
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