package org.s4s0l.shathel.commons.bin

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

import java.util.stream.Collectors

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
class BinaryManagerImpl implements BinaryManager {

    private final List<BinaryLocator> locators
    private final File binBase

    BinaryManagerImpl(List<BinaryLocator> locators, File binBase) {
        this.locators = locators
        this.binBase = binBase
    }

    @Override
    String locate(String name) {
        locators.stream()
                .filter { it.binaryName == name }
                .map { it.locate(binBase) }
                .filter { it.isPresent() }
                .findFirst()
                .map { it.get() }
                .orElseThrow {
            new RuntimeException("Unable to find executable for $name")
        }
    }

    @Override
    List<BinaryVerificationResult> verifyAll() {
        locators.stream().map {
            new BinaryVerificationResult(it.binaryName, it.requiredVersion, it.getVersionFound(binBase))
        }.collect(Collectors.toList())
    }
}
