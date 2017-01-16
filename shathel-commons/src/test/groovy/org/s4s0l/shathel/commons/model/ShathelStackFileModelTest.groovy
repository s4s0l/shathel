package org.s4s0l.shathel.commons.model

import org.yaml.snakeyaml.Yaml
import spock.lang.Specification

/**
 * @author Matcin Wielgus
 */
class ShathelStackFileModelTest extends Specification {


    def "Should read dependencies"(){

        when:
        ShathelStackFileModel model = new ShathelStackFileModel(new Yaml().load(
                """
            version: 1
            shathel-stack:
              dependencies:
                - org.s4s0l.shathel.gradle.sample:simple-project:
                    version:
                        expected: "1.2.3-SNAPSHOT"
        """))
        def dependencies = model.dependencies
        def x = null


        then:
        x == null
        dependencies.size() == 1
        dependencies[0].maxVersion == null
        dependencies[0].minVersion == null
        dependencies[0].expectedVersion == "1.2.3-SNAPSHOT"
        dependencies[0].groupAndProject == "org.s4s0l.shathel.gradle.sample:simple-project"

        when:
        model = new ShathelStackFileModel(new Yaml().load(
                """
            version: 1
            shathel-stack:
              dependencies:
                - org.s4s0l.shathel.gradle.sample:simple-project:
                    version:
                        expected: "1.2.3-SNAPSHOT"
                - org.s4s0l.shathel.gradle.sample:simple-project2:
                    version:
                        expected: "1.2.4-SNAPSHOT"
                        min: "1.2.3-SNAPSHOT"
                        max: "1.2.5-SNAPSHOT"
        """))
        dependencies = model.dependencies


        then:
        dependencies.size() == 2
        dependencies[1].maxVersion == "1.2.5-SNAPSHOT"
        dependencies[1].minVersion == "1.2.3-SNAPSHOT"
        dependencies[1].expectedVersion == "1.2.4-SNAPSHOT"
        dependencies[1].groupAndProject == "org.s4s0l.shathel.gradle.sample:simple-project2"
    }

}
