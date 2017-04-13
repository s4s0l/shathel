package org.s4s0l.shathel.commons.core.model

import org.yaml.snakeyaml.Yaml

/**
 * @author Marcin Wielgus
 */
class EnvironmentFileModel implements Cloneable {

    Object parsedYml

    static EnvironmentFileModel load(File f) {
        return load(f.text)
    }

    static EnvironmentFileModel load(String f) {
        return new EnvironmentFileModel(new Yaml().load(f))
    }

    EnvironmentFileModel(Object parsedYml) {
        this.parsedYml = parsedYml;

    }

    String getGav() {
        parsedYml['shathel-env']['gav']
    }

    Map<String, String> getMandatoryEnvironmentVariables() {
        return parsedYml['shathel-env']['mandatoryEnvs']
    }

    String getImageUser() {
        return parsedYml['shathel-env']['user']
    }

    Map<String, String> getImagePreparationScript() {
        getScript(parsedYml['shathel-env']['phases']['image-preparation'])
    }

    Map<String, String> getInfrastructureScript() {
        getScript(parsedYml['shathel-env']['phases']['infrastructure'])
    }

    Map<String, String> getSetupScript() {
        getScript(parsedYml['shathel-env']['phases']['setup'])
    }

    Map<String, String> getSwarmScript() {
        getScript(parsedYml['shathel-env']['phases']['swarm'])
    }

    private Map<String, String> getScript(Object o) {
        return [
                name  : o.name,
                type  : o.type,
                inline: o.inline,

        ]
    }
}
