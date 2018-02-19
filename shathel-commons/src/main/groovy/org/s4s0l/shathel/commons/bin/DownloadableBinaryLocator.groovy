package org.s4s0l.shathel.commons.bin

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.codehaus.groovy.runtime.IOGroovyMethods
import org.s4s0l.shathel.commons.utils.ExecWrapper
import org.s4s0l.shathel.commons.utils.IoUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class DownloadableBinaryLocator implements BinaryLocator {
    private static
    final Logger LOGGER = LoggerFactory.getLogger(DownloadableBinaryLocator.class)
    private final String binaryName
    private final String version
    private final String versionCommand
    private final String versionRegexpr
    private final String url
    private final boolean useGlobalBinary
/**
 * If versionCommand is empty no test against binary installed globally is performed
 */
    DownloadableBinaryLocator(String binaryName, String version, String versionCommand, String versionRegexpr, String url, boolean useGlobalBinary = true) {
        this.binaryName = binaryName
        this.version = version
        this.versionCommand = versionCommand
        this.versionRegexpr = versionRegexpr
        this.url = url
        this.useGlobalBinary = useGlobalBinary
    }

    @Override
    String getBinaryName() {
        return binaryName
    }

    File getDownloadedFileName(File binBase) {
        return new File(binBase, "$binaryName/$version/$binaryName")
    }

    File getDownloadedZipFileName(File binBase) {
        return new File(binBase, "$binaryName/$version/${binaryName}.zip")
    }

    @Override
    Optional<String> locate( File binBase) {

        def localVersion = getLocallyInstalledVersion()
        if (localVersion.isPresent() && localVersion.get() == version) {
            return Optional.of(binaryName)
        }

        if (!getDownloadedFileName(binBase).exists()) {
            getDownloadedFileName(binBase).parentFile.mkdirs()
            downloadFile(getDownloadedZipFileName(binBase), url)
            IoUtils.unZipIt(getDownloadedZipFileName(binBase), getDownloadedFileName(binBase).parentFile)
            getDownloadedZipFileName(binBase).delete()
            Files.setPosixFilePermissions(getDownloadedFileName(binBase).toPath(), Collections.singleton(PosixFilePermission.OWNER_EXECUTE))
        }
        return Optional.of(getDownloadedFileName(binBase).absolutePath)

    }

    @Override
    String getRequiredVersion() {
        return version
    }

    @Override
    Optional<String> getVersionFound(File binBase) {
        getVersionFrom(locate(binBase).get())
    }


    Optional<String> getLocallyInstalledVersion() {
        if (!useGlobalBinary) {
            return Optional.empty()
        }
        return getVersionFrom(binaryName)
    }

    private Optional<String> getVersionFrom(String binaryPath) {
        try {
            def output = new ExecWrapper(LOGGER, binaryPath).executeForOutput(versionCommand)
            List<String> groups = (output =~ versionRegexpr)[0] as List<String>
            return Optional.of(groups[1])
        } catch (Exception e) {
            LOGGER.trace("Unable to check version of $binaryPath", e)
        }
        Optional.empty()
    }


    static downloadFile(File localFile, String remoteUrl) {
        localFile.withOutputStream { out ->
            new URL(remoteUrl).withInputStream { from -> IOGroovyMethods.leftShift(out, from) }
        }
    }
}
