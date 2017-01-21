package org.s4s0l.shathel.commons.filestorage

import org.apache.commons.io.FileUtils
import org.s4s0l.shathel.commons.core.Parameters
import spock.lang.Specification

/**
 * @author Matcin Wielgus
 */
class FileStorageTest extends Specification {

    def "FileStorageTest with overrides"() {
        given:
        FileStorage fs = create("build/FileStorageTest/1", [
                'shathel.storage.data.x.dir':'data_x',
                'shathel.storage.tmp.x.dir':f('tmp_x').absolutePath,
                'shathel.storage.shathel-solution.yml': 'my-file.yml'
        ])

        when:
        def dataDir = fs.getPersistedDirectory("x")

        then:
        dataDir.absolutePath == f("1/data_x").absolutePath

        when:
        def tmpDir = fs.getTemporaryDirectory("x")

        then:
        tmpDir.absolutePath == f("tmp_x").absolutePath

        when:
        def configuration = fs.getConfiguration()

        then:
        configuration.absolutePath == f("1/my-file.yml").absolutePath
    }


    def "FileStorageTest with defaults"() {
        given:
        FileStorage fs = create("build/FileStorageTest/1", [:])


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
        def directory = fs.getPersistedDirectory("x")

        then:
        directory.exists()
        f("1/data/x").exists()

        when:
        def tmpDir = fs.getTemporaryDirectory("x")

        then:
        tmpDir.exists()
        f("1/tmp/x").exists()

        when:
        f("1/tmp/x/aaaa").text = "something"
        fs.getTemporaryDirectory("x")

        then:
        f("1/tmp/x/aaaa").exists()

    }

    private File f(String name){
        return new File("build/FileStorageTest/$name");
    }

    private FileStorage create(String where, LinkedHashMap params) {
        FileUtils.deleteDirectory(new File(where))
        new FileStorage(new File(where), Parameters.builder().parameters(params).build())
    }


}
