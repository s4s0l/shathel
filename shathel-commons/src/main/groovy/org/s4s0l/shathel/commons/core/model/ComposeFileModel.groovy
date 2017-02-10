package org.s4s0l.shathel.commons.core.model

import org.yaml.snakeyaml.Yaml

import java.util.function.BiFunction
import java.util.function.Function

/**
 * @author Matcin Wielgus
 */
class ComposeFileModel {
    Object parsedYml;

    void replaceInAllStrings(String what, String with) {
        parsedYml = replaceInAllObject(parsedYml, what, with)
    }

    Map getYml(){
        return parsedYml
    }

    private Object replaceInAllObject(Object map, String what, String with) {
        if (map instanceof String) {
            return map.replace(what, with);
        }
        if (map instanceof List) {
            return replaceInAllList(map, what, with)
        }
        if (map instanceof Map) {
            return replaceInAllMap(map, what, with)
        }
        return map;
    }

    private Map replaceInAllMap(Map map, String what, String with) {
        map.keySet().each { it ->
            map.replace(it, replaceInAllObject(map.get(it), what, with))
        }
        map
    }

    private List replaceInAllList(List map, String what, String with) {
        return map.collect {
            replaceInAllObject(it, what, with)
        }
    }

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

    void mapMounts(BiFunction<String, String, String> mapper) {
        parsedYml.services.each {
            it.value.volumes = (it.value.volumes ?: []).collect { v ->
                mapper.apply(it.key, v)
            }
            if (it.value.volumes.isEmpty()) {
                it.value.remove('volumes')
            }
        }
    }

    void mapBuilds(BiFunction<String, Map<String, Object>, String> mapper) {
        parsedYml.services.each {
            if (it.value.build != null) {
                def map = [args: [:], dockerfile: 'Dockerfile']
                if (it.value.build instanceof String) {
                    map.context = it.value.build
                } else {
                    map.context = it.value.build.context
                    map.dockerfile = it.value.build.dockerfile
                    map.args = it.value.build.args ?: [:]
                }
                def image = mapper.apply(it.key, map)
                it.value.remove('build')
                it.value.image = image
            }
        }
    }

    void mapImages(Function<String,  String> mapper) {
        parsedYml.services.each {
            it.value.image = mapper.apply(it.value.image)
        }
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
