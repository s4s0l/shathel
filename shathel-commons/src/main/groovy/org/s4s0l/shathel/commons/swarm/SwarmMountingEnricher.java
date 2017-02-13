package org.s4s0l.shathel.commons.swarm;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.s4s0l.shathel.commons.core.environment.EnricherExecutor;
import org.s4s0l.shathel.commons.core.environment.EnvironmentApiFacade;
import org.s4s0l.shathel.commons.core.environment.EnvironmentContext;
import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.scripts.Executor;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Matcin Wielgus
 */
public class SwarmMountingEnricher extends EnricherExecutor {
    private final SwarmClusterWrapper swarmClusterWrapper;
    private static final Logger LOGGER = getLogger(SwarmMountingEnricher.class);

    public SwarmMountingEnricher(SwarmClusterWrapper swarmClusterWrapper) {
        this.swarmClusterWrapper = swarmClusterWrapper;
    }

    @Override
    protected List<Executor> executeProvidingProvisioner(EnvironmentContext environmentContext, EnvironmentApiFacade apiFacade,
                                                         StackDescription stack, ComposeFileModel model) {
        List<Executor> execs = new ArrayList<>();
        model.mapMounts((service, volume) -> {
            if (volume.startsWith("./")) {
                String toPath = swarmClusterWrapper.getDataDirectory() + "/" + stack.getReference().getSimpleName() + "-" + service + "/" + getDirectory(volume);
                String[] split = volume.split(":");
                String remotePart = split[1];
                File file = new File(stack.getStackResources().getComposeFileDirectory(), split[0]);
                final File directoryToCopy;
                String resultingMount = toPath + ":" + remotePart;
                if(file.isFile()){
                    resultingMount = toPath + "/" + file.getName()+ ":" + remotePart;
                    directoryToCopy = file.getParentFile();
                }else{
                    directoryToCopy = file;
                }
                execs.add(context -> {
                    prepareMounts(directoryToCopy, toPath);
                    return "ok";
                });
                return resultingMount;
            } else {
                return volume;
            }
        });
        return execs;
    }


    private String getDirectory(String volume) {
        Matcher matcher = Pattern.compile("\\./([^/]+)/?.*").matcher(volume);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }

    private void prepareMounts(File fromDirectory, String toRemotePath) {
        //ALL nodes must be running!!!!
        Path basePath = Paths.get(fromDirectory.getAbsolutePath());
        List<String> nodeNames = swarmClusterWrapper.getNodeNames();
        LOGGER.debug("Moving {} to {} on remotes", fromDirectory.getAbsolutePath(), toRemotePath);
        for (String nodeName : nodeNames) {
            Iterator<File> fileIterator = FileUtils.iterateFilesAndDirs(fromDirectory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

            swarmClusterWrapper.sudo(nodeName, "mkdir -p " + toRemotePath);
            swarmClusterWrapper.sudo(nodeName, "chown -R " + swarmClusterWrapper.getNonRootUser() + " " + toRemotePath);
            while (fileIterator.hasNext()) {
                File next = fileIterator.next();
                String relative = basePath.relativize(Paths.get(next.getAbsolutePath())).toString();
                if (next.isDirectory()) {
                    LOGGER.debug("{} Making dir {}", nodeName, toRemotePath + "/" + relative);
                    swarmClusterWrapper.ssh(nodeName, "mkdir -p " + toRemotePath + "/" + relative);
                }
                if (next.isFile()) {
                    LOGGER.debug("{} Scp {} to {}", nodeName, next.getAbsolutePath(), nodeName + ":" + toRemotePath + "/" + relative);
                    swarmClusterWrapper.scp(next.getAbsolutePath(), nodeName + ":" + toRemotePath + "/" + relative);
                }
            }
            swarmClusterWrapper.sudo(nodeName, "chown -R 1000 " + toRemotePath);

        }


    }
}
