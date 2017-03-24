package org.s4s0l.shathel.deployer.shell.customization;

import org.s4s0l.shathel.commons.core.CommonParams;
import org.springframework.shell.plugin.HistoryFileNameProvider;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author Marcin Wielgus
 */
@Component
public class HistoryFile implements HistoryFileNameProvider {
    @Override
    public String getHistoryFileName() {
        try {
            File file = getHistoryFile();
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }
            return file.getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public File getHistoryFile() {
        String property = System.getProperty("shathel.deployer.historyfile");
        if (property == null) {
            property = "shell-history.log";
        }
        if (new File(property).isAbsolute()) {
            return new File(property);
        } else {
            String shathelDir = System.getenv("SHATHEL_DIR");
            if (shathelDir == null) {
                shathelDir = System.getProperty(CommonParams.SHATHEL_DIR);
            }
            if (shathelDir == null) {
                shathelDir = new File(System.getProperty("user.home"), ".shathel").getAbsolutePath();
            }
            return new File(shathelDir, property);
        }
    }

    @Override
    public String getProviderName() {
        return "History Provider";
    }

}
