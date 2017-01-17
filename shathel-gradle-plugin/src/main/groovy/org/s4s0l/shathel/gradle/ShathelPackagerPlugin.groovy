package org.s4s0l.shathel.gradle


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Zip
import org.s4s0l.gradle.bootcker.BootckerComposePreparator
import org.s4s0l.gradle.bootcker.ComposeFile
import org.s4s0l.gradle.bootcker.ComposeFilesContainer
import org.s4s0l.shathel.commons.files.model.ShathelStackFileModel
import org.yaml.snakeyaml.Yaml

/**
 * @author Matcin Wielgus
 */
class ShathelPackagerPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create('shathel', ShathelPackagerExtension)

        project.configurations.create('shathel')

        def shtlStackModel = new ShathelStackFileModel(new Yaml().load(project.file('./src/main/shathel/shthl-stack.yml').text));
        shtlStackModel.dependencies.each { d ->
            project.dependencies.shathel group: d.group, name: d.name, version: d.version, classifier: 'shathel', ext: 'zip'
        }


        project.with {
            apply plugin: 'maven'
            def shtTemporaryDirectory = new File(buildDir, 'shtTemporary/package')

            task([type: Copy], 'shtPrepareSources') {
                from 'src/main/shathel'
                exclude 'stack/docker-compose.yml'
                exclude 'shthl-stack.yml'
                into shtTemporaryDirectory

            }

            task([dependsOn: 'shtPrepareSources'], 'shtProcessSources') {
                doLast {
                    //bootcker magic
                    def preparator = new BootckerComposePreparator(project, 'shtTemporary/package/stack')
                    Map<ComposeFile, String> prepare = preparator
                            .prepare(new SingleFileContainer(project,
                            'src/main/shathel/stack/docker-compose.yml')
                            .getExistingComposeFiles())
                    assert prepare.size() == 1

                    //make sure version and project name are same as project
                    def model = new ShathelStackFileModel(new Yaml().load(project.file('./src/main/shathel/shthl-stack.yml').text))
                    model.name = "${project.group}:${project.name}".toString()
                    model.version = project.version
                    new File(shtTemporaryDirectory, 'shthl-stack.yml').text = new Yaml().dump(model.parsedYml)
                }
            }

            task([type: Zip, dependsOn: 'shtProcessSources'], 'shtAssemble') {
                classifier = 'shathel'
                from shtTemporaryDirectory
                destinationDir file('build/libs')
                eachFile { FileCopyDetails details ->
                }
            }


            task([type: Copy, dependsOn: shtAssemble], 'shtCollectDependencies') {
                from configurations['shathel']
                from shtAssemble
                into new File(buildDir, 'shtTemporary/dependencies')
                rename '(.*-)([0-9]{8}\\.[0-9]{6}-[0-9]+)-shathel.zip', '$1SNAPSHOT-shathel.zip'
            }

            assemble.dependsOn shtAssemble
            artifacts {
                archives shtAssemble
            }
        }
    }
}

class SingleFileContainer implements ComposeFilesContainer {
    final Project p;
    final String composeFile;

    SingleFileContainer(Project p, String composeFile) {
        this.p = p
        this.composeFile = composeFile
    }

    @Override
    Map<String, String> getDeclaredComposeFiles() {
        return [main: composeFile]
    }

    @Override
    Project getProject() {
        return p
    }
}