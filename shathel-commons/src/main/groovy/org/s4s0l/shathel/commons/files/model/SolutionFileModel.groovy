package org.s4s0l.shathel.commons.files.model

import org.yaml.snakeyaml.Yaml

/**
 * @author Matcin Wielgus
 */
class SolutionFileModel {
    final Object parsedYml;

    static SolutionFileModel load(File f) {
        return new SolutionFileModel(new Yaml().load(f.text))
    }

    static SolutionFileModel empty() {
        return new SolutionFileModel(new HashMap())
    }

    SolutionFileModel(Object parsedYml) {
        this.parsedYml = parsedYml;   \
           if (this.parsedYml.version != 1) {
            throw new RuntimeException("Invalid stack version number")
        }
    }
}
