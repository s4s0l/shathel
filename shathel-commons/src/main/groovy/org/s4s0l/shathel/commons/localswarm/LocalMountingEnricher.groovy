package org.s4s0l.shathel.commons.localswarm

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.apache.commons.io.FileUtils
import org.s4s0l.shathel.commons.core.environment.EnricherExecutable
import org.s4s0l.shathel.commons.core.environment.EnricherExecutableParams
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext
import org.s4s0l.shathel.commons.core.environment.ProvisionerExecutableParams
import org.s4s0l.shathel.commons.core.model.ComposeFileModel
import org.s4s0l.shathel.commons.core.stack.StackDescription

import java.util.function.BiFunction
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author Marcin Wielgus
 */
class LocalMountingEnricher extends EnricherExecutable {


    @Override
    protected void execute(EnricherExecutableParams params) {
        ComposeFileModel model = params.getModel()
        StackDescription stack = params.getStack()
        EnvironmentContext environmentContext = params.getEnvironmentContext()
        EnricherExecutableParams.Provisioners provisioners = params.getProvisioners()
        model.mapMounts { String service, String volume ->
            if (volume.startsWith("/shathel-data/")) {
                String subDirName = stack.getReference().getName() + "/" + service + "/"
                File subDir = new File(environmentContext.getDataDirectory(), subDirName)
                String pathToCreate = volume.split(":")[0].replaceAll("/shathel-data/", subDir.getAbsolutePath() + "/")
                provisioners.add("prepare-mount-dir:" + pathToCreate, { ProvisionerExecutableParams context -> new File(pathToCreate).mkdirs() })
                return volume.replace("/shathel-data/", subDir.getAbsolutePath() + "/")
            } else if (volume.startsWith("./")) {
                String upperDir = getDirectory(volume)
                String baseToPath = environmentContext.getDataDirectory().absolutePath + "/" + stack.getReference().getSimpleName() + "/"
                String[] split = volume.split(":")

                String resultingMount = baseToPath + split[0].substring(2) + ":" + split[1]

                final File directoryToCopyTo = new File(baseToPath, upperDir)

                provisioners.add("prepare-mount-dir:" + directoryToCopyTo.getAbsolutePath(), { ProvisionerExecutableParams context ->
                    try {
                        final File directoryToCopyFrom = new File(new File(context.dir, "stack"), upperDir)
                        if (directoryToCopyTo.exists()) {
                            params.getApiFacade().getManagerNodeWrapper().containerCreateRun(
                                    "--rm -v " + directoryToCopyTo.getParentFile().getAbsolutePath() + ":/dir alpine rm -fR /dir/" + directoryToCopyTo.getName())
                        }
                        FileUtils.copyDirectory(directoryToCopyFrom, directoryToCopyTo)
                    } catch (IOException e) {
                        throw new RuntimeException("Unable to copy", e)
                    }
                })
                return resultingMount
            } else {
                return volume
            }
        } as BiFunction<String, String, String>
    }


    private String getDirectory(String volume) {
        Matcher matcher = Pattern.compile("\\./([^/:]+)/?.*").matcher(volume)
        if (matcher.matches()) {
            return matcher.group(1)
        } else {
            return ""
        }
    }

}
