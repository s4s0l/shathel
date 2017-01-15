package org.s4s0l.shathel.deployer

import org.springframework.shell.plugin.BannerProvider

/**
 * @author Matcin Wielgus
 */
class CustomBanner implements BannerProvider{
    @Override
    String getBanner() {
        return null
    }

    @Override
    String getVersion() {
        return null
    }

    @Override
    String getWelcomeMessage() {
        return null
    }

    @Override
    String getProviderName() {
        return "Custom Banner Provider"
    }
}
