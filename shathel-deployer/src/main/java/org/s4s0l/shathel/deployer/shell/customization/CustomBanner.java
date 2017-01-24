package org.s4s0l.shathel.deployer.shell.customization;

import org.springframework.shell.plugin.BannerProvider;
import org.springframework.stereotype.Component;

/**
 * @author Matcin Wielgus
 */
@Component
public class CustomBanner implements BannerProvider {
    @Override
    public String getBanner() {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public String getWelcomeMessage() {
        return null;
    }

    @Override
    public String getProviderName() {
        return "Custom Banner Provider";
    }

}
