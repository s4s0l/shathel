package org.s4s0l.shathel.commons.core.model

import org.yaml.snakeyaml.Yaml

/**
 * @author Matcin Wielgus
 */
class StackFileModel implements Cloneable {

    final Object parsedYml;

    static StackFileModel load(File f) {
        return new StackFileModel(new Yaml().load(f.text))
    }

    static void dump(StackFileModel model, File f) {
        f.text = new Yaml().dump(model.parsedYml);
    }

    StackFileModel(Object parsedYml) {
        this.parsedYml = parsedYml;         \
                 if (this.parsedYml.version != 1) {
            throw new RuntimeException("Invalid stack version number")
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        def yaml = new Yaml()
        return new StackFileModel(yaml.load(yaml.dump(parsedYml)));
    }

    def setGav(String gav) {
        parsedYml['shathel-stack']['gav'] = gav
    }

    String getGav() {
        parsedYml['shathel-stack']['gav']
    }


    String getName() {
        GavUtils.getName(parsedYml['shathel-stack']['gav'])
    }


    String getGroup() {
        GavUtils.getGroup(parsedYml['shathel-stack']['gav'])
    }

    String getVersion() {
        GavUtils.getVersion(parsedYml['shathel-stack']['gav'])
    }


    String getDeployName() {

        (parsedYml['shathel-stack']['deployName']) ?: getName();
    }

    Collection<Map<String, String>> getDependencies() {
        def deps = parsedYml['shathel-stack'].dependencies
        deps.collect {
            kv ->
                [
                        group     : GavUtils.getGroup(kv.key),
                        name      : GavUtils.getName(kv.key),
                        version   : GavUtils.getVersion(kv.key),
                        minVersion: kv.value?.min,
                        maxVersion: kv.value?.max
                ]

        }
    }

    Collection<Map<String, String>> getEnrichers() {
        def deps = parsedYml['shathel-stack'].enrichers
        deps.collect {
            kv ->
                [
                        name  : kv.key,
                        target: kv.value?.target ?: 'DEPS',
                        type  : kv.value?.type ?: 'groovy',
                        inline: kv.value?.inline,

                ]

        }
    }


}
