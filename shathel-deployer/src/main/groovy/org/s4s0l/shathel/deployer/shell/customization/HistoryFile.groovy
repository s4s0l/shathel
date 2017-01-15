package org.s4s0l.shathel.deployer.shell.customization

import org.springframework.shell.plugin.HistoryFileNameProvider
import org.springframework.stereotype.Component

/**
 * @author Matcin Wielgus
 */
@Component
class HistoryFile implements HistoryFileNameProvider{
    @Override
    String getHistoryFileName() {
        def file = new File(System.getProperty("user.home"), ".shathel/shell-history.log")
        if(!file.exists()){
            if(!file.parentFile.exists()){
                file.parentFile.mkdirs();
            }
            file.createNewFile();
        }
        return file.absolutePath
    }

    @Override
    String getProviderName() {
        return "History Provider"
    }
}
