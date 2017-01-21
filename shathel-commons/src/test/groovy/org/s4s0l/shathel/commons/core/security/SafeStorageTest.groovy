package org.s4s0l.shathel.commons.core.security

import org.apache.commons.io.FileUtils
import spock.lang.Specification

/**
 * @author Matcin Wielgus
 */
class SafeStorageTest extends Specification {
    static PASS = "secret".chars
    def setupSpec() {
        FileUtils.deleteDirectory(new File("build/endDecTests"))
        new File("build/endDecTests/src").mkdirs()

    }



    def "Should encrypt and decrypt file"(){
        given:
        def x = new File("build/endDecTests/src/sample").with {
            text = "ala ma kota"
            it
        }
        SafeStorageImpl storage = new SafeStorageImpl(new File("build/endDecTests/dst"),PASS.clone())

        when:
        storage.writeFile("test", x)

        then:
        new File("build/endDecTests/dst/files/test").exists()
        !new File("build/endDecTests/dst/files/test").text.contains("kota")

        when:
        new SafeStorageImpl(new File("build/endDecTests/dst2"),PASS.clone()).writeFile("test2", x)

        then:
        new File("build/endDecTests/dst2/files/test2").exists()
        new File("build/endDecTests/dst2/files/test2").text != new File("build/endDecTests/dst/files/test").text


        when:
        SafeStorageImpl storage2 = new SafeStorageImpl(new File("build/endDecTests/dst"),PASS.clone())
        storage2.readFile("test", new File("build/endDecTests/src/sample_out"))

        then:
        new File("build/endDecTests/src/sample_out").text == "ala ma kota"
    }

    def "Should encrypt and decrypt string literals"(){
        given:
        SafeStorageImpl storage = new SafeStorageImpl(new File("build/endDecTests/dst"),PASS.clone())

        when:
        storage.writeValue("someKey", "someValue")

        then:
        new File("build/endDecTests/dst/values/someKey").exists()
        !new File("build/endDecTests/dst/values/someKey").text.contains("someValue")

        when:
        new SafeStorageImpl(new File("build/endDecTests/dst2"),PASS.clone()).writeValue("someKey", "someValue")

        then:
        new File("build/endDecTests/dst2/values/someKey").exists()
        new File("build/endDecTests/dst2/values/someKey").text != new File("build/endDecTests/dst/values/someKey").text


        when:
        def value = new SafeStorageImpl(new File("build/endDecTests/dst"),PASS.clone()).readValue("someKey")

        then:
        value.get() == "someValue"

        when:
        def value2 = new SafeStorageImpl(new File("build/endDecTests/dst2"),PASS.clone()).readValue("someKey")

        then:
        value2.get() == "someValue"
    }
}
