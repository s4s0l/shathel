package org.s4s0l.shathel.commons.swarm;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.s4s0l.shathel.commons.core.environment.EnricherExecutable;
import org.s4s0l.shathel.commons.core.environment.EnricherExecutableParams;
import org.s4s0l.shathel.commons.core.environment.ShathelNode;
import org.s4s0l.shathel.commons.core.model.ComposeFileModel;
import org.s4s0l.shathel.commons.core.stack.StackDescription;
import org.s4s0l.shathel.commons.ssh.SshOperations;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Marcin Wielgus
 */
public class SwarmMountingEnricher extends EnricherExecutable {
    private final String remoteDataDirectory;
    private final SshOperations sshOperations;

    private static final Logger LOGGER = getLogger(SwarmMountingEnricher.class);

    public SwarmMountingEnricher(String remoteDataDirectory, SshOperations sshOperations) {
        this.remoteDataDirectory = remoteDataDirectory;
        this.sshOperations = sshOperations;
    }

    @Override
    protected void execute(EnricherExecutableParams params) {
        ComposeFileModel model = params.getModel();
        StackDescription stack = params.getStack();
        EnricherExecutableParams.Provisioners provisioners = params.getProvisioners();
        model.mapMounts((service, volume) -> {
            if (volume.startsWith("./")) {
                String toPath = remoteDataDirectory + "/" + stack.getReference().getSimpleName() + "-" + service + "/" + getDirectory(volume);
                String[] split = volume.split(":");
                String remotePart = split[1];
                File file = new File(stack.getStackResources().getComposeFileDirectory(), split[0]);
                final File directoryToCopy;
                String resultingMount = toPath + ":" + remotePart;
                if (file.isFile()) {
                    resultingMount = toPath + "/" + file.getName() + ":" + remotePart;
                    directoryToCopy = file.getParentFile();
                } else {
                    directoryToCopy = file;
                }
                provisioners.add("prepare-mount-dir:" + toPath, context -> prepareMounts(context.getCurrentNodes(), directoryToCopy, toPath));
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

    private void prepareMounts(List<ShathelNode> nodeNames, File fromDirectory, String toRemotePath) {
        //ALL nodes must be running!!!!
        Path basePath = Paths.get(fromDirectory.getAbsolutePath());
        LOGGER.debug("Moving {} to {} on remotes", fromDirectory.getAbsolutePath(), toRemotePath);
        for (ShathelNode sn : nodeNames) {
            Iterator<File> fileIterator = FileUtils.iterateFilesAndDirs(fromDirectory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
            sshOperations.sudo(sn, "mkdir -p " + toRemotePath);
            sshOperations.sudo(sn, "chown -R " + sshOperations.getScpUser() + " " + toRemotePath);
            while (fileIterator.hasNext()) {
                File next = fileIterator.next();
                String relative = basePath.relativize(Paths.get(next.getAbsolutePath())).toString();
                if (next.isDirectory()) {
                    LOGGER.debug("{} Making dir {}", sn.getNodeName(), toRemotePath + "/" + relative);
                    sshOperations.ssh(sn, "mkdir -p " + toRemotePath + "/" + relative);
                }
                if (next.isFile()) {
                    LOGGER.debug("{} Scp {} to {}", sn.getNodeName(), next.getAbsolutePath(), sn.getNodeName()+ ":" + toRemotePath + "/" + relative);
                    sshOperations.scp(sn, next, toRemotePath + "/" + relative);
                }
            }
            sshOperations.sudo(sn, "chown -R 1000 " + toRemotePath);
        }
    }
}
