package org.s4s0l.shathel.commons.model

import org.yaml.snakeyaml.Yaml

/**
 * @author Matcin Wielgus
 */
class ShathelStackFileModel {

    final Object parsedYml;


    ShathelStackFileModel(Object parsedYml) {
        this.parsedYml = parsedYml; \
         if (this.parsedYml.version != 1) {
            throw new RuntimeException("Invalid stack version number")
        }
    }


    def setVersion(String version){
        parsedYml['shathel-stack']['version'] = version
    }

    def setName(String name){
        parsedYml['shathel-stack']['name'] = name
    }

    Collection<Dependency> getDependencies() {
        def deps = parsedYml['shathel-stack'].dependencies


        deps.collect { depMap ->
                depMap.collect {
                    kv-> new Dependency(
                            groupAndProject:kv.key,
                            expectedVersion:kv.value['version'].expected,
                            minVersion: kv.value['version'].min,
                            maxVersion: kv.value['version'].max
                    )

                }
             }.flatten()
    }

}
