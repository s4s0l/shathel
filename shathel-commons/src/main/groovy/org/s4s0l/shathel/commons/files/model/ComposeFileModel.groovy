package org.s4s0l.shathel.commons.files.model

import org.yaml.snakeyaml.Yaml

/**
 * @author Matcin Wielgus
 */
class ComposeFileModel {
    final Object parsedYml;

    static ComposeFileModel load(File f) {
        return new SolutionFileModel(new Yaml().load(f.text))
    }

    ComposeFileModel(Object parsedYml) {
        this.parsedYml = parsedYml;

    }

    static void dump(ComposeFileModel model, File f) {
        f.text = new Yaml().dump(model.parsedYml);
    }
}
