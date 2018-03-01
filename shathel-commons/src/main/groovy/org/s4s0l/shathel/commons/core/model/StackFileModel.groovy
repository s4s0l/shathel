package org.s4s0l.shathel.commons.core.model

import org.yaml.snakeyaml.Yaml

/**
 * @author Marcin Wielgus
 */
class StackFileModel implements Cloneable {

    final Object parsedYml

    static StackFileModel load(File f) {
        return new StackFileModel(new Yaml().load(f.text))
    }

    static void dump(StackFileModel model, File f) {
        f.text = new Yaml().dump(model.parsedYml)
    }

    StackFileModel(Object parsedYml) {
        this.parsedYml = parsedYml;              \
                      if (this.parsedYml.version != 1) {
            throw new RuntimeException("Invalid stack version number")
        }
    }

    @Override
    Object clone() throws CloneNotSupportedException {
        def yaml = new Yaml()
        return new StackFileModel(yaml.load(yaml.dump(parsedYml)))
    }

    def setGav(String gav) {
        parsedYml['shathel-stack']['gav'] = gav
    }

    String getGav() {
        parsedYml['shathel-stack']['gav'].replace('$version',"UNKNOWN")
    }


    String getDeployName() {

        parsedYml['shathel-stack']['deployName']
    }

    Collection<Map<String, Object>> getDependencies() {
        def deps = parsedYml['shathel-stack'].dependencies
        deps.collect {
            kv ->
                [
                        gav       : kv.key.replace('$version',"UNKNOWN"),
                        minVersion: kv.value?.min,
                        maxVersion: kv.value?.max,
                        optional  : kv.value?.optional ?: false,
                        envs      : kv.value?.envs ?: [:],
                ]

        }
    }


    Collection<Map<String, String>> getEnrichers() {
        def deps = parsedYml['shathel-stack'].enrichers
        if (deps == null)
            return Collections.emptyList()
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


    Collection<Map<String, String>> getPreProvisioners() {
        def deps = parsedYml['shathel-stack']['pre-provisioners']
        return collectProvisioners(deps)
    }

    Collection<Map<String, String>> getPostProvisioners() {
        def deps = parsedYml['shathel-stack']['post-provisioners']
        return collectProvisioners(deps)
    }

    private List<? extends Map<String, String>> collectProvisioners(deps) {
        if (deps == null)
            return Collections.emptyList()
        deps.collect {
            kv ->
                [
                        name  : kv.key,
                        type  : kv.value?.type ?: 'groovy',
                        inline: kv.value?.inline,

                ]

        }
    }

    Map<String, String> getMandatoryEnvs() {
        parsedYml['shathel-stack']['mandatoryEnvs'] ?: [:]
    }
}
