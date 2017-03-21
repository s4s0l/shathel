package org.s4s0l.shathel.commons.ivy

import org.apache.commons.io.FileUtils
import org.s4s0l.shathel.commons.core.MapParameters
import org.s4s0l.shathel.commons.core.dependencies.StackLocator
import org.s4s0l.shathel.commons.core.stack.StackReference
import spock.lang.Specification

/**
 * @author Marcin Wielgus
 */
class IvyDownloaderTest extends Specification {
    def "Test remoteDownload"() {
        given:
        def downloader = new IvyDownloader(MapParameters.builder().build())

        when:
        def download = downloader.download(new StackLocator(new StackReference("org.s4s0l.shathel:shathel-stack-core:0.0.6")), getRootDir(), true)

        then:
        new File(download.get(), "shthl-stack.yml").text.contains("gav: org.s4s0l.shathel:shathel-stack-core:0.0.6")
    }


    def "Test with custom ivy settings - success"() {
        given:
        def downloader = new IvyDownloader(MapParameters.builder()
                .parameter(IvyDownloader.SHATHEL_IVY_SETTINGS, "./src/test/resources/ivy-settings.xml")
                .parameter("ivy.home", new File("${getRootDirName()}/.ivy2").getAbsolutePath())
                .build())

        when:
        def download = downloader.download(new StackLocator(new StackReference("org.s4s0l.shathel:shathel-stack-portainer:0.0.6")), getRootDir(), false)

        then:
        new File("${getRootDirName()}/.ivy2").exists()
        new File(download.get(), "shthl-stack.yml").text.contains("gav: org.s4s0l.shathel:shathel-stack-portainer:0.0.6")
    }


    def "Test with custom ivy settings - fail"() {
        given:
        def downloader = new IvyDownloader(MapParameters.builder().parameter(IvyDownloader.SHATHEL_IVY_SETTINGS, "./src/test/resources/ivy-settings-invalid.xml").build())

        when:
        def download = downloader.download(new StackLocator(new StackReference("org.s4s0l.shathel:shathel-stack-portainer:0.0.6")), getRootDir(), true)

        then:
        !download.isPresent()
    }


    def setupSpec() {
        FileUtils.deleteDirectory(getRootDir())

    }

    String getRootDirName() {
        "build/Test${getClass().getSimpleName()}"
    }

    File getRootDir() {
        return new File(getRootDirName())
    }
}
