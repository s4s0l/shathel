package org.s4s0l.shathel.commons.git

import org.apache.commons.io.FileUtils
import org.s4s0l.shathel.commons.core.dependencies.DependencyDownloader
import org.s4s0l.shathel.commons.core.dependencies.StackLocator
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.commons.utils.ExecWrapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Marcin Wielgus
 */
class GitDependencyDownloader implements DependencyDownloader {
    private static
    final Logger LOGGER = LoggerFactory.getLogger(GitDependencyDownloader.class);

    @Override
    Optional<File> download(StackLocator locator, File directory, boolean forceful) {
        if (!locator.getReference().isPresent()) {
            return Optional.empty()
        }
        def reference = locator.getReference().get()
        if (reference.group.startsWith("git@")) {
            def repoDir = new File(directory, "git/${reference.getStackDirecctoryName()}")
            def statusFile = new File(repoDir, ".git/.shathel-git-status")
            def ref = new GitRef(reference)
            if (!forceful && statusFile.exists()) {
                File stackDir = ref.getDirectoryInRepo(repoDir)
                return Optional.of(stackDir)
            }
            FileUtils.deleteDirectory(repoDir)
            repoDir.mkdirs()
            exec(repoDir, "init")
            exec(repoDir, "remote add origin ${ref.remote()}")

            exec(repoDir, "fetch origin ${ref.fetchTarget()}")
            exec(repoDir, "reset --hard ${ref.resetTarget()}")
            File stackDir = ref.getDirectoryInRepo(repoDir)
            statusFile.text = "fetched"
            return Optional.of(stackDir)
        }
        return Optional.empty()
    }


    def exec(File f, String command) {
        git().executeForOutput(f, [:], command)
    }

    private ExecWrapper git() {
        new ExecWrapper(LOGGER, "git")
    }

    static class GitRef {
        private final StackReference reference;

        GitRef(StackReference reference) {
            this.reference = reference
        }

        String remote() {
            def group = reference.group
            return "https://${group-"git@"}"
        }

        String repoName() {
            reference.group.substring(reference.group.lastIndexOf("/") + 1);
        }

        File getDirectoryInRepo(File repoDir) {
            def name = repoName()
            def actualDir
            if (name == reference.name) {
                actualDir = repoDir
            } else {
                actualDir = new File(repoDir, reference.name)
            }
            def stackFile = new File(actualDir, "shthl-stack.yml")
            def replaced = stackFile.text.replace("\$version", reference.version)
            stackFile.text = replaced
            return actualDir
        }

        def simpleVersion() {
            if (reference.version.contains("@")) {
                def split = reference.version.split("@")
                return split[0]
            } else {
                return reference.version
            }
        }

        def branch() {
            if (reference.version.contains("@")) {
                def split = reference.version.split("@")
                return split[1]
            } else {
                return "master"
            }
        }

        def isHash() {
            return simpleVersion().size() == 40
        }

        def resetTarget() {
            if (isHash()) {
                return simpleVersion()
            } else {
                "FETCH_HEAD"
            }
        }

        def fetchTarget() {
            if (isHash()) {
                branch()
            } else {
                reference.version
            }
        }
    }
}
