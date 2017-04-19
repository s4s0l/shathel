package org.s4s0l.shathel.commons.bin

import org.apache.commons.io.FileUtils
import org.s4s0l.shathel.commons.utils.ExecWrapper
import spock.lang.Specification

/**
 * @author Marcin Wielgus
 */
class DownloadableBinaryLocatorTest extends Specification {
    String getRootDirName() {
        "build/Test${getClass().getSimpleName()}"
    }

    File getRootDir() {
        return new File(getRootDirName())
    }

    def setup() {
        FileUtils.deleteDirectory(getRootDir())
        getRootDir().mkdirs()
    }

    def "DownloadableBinaryLocator should download requested binary"() {
        when:
        DownloadableBinaryLocator locator = new DownloadableBinaryLocator(
                "packer",
                "1.0.0",
                "version",
                /v(([0-9]+\.?)+)/,
                "https://releases.hashicorp.com/packer/1.0.0/packer_1.0.0_linux_amd64.zip"
        )

        then:
        locator.requiredVersion == "1.0.0"
        locator.getVersionFound(getRootDir()).get() == "1.0.0"
        locator.binaryName == "packer"
        locator.locate(getRootDir()).get() == "${getRootDir().absolutePath}/packer/1.0.0/packer"
        !new File("${getRootDir().absolutePath}/packer/1.0.0/packer.zip").exists()
    }


    def "DownloadableBinaryLocator shouldNot download if installed version is ok"() {
        when:
        DownloadableBinaryLocator locator = new DownloadableBinaryLocator(
                "openssl",
                ("openssl version".execute().text =~ /(([0-9]+\.?)+)/)[0][1],
                "version",
                /(([0-9]+\.?)+)/,
                "https://releases.hashicorp.com/packer/1.0.0/packer_1.0.0_linux_amd64.zip"
        )

        then:
        locator.locate(getRootDir()).get() == "openssl"

    }
}
