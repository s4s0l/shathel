package org.s4s0l.shathel.deployer.shell.customization;

import org.springframework.shell.plugin.BannerProvider;
import org.springframework.shell.support.util.VersionUtils;
import org.springframework.stereotype.Component;

/**
 * @author Marcin Wielgus
 */
@Component
public class CustomBanner implements BannerProvider {
    @Override
    public String getBanner() {
        return null;
    }
    public static String versionInfo() {
        Package pkg = CustomBanner.class.getPackage();
        String version = null;
        if (pkg != null) {
            version = pkg.getImplementationVersion();
        }
        return (version != null ? version : "Unknown Version");
    }
    @Override
    public String getVersion() {
        return versionInfo();
    }

    @Override
    public String getWelcomeMessage() {
        return null;
    }

    @Override
    public String getProviderName() {
        return "Shathel Deployer";
    }

}
