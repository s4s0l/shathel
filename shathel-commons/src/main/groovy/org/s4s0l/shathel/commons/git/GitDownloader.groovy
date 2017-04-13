package org.s4s0l.shathel.commons.git

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.apache.commons.io.FileUtils
import org.s4s0l.shathel.commons.core.dependencies.ReferenceResolver
import org.s4s0l.shathel.commons.core.dependencies.StackDependencyDownloader
import org.s4s0l.shathel.commons.core.dependencies.StackLocator
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.s4s0l.shathel.commons.utils.ExecWrapper
import org.s4s0l.shathel.commons.utils.Utils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Marcin Wielgus
 */
@TypeChecked
@CompileStatic
abstract class GitDownloader {
    private static final Logger LOGGER = LoggerFactory.getLogger(GitDownloader.class)

    abstract String getDefaultGroup();

    abstract String getDefaultVersion();


    Optional<File> download(StackLocator locator, File directory, boolean forceful) {
        Optional<StackReference> resolvedReference
        resolvedReference = resolveReference(locator)
        if (!resolvedReference.isPresent()) {
            return Optional.empty();
        }
        def reference = resolvedReference.get()
        if (reference.group.startsWith("git@")) {
            def ref = new GitRef(reference)
            def repoDir = new File(directory, "git/${ref.getLocalDirectoryName()}")
            def statusFile = new File(repoDir, ".git/.shathel-git-status")
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

    protected Optional<StackReference> resolveReference(StackLocator locator) {
        if (!locator.getReference().isPresent()) {
            if (locator.location.startsWith("git@")) {
                String nogitLocation = locator.location.substring(4)
                String group = getDefaultGroup()
                String version = getDefaultVersion()
                def resolve = new ReferenceResolver(group, version).resolve(new StackLocator(nogitLocation))
                if (resolve.isPresent()) {
                    return Optional.of(new StackReference("git@${resolve.get().gav}"))
                }
            }
            return Optional.empty()
        } else {
            return Optional.of(locator.getReference().get())
        }
    }


    def exec(File f, String command) {
        git().executeForOutput(f, command)
    }

    private ExecWrapper git() {
        new ExecWrapper(LOGGER, "git")
    }


}

class GitRef {
    private final StackReference reference;

    GitRef(StackReference reference) {
        this.reference = reference
    }

    String getLocalDirectoryName() {
        def group = reference.group
        return "${group - "git@"}_${simpleVersion()}".toLowerCase().replaceAll("[^A-Za-z0-9]", "_")
    }

    String remote() {
        def group = reference.group
        return "https://${group - "git@"}"
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

        return actualDir
    }

    String simpleVersion() {
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
