package org.s4s0l.shathel.commons.bin

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.s4s0l.shathel.commons.utils.ExecWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class PreinstalledBinaryLocator implements BinaryLocator {
    private static
    final Logger LOGGER = LoggerFactory.getLogger(PreinstalledBinaryLocator.class);
    private final String binaryName;
    private final String versionCommand;
    private final String versionRegexpr;
    private final String requiredVersion;

    PreinstalledBinaryLocator(String binaryName, String versionCommand, String versionRegexpr, String requiredVersion) {
        this.binaryName = binaryName
        this.versionCommand = versionCommand
        this.versionRegexpr = versionRegexpr
        this.requiredVersion = requiredVersion
    }

    @Override
    String getBinaryName() {
        return binaryName
    }

    @Override
    Optional<String> locate(File baseDir) {
        Optional.of(binaryName)
    }

    @Override
    String getRequiredVersion() {
        return requiredVersion
    }

    @Override
    Optional<String> getVersionFound(File baseDir) {
        try {
            def output = new ExecWrapper(LOGGER, binaryName).executeForOutput(versionCommand)
            List<String> groups = (output =~ versionRegexpr)[0] as List<String>
            return Optional.of(groups[1])
        } catch (Exception e) {
            LOGGER.trace("Unable to check version of $binaryName", e)
        }
        Optional.empty()

    }
}
