package org.s4s0l.shathel.commons.localcompose;

import org.apache.commons.io.FileUtils;
import org.s4s0l.shathel.commons.core.environment.EnricherExecutable;
import org.s4s0l.shathel.commons.core.environment.EnricherExecutableParams;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext;
import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.core.stack.StackDescription;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Marcin Wielgus
 */
public class LocalMountingEnricher extends EnricherExecutable {


    @Override
    protected void execute(EnricherExecutableParams params) {
        ComposeFileModel model = params.getModel();
        StackDescription stack = params.getStack();
        EnvironmentContext environmentContext = params.getEnvironmentContext();
        EnricherExecutableParams.Provisioners provisioners = params.getProvisioners();
        model.mapMounts((service, volume) -> {
            if (volume.startsWith("/shathel-data/")) {
                String subDirName = stack.getReference().getName() + "/" + service + "/";
                File subDir = new File(environmentContext.getDataDirectory(), subDirName);

                String pathToCreate = volume.split(":")[0].replaceAll("/shathel-data/", subDir.getAbsolutePath() + "/");
                provisioners.add("prepare-mount-dir:" + pathToCreate, context -> new File(pathToCreate).mkdirs());
                return volume.replace("/shathel-data/", subDir.getAbsolutePath() + "/");
            } else if (volume.startsWith("./")) {
                String upperDir = getDirectory(volume);
                String baseToPath = environmentContext.getDataDirectory() + "/" + stack.getReference().getSimpleName() + "/";
                String[] split = volume.split(":");

                String resultingMount = baseToPath + split[0].substring(2) + ":" + split[1];


                final File directoryToCopyFrom = new File(stack.getStackResources().getComposeFileDirectory(), upperDir);
                final File directoryToCopyTo = new File(baseToPath, upperDir);

                provisioners.add("prepare-mount-dir:" + directoryToCopyTo.getAbsolutePath(), context -> {
                    try {

                        if (directoryToCopyTo.exists()) {
                            params.getApiFacade().getDockerForManagementNode().containerCreate(
                                    "--rm -v " + directoryToCopyTo.getParentFile().getAbsolutePath() + ":/dir alpine rm -fR /dir/" + directoryToCopyTo.getName());
                        }
                        FileUtils.copyDirectory(directoryToCopyFrom, directoryToCopyTo);
                    } catch (IOException e) {
                        throw new RuntimeException("Unable to copy", e);
                    }
                });
                return resultingMount;
            } else {
                return volume;
            }
        });
    }


    private String getDirectory(String volume) {
        Matcher matcher = Pattern.compile("\\./([^/:]+)/?.*").matcher(volume);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }

}
