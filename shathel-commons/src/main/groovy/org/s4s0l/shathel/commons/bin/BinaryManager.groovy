package org.s4s0l.shathel.commons.bin

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.utils.ExtensionContext
import org.s4s0l.shathel.commons.utils.ExtensionInterface

import java.security.cert.Extension

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
interface BinaryManager {

    String locate(String name)

    List<BinaryVerificationResult> verifyAll()
}

interface BinaryManagerExtensionManager extends ExtensionInterface {
    BinaryManager getManager(ExtensionContext extensionContext)
}

@TypeChecked
@CompileStatic
class BinaryVerificationResult {
    BinaryVerificationResult(String binaryName, String versionExpected, Optional<String> versionFound) {
        this.binaryName = binaryName
        this.versionExpected = versionExpected
        this.versionFound = versionFound
    }
    final String binaryName
    final String versionExpected
    final Optional<String> versionFound

    boolean isOk() {
        return versionFound.isPresent()
    }
}

@TypeChecked
@CompileStatic
interface BinaryLocator extends ExtensionInterface {

    String getBinaryName()

    Optional<String> locate(File binBase)

    String getRequiredVersion()

    Optional<String> getVersionFound(File binBase)

}
