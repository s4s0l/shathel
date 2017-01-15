package org.s4s0l.shathel.deployer.shell.customization

import org.springframework.shell.plugin.BannerProvider
import org.springframework.stereotype.Component

/**
 * @author Matcin Wielgus
 */
@Component
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
