package org.s4s0l.shathel.gradle


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Zip
import org.s4s0l.gradle.bootcker.BootckerComposePreparator
import org.s4s0l.gradle.bootcker.ComposeFile
import org.s4s0l.gradle.bootcker.ComposeFilesContainer
import org.yaml.snakeyaml.Yaml

/**
 * @author Matcin Wielgus
 */
class ShathelPackagerPlugin implements Plugin<Project> {

    def getLocalProjectNameFromGav(String gav) {
        def of = gav.lastIndexOf(":")
        def ret = gav.substring(0, of)
        ret.startsWith(":") ? ret : ":$ret"

    }

    @Override
    void apply(Project project) {
//        project.extensions.create('shathel', ShathelPackagerExtension)

        project.configurations.create('shathel')

        def shtlStackModel = new Yaml().load(project.file('./src/main/shathel/shthl-stack.yml').text)
        def deps = shtlStackModel['shathel-stack'].dependencies.collect {
            kv ->
                def splitted = kv.key.split(':')
                splitted = splitted.length == 2 ? "org.s4s0l.shathel:${kv.key}".split(":") : splitted
                [
                        group     : splitted[0],
                        name      : splitted[1],
                        version   : splitted[splitted.length - 1],
                        minVersion: kv.value?.min,
                        maxVersion: kv.value?.max,
                        gav       : kv.key
                ]

        }
        deps.each { d ->
            if (d.version == '$version' && project.findProject(getLocalProjectNameFromGav(d.gav))) {
                project.dependencies {
                    shathel project.dependencies.project(path: getLocalProjectNameFromGav(d.gav), configuration: 'shathel')
                }
            } else {
                project.dependencies.shathel group: d.group, name: d.name, version: d.version, classifier: 'shathel', ext: 'zip'
            }
        }


        project.with {
            apply plugin: 'maven'
            def shtTemporaryDirectory = new File(buildDir, 'shtTemporary/package')



            task('shtProcessSources') {
                doLast {
                    //bootcker magic
                    def preparator = new BootckerComposePreparator(project, 'shtTemporary/package/stack')
                    Map<ComposeFile, String> prepare = preparator
                            .prepare(new SingleFileContainer(project,
                            'src/main/shathel/stack/docker-compose.yml')
                            .getExistingComposeFiles())
                    assert prepare.size() == 1

                    //make sure version and project name are same as project
                    def parsedYml = new Yaml().load(project.file('./src/main/shathel/shthl-stack.yml').text)
                    parsedYml['shathel-stack']['gav'] = "${project.group}:${project.name}:${project.version}".toString()
                    if(shtlStackModel['shathel-stack'].dependencies!=null){
                        parsedYml['shathel-stack'].dependencies = shtlStackModel['shathel-stack'].dependencies.collectEntries {
                            [(it.key.replace('$version', project.version)): it.value]
                        }
                    }
                    new File(shtTemporaryDirectory, 'shthl-stack.yml').text = new Yaml().dump(parsedYml)
                }
            }

            task([type: Copy,dependsOn: shtProcessSources], 'shtPrepareSources') {
                from 'src/main/shathel'
                include '**/*'
                exclude 'stack/docker-compose.yml'
                exclude 'shthl-stack.yml'
                into shtTemporaryDirectory
            }

            task([type: Zip, dependsOn: 'shtPrepareSources'], 'shtAssemble') {
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
                shathel shtAssemble
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