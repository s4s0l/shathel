package org.s4s0l.shathel.commons.files.model

import org.yaml.snakeyaml.Yaml

/**
 * @author Matcin Wielgus
 */
class ShathelStackFileModel implements Cloneable {

    final Object parsedYml;

    static ShathelStackFileModel load(File f) {
        return new ShathelStackFileModel(new Yaml().load(f.text))
    }

    static void dump(ShathelStackFileModel model, File f) {
        f.text = new Yaml().dump(model.parsedYml);
    }

    ShathelStackFileModel(Object parsedYml) {
        this.parsedYml = parsedYml;   \
           if (this.parsedYml.version != 1) {
            throw new RuntimeException("Invalid stack version number")
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        def yaml = new Yaml()
        return new ShathelStackFileModel(yaml.load(yaml.dump(parsedYml)));
    }

    def setVersion(String version) {
        parsedYml['shathel-stack']['version'] = version
    }

    def setName(String name) {
        parsedYml['shathel-stack']['name'] = name
    }

    String getName() {
        parsedYml['shathel-stack']['name']
    }

    def setGroup(String group) {
        parsedYml['shathel-stack']['group'] = name
    }

    String getGroup() {
        parsedYml['shathel-stack']['group']
    }

    String getVersion() {
        parsedYml['shathel-stack']['version']
    }

    Collection<Map<String, String>> getDependencies() {
        def deps = parsedYml['shathel-stack'].dependencies
        deps.collect { depMap ->
            depMap.collect {
                kv ->
                    [
                            group     : kv.key.split(':')[0],
                            name      : kv.key.split(':')[1],
                            version   : kv.value['version'].expected,
                            minVersion: kv.value['version'].min,
                            maxVersion: kv.value['version'].max
                    ]

            }
        }.flatten()
    }
/**
 * @author Matcin Wielgus
 */

}
