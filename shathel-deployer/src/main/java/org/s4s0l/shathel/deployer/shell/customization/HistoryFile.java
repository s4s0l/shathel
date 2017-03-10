package org.s4s0l.shathel.deployer.shell.customization;

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
            File file = new File(System.getProperty("user.home"), ".shathel/shell-history.log");
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

    @Override
    public String getProviderName() {
        return "History Provider";
    }

}
