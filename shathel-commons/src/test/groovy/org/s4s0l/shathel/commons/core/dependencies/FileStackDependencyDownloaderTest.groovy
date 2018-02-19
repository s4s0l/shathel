package org.s4s0l.shathel.commons.core.dependencies

import org.s4s0l.shathel.commons.core.MapParameters
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Marcin Wielgus
 */
class FileStackDependencyDownloaderTest extends Specification {

    String getRootDirName() {
        "build/Test${getClass().getSimpleName()}"
    }

    File getRootDir() {
        def f = new File(getRootDirName())
        f.mkdirs()
        f
    }

    static
    def baseDir = new File("src/test/FileStackDependencyDownloaderTest").absoluteFile
    static
    def dir1 = new File("src/test/FileStackDependencyDownloaderTest/dir1").absoluteFile
    static
    def dir2 = new File("src/test/FileStackDependencyDownloaderTest/dir2").absoluteFile

    static def sourceDirectoryLocations = new MapParameters([
            (FileStackDependencyDownloader.SHATHEL_FILE_BASE_DIR)       : "${dir1.absolutePath},${dir2.absolutePath},, ,".toString(),
            (FileStackDependencyDownloader.SHATHEL_FILE_DEFAULT_VERSION): '2.0'
    ])

    @Unroll
    def "file downloader should be able to locate stack #s in dir #d"(String s, String d) {
        given:
        def fileDownloader = new FileStackDependencyDownloader(sourceDirectoryLocations)
        when:
        def downloaded = fileDownloader.download(new StackLocator(s), getRootDir(), true)
        then:
        downloaded.get() == new File(baseDir, d)

        where:
        s                                | d
        "just-name"                      | "dir1/just-name"
        "with-version"                   | "dir1/with-version-2.0"
        "with-version-s"                 | "dir1/with-version-s-2.0-shathel"
        "onlyin2"                        | "dir2/onlyin2"
        "${dir2.absolutePath}/just-name" | "dir2/just-name"

    }
}
