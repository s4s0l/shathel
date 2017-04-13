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

    DownloadableBinaryLocator(String binaryName, String version, String versionCommand, String versionRegexpr, String url) {
        this.binaryName = binaryName
        this.version = version
        this.versionCommand = versionCommand
        this.versionRegexpr = versionRegexpr
        this.url = url
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

        def localVersion = getVersion(binaryName)
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
        getVersion(locate(binBase).get())
    }


    Optional<String> getVersion(String command) {
        try {
            def output = new ExecWrapper(LOGGER, command).executeForOutput(versionCommand)
            List<String> groups = (output =~ versionRegexpr)[0] as List<String>
            return Optional.of(groups[1])
        } catch (Exception e) {
            LOGGER.trace("Unable to check version of $binaryName", e)
        }
        Optional.empty()
    }


    static downloadFile(File localFile, String remoteUrl) {
        localFile.withOutputStream { out ->
            new URL(remoteUrl).withInputStream { from -> IOGroovyMethods.leftShift(out, from) }
        }
    }
}
