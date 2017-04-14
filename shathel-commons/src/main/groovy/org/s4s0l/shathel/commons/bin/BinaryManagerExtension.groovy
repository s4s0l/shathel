package org.s4s0l.shathel.commons.bin

import org.s4s0l.shathel.commons.core.CommonParams
import org.s4s0l.shathel.commons.core.ParameterProvider
import org.s4s0l.shathel.commons.utils.ExtensionContext

import java.util.stream.Collectors

/**
 * @author Marcin Wielgus
 */
class BinaryManagerExtension implements BinaryManagerExtensionManager {
    ParameterProvider parameterProvider

    BinaryManagerExtension(ParameterProvider parameterProvider) {
        this.parameterProvider = parameterProvider
    }

    @Override
    BinaryManager getManager(ExtensionContext extensionContext) {
        return new BinaryManagerImpl(getLocators(extensionContext), getBaseDir())
    }


    private List getLocators(ExtensionContext extensionContext) {
        extensionContext.lookupAll(BinaryLocator).collect(Collectors.toList())
    }

    private File getBaseDir() {
        def file = new File(parameterProvider.getParameter(CommonParams.SHATHEL_DIR).orElse(".shathel"), "bin")
        file.mkdirs()
        return file
    }

}
