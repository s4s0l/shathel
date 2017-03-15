package org.s4s0l.shathel.commons.core.model

import com.google.common.collect.ImmutableMap
import org.yaml.snakeyaml.Yaml

/**
 * @author Marcin Wielgus
 */
class SolutionFileModel {
    final Object parsedYml;

    static SolutionFileModel load(File f) {
        return new SolutionFileModel(new Yaml().load(f.text))
    }

    static SolutionFileModel empty() {
        return new SolutionFileModel(ImmutableMap.<String, Integer> builder().
                put("version", "1").
                build())
    }

    SolutionFileModel(Object parsedYml) {
        this.parsedYml = parsedYml;         \
                 if (this.parsedYml.version != 1) {
            throw new RuntimeException("Invalid stack version number")
        }
    }

    Map<String, String> getEnvironment(String name) {
        def e = parsedYml['shathel-solution']['environments'][name];
        def params = [
                name: name,
                type: e.type.toString() //dummy npe based verification
        ] << e;
        params.collectEntries {
            [(it.key): it.value?.toString()]
        }
    }

    String getName() {
        parsedYml['shathel-solution']['name']
    }

    Set<String> getEnvironments() {
        return parsedYml['shathel-solution']['environments'].keySet();
    }

    Map<String, Map<String, String>> getStack(String name) {
        def stack = parsedYml['shathel-solution'][name] ?: [:]
        if (stack['envs'] == null) {
            stack['envs'] = [:]
        }
        return ['envs'  : stack['envs'],
                'params': stack.findAll { it.key != 'envs' }]
    }

    Map<String, String> getEnvs() {
        return parsedYml['shathel-solution']['envs'] ?: [:]
    }
}
