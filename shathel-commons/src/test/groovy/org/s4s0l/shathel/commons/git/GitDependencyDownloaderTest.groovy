package org.s4s0l.shathel.commons.git

import org.apache.commons.io.FileUtils
import org.s4s0l.shathel.commons.core.MapParameters
import org.s4s0l.shathel.commons.core.dependencies.StackLocator
import org.s4s0l.shathel.commons.core.stack.StackReference
import spock.lang.Specification

/**
 * @author Marcin Wielgus
 */
class GitDependencyDownloaderTest extends Specification {


    def setupSpec() {
        FileUtils.deleteDirectory(getRootDir())
    }

    def "download dependency"() {
        given:
        def params = MapParameters.builder()
                .parameter(GitDependencyDownloader.SHATHEL_GIT_DEFAULT_VERSION, "0.0.2")
                .parameter(GitDependencyDownloader.SHATHEL_GIT_DEFAULT_GROUP, "github.com/s4s0l/shathel-sample-stacks")
                .build()
        GitDependencyDownloader gdd = new GitDependencyDownloader(params)

        when:
        def stack = new StackLocator(new StackReference("git@github.com/s4s0l/shathel-sample-stacks:shathel-sample-stacks:a173704738d1d1d0e49901db43103795749a53a0"))
        def download = gdd.download(stack, getRootDir(), true)

        then:
        download.get().exists()
        download.get().getAbsolutePath().contains("a173704738d1d1d0e49901db43103795749a53a0")
        new File(download.get(), "shthl-stack.yml").text == """version: 1
shathel-stack:
  gav: git@github.com/s4s0l/shathel-sample-stacks:shathel-sample-stacks:a173704738d1d1d0e49901db43103795749a53a0
  dependencies:
    git@github.com/s4s0l/shathel-sample-stacks:child-stack:a173704738d1d1d0e49901db43103795749a53a0:"""




        when:
        def stack2 = new StackLocator(new StackReference("git@github.com/s4s0l/shathel-sample-stacks:child-stack:0.0.1"))
        def download2 = gdd.download(stack2, getRootDir(), true)

        then:
        download2.get().exists()
        download2.get().getAbsolutePath().contains("0.0.1")
        new File(download2.get(), "shthl-stack.yml").text == """version: 1
shathel-stack:
  gav: git@github.com/s4s0l/shathel-sample-stacks:child-stack:0.0.1
"""





        when:
        def stack3 = new StackLocator(new StackReference("git@github.com/s4s0l/shathel-sample-stacks:shathel-sample-stacks:a173704738d1d1d0e49901db43103795749a53a0@master"))
        def download3 = gdd.download(stack3, getRootDir(), true)

        then:
        download3.get().exists()
        download3.get().getAbsolutePath().contains("a173704738d1d1d0e49901db43103795749a53a0@master")
        new File(download3.get(), "shthl-stack.yml").text == """version: 1
shathel-stack:
  gav: git@github.com/s4s0l/shathel-sample-stacks:shathel-sample-stacks:a173704738d1d1d0e49901db43103795749a53a0@master
  dependencies:
    git@github.com/s4s0l/shathel-sample-stacks:child-stack:a173704738d1d1d0e49901db43103795749a53a0@master:"""


        when:
        def stack4 = new StackLocator(new StackReference("aaa:bbb:1"))
        def download4 = gdd.download(stack4, getRootDir(), true)

        then:
        !download4.isPresent()

        when:
        def stack5 = new StackLocator("git@child-stack")
        def download5 = gdd.download(stack5, getRootDir(), true)

        then:
        download5.get().exists()
        download5.get().getAbsolutePath().contains("0.0.2")
        new File(download5.get(), "shthl-stack.yml").text == """version: 1
shathel-stack:
  gav: git@github.com/s4s0l/shathel-sample-stacks:child-stack:0.0.2
"""

        when:
        //not existing
        def stack6 = new StackLocator(new StackReference("git@github.com/s4s0l/shathel-sample-stacks:child-stack:53b4dad37a11de70e2bcc3196119944b207b0748@master"))
        def download6 = gdd.download(stack6, getRootDir(), true)

        then:
        thrown(RuntimeException)

    }

    File getRootDir() {
        return new File(getRootDirName())
    }

    String getRootDirName() {
        "build/Test${getClass().getSimpleName()}"
    }
}
