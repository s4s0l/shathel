package org.s4s0l.shathel.commons.core.model

import org.s4s0l.shathel.commons.core.Parameters
import org.yaml.snakeyaml.Yaml

/**
 * @author Marcin Wielgus
 */
class DefaultSolutiuonFileProvider {

    String getDefaultConfig(String projectName, Parameters parameters) {
        def model = new Yaml().load(getClass().getResource("/default-shathel-solution.yml").text);

        def solution = model['shathel-solution']

        def allParamNames = parameters.allParameters

        allParamNames.findAll { it.startsWith("shathel.solution.") }.each {
            solution[it - "shathel.solution."] = parameters.getParameter(it).orElseThrow {
                new RuntimeException("WTF?")
            }
        }
        solution.name = projectName

        def envs = solution['environments']

        allParamNames.findAll {
            it.matches("shathel\\.env\\.[a-zA-Z0-1\\-_]+\\.type")
        }
        .each {
            def envName = it =~ /shathel\.env\.([a-zA-Z0-1\-_]+)\.type/
            envName = envName[0][1]
            if (envs[envName] == null) {
                envs[envName] = [:]
            }
        }

        envs.keySet().each { String env ->
            allParamNames.findAll {
                it.startsWith("shathel.env.$env.") &&
                        !it.toLowerCase().contains("password") &&
                        !it.toLowerCase().endsWith("_secret_value")
            }.each {
                envs[env][it - "shathel.env.$env."] = parameters.getParameter(it).orElseThrow {
                    new RuntimeException("WTF?!")
                }
            }
        }
        return new Yaml().dump(model)
    }
}
