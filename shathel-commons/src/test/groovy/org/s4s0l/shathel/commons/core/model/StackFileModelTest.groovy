package org.s4s0l.shathel.commons.core.model

import org.s4s0l.shathel.commons.core.model.StackFileModel
import org.yaml.snakeyaml.Yaml
import spock.lang.Specification

/**
 * @author Marcin Wielgus
 */
class StackFileModelTest extends Specification {


    def "Should read dependencies"(){

        when:
        StackFileModel model = new StackFileModel(new Yaml().load(
                """
            version: 1
            shathel-stack:
              dependencies:
                org.s4s0l.shathel.gradle.sample:simple-project:1.2.3-SNAPSHOT:
        """))
        def dependencies = model.dependencies
        def x = null


        then:
        x == null
        dependencies.size() == 1
        dependencies[0].maxVersion == null
        dependencies[0].minVersion == null
        dependencies[0].gav == "org.s4s0l.shathel.gradle.sample:simple-project:1.2.3-SNAPSHOT"

        when:
        model = new StackFileModel(new Yaml().load(
                """
            version: 1
            shathel-stack:
              dependencies:
                org.s4s0l.shathel.gradle.sample:simple-project:1.2.3-SNAPSHOT:
                org.s4s0l.shathel.gradle.sample:simple-project2:1.2.4-SNAPSHOT:
                        min: "1.2.3-SNAPSHOT"
                        max: "1.2.5-SNAPSHOT"
        """))
        dependencies = model.dependencies


        then:
        dependencies.size() == 2
        dependencies[1].maxVersion == "1.2.5-SNAPSHOT"
        dependencies[1].minVersion == "1.2.3-SNAPSHOT"
        dependencies[1].gav == "org.s4s0l.shathel.gradle.sample:simple-project2:1.2.4-SNAPSHOT"
    }

}
