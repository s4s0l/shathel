package org.s4s0l.shathel.commons.core.security

import org.apache.commons.io.FileUtils
import spock.lang.Specification

/**
 * @author Matcin Wielgus
 */
class SafeStorageTest extends Specification {
    static PASS = "secret".chars
    def setupSpec() {
        FileUtils.deleteDirectory(new File("${rootDir}"))
        new File("${rootDir}/src").mkdirs()

    }

    def getRootDir(){
        return "build/Test${getClass().getSimpleName()}"
    }


    def "Should encrypt and decrypt file"(){
        given:
        def x = new File("${rootDir}/src/sample").with {
            text = "ala ma kota"
            it
        }
        SafeStorageImpl storage = new SafeStorageImpl(new File("${rootDir}/dst"),PASS.clone())

        when:
        storage.writeFile("test", x)

        then:
        new File("${rootDir}/dst/files/test").exists()
        !new File("${rootDir}/dst/files/test").text.contains("kota")

        when:
        new SafeStorageImpl(new File("${rootDir}/dst2"),PASS.clone()).writeFile("test2", x)

        then:
        new File("${rootDir}/dst2/files/test2").exists()
        new File("${rootDir}/dst2/files/test2").text != new File("${rootDir}/dst/files/test").text


        when:
        SafeStorageImpl storage2 = new SafeStorageImpl(new File("${rootDir}/dst"),PASS.clone())
        storage2.readFile("test", new File("${rootDir}/src/sample_out"))

        then:
        new File("${rootDir}/src/sample_out").text == "ala ma kota"
    }

    def "Should encrypt and decrypt string literals"(){
        given:
        SafeStorageImpl storage = new SafeStorageImpl(new File("${rootDir}/dst"),PASS.clone())

        when:
        storage.writeValue("someKey", "someValue")

        then:
        new File("${rootDir}/dst/values/someKey").exists()
        !new File("${rootDir}/dst/values/someKey").text.contains("someValue")

        when:
        new SafeStorageImpl(new File("${rootDir}/dst2"),PASS.clone()).writeValue("someKey", "someValue")

        then:
        new File("${rootDir}/dst2/values/someKey").exists()
        new File("${rootDir}/dst2/values/someKey").text != new File("${rootDir}/dst/values/someKey").text


        when:
        def value = new SafeStorageImpl(new File("${rootDir}/dst"),PASS.clone()).readValue("someKey")

        then:
        value.get() == "someValue"

        when:
        def value2 = new SafeStorageImpl(new File("${rootDir}/dst2"),PASS.clone()).readValue("someKey")

        then:
        value2.get() == "someValue"
    }
}
