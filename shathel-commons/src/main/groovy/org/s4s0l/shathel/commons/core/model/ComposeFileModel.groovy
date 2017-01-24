package org.s4s0l.shathel.commons.core.model

import org.yaml.snakeyaml.Yaml

/**
 * @author Matcin Wielgus
 */
class ComposeFileModel {
    final Object parsedYml;

    static ComposeFileModel load(File f) {
        return load(f.text)
    }

    static ComposeFileModel load(String f) {
        return new ComposeFileModel(new Yaml().load(f))
    }

    ComposeFileModel(Object parsedYml) {
        this.parsedYml = parsedYml;

    }


    String getVersion() {
        return parsedYml['version']
    }

    void setVersion(String version) {
        parsedYml['version'] = version
    }

    static void dump(ComposeFileModel model, File f) {
        f.text = new Yaml().dump(model.parsedYml);
    }

    void addLabelToServices(String key, String value) {
        parsedYml.services?.each {
            if (it.value.labels == null) {
                it.value.labels = [:]
            }
            it.value.labels << [(key): value]
            if (it.value.deploy == null) {
                it.value.deploy = [:]
            }
            if (it.value.deploy.labels == null) {
                it.value.deploy.labels = [:]
            }
            it.value.deploy.labels << [(key): value]

        }
    }

    /**
     * adds external network to all services
     * @param networkName
     */
    void addExternalNetwork(String networkName) {
        parsedYml.services?.each {
            if (it.value.networks == null) {
                it.value.networks = [networkName]
            } else {
                it.value.networks << networkName
            }
        }
        if (parsedYml.networks == null) {
            parsedYml.networks = [:]
        }
        parsedYml.networks << [(networkName): [external: true]]
    }
}
