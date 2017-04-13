package org.s4s0l.shathel.commons.bin

import spock.lang.Specification

/**
 * @author Marcin Wielgus
 */
class PreinstalledBinaryLocatorTest extends Specification {

    def "Preinstalled binary locator should detect version of installed binary"() {
        when:
        PreinstalledBinaryLocator locator = new PreinstalledBinaryLocator(
                "ls", "--version", /(([0-9]+\.?)+)/, "any"
        )

        then:
        locator.requiredVersion == "any"
        locator.getVersionFound(null).isPresent()
        locator.getVersionFound(null).get().length() > 1
        locator.binaryName == "ls"
        locator.locate().get() == "ls"
    }

}
