package org.s4s0l.shathel.commons.filestorage

import org.apache.commons.io.FileUtils
import org.s4s0l.shathel.commons.core.MapParameters
import org.s4s0l.shathel.commons.core.Parameters
import spock.lang.Specification

/**
 * @author Marcin Wielgus
 */
class FileStorageTest extends Specification {

    def getRootDir() {
        return "build/Test${getClass().getSimpleName()}"
    }

    def "FileStorageTest with overrides"() {
        given:
        FileStorage fs = create("${rootDir}/1" )
        def params = MapParameters.builder().parameters([
                'dataDir'          : 'data_x',
                'tempDir'           : f('tmp_x').absolutePath
        ]).build()

        when:
        def dataDir = fs.getDataDirectory(params, "xxx")

        then:
        dataDir.absolutePath == f("1/data_x").absolutePath

        when:
        def tmpDir = fs.getTemptDirectory(params, "xxx")

        then:
        tmpDir.absolutePath == f("tmp_x").absolutePath

        when:
        def configuration = fs.getConfiguration()

        then:
        configuration.absolutePath == f("1/shathel-solution.yml").absolutePath
    }


    def "FileStorageTest with defaults"() {
        given:
        FileStorage fs = create("${rootDir}/1")
        def params = MapParameters.builder().parameters([:]).build()

        when:
        fs.verify()

        then:
        thrown(RuntimeException)

        when:
        fs.getConfiguration().text = "aaa"
        fs.verify()

        then:
        noExceptionThrown()
        f("1/shathel-solution.yml").exists()

        when:
        def directory = fs.getDataDirectory(params, "dummy")

        then:
        directory.exists()
        f("1/dummy/data").exists()

        when:
        def tmpDir = fs.getTemptDirectory(params, "dummy")

        then:
        tmpDir.exists()
        f("1/dummy/temp").exists()



    }

    private File f(String name) {
        return new File("${rootDir}/$name");
    }

    private FileStorage create(String where) {
        FileUtils.deleteDirectory(new File(where))
        new FileStorage(new File(where))
    }


}
