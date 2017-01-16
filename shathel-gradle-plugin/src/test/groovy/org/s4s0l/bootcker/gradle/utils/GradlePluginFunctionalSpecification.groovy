package org.s4s0l.bootcker.gradle.utils

import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GradleVersion
import org.junit.Rule
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestName
import org.junit.rules.TestRule
import spock.lang.Specification

/**
 * Contents of this composeConfig are heavily based on/inspired by FunctionalSpec.groovy from
 * https://github.com/groovy/groovy-android-gradle-plugin project.
 * What I changed:
 *   - added default properties passed to gradle
 *   - temporary folder replaced with one that does not disappear
 *   - running gradle project from folder not only inlined files
 *
 *
 * @author Marcin Wielgus <mwielgus@outlook.com>
 * @see <a href="https://github.com/groovy/groovy-android-gradle-plugin/blob/5dfe5c1be565886fd6f5c1478d86ccaa4183447d/groovy-android-gradle-plugin/src/test/groovy/groovyx/functional/FunctionalSpec.groovy">Original stuff</a>
 */
abstract class GradlePluginFunctionalSpecification extends Specification {

    TestName name = new TestName();
    TemporaryDirectory projectDirectory = new TemporaryDirectory(name);
    @Rule
    public TestRule chain = RuleChain
            .outerRule(name)
            .around(projectDirectory);

    File getBuildFile() {
        return makeFile('build.gradle')
    }

    File makeFile(String path) {
        def f = file(path)
        if (!f.exists()) {
            f.parentFile.mkdirs()
            f.createNewFile()
        }
        return f
    }


    void useProjectStructure(String directory) {
        useProjectStructure(new File(directory))
    }

    void useProjectStructure(File directory) {
        FileUtils.copyDirectory(directory, projectDirectory.root)
    }

    File file(String path) {
        def file = new File(projectDirectory.root, path)
        assert file.parentFile.mkdirs() || file.parentFile.exists()
        return file
    }

    File findFile(String path) {
        def file = new File(projectDirectory.root, path)
        def regexprName = file.getName();
        def files = file.parentFile.listFiles(new FilenameFilter() {
            @Override
            boolean accept(File dir, String n) {
                (n =~ regexprName).matches()
            }
        })

        assert  files.size() == 1
        file = files[0]

        assert file.parentFile.mkdirs() || file.parentFile.exists()
        return file
    }

    GradleRunner runner(String gradleVersion, String... args) {
        def allArgs = new ArrayList();
        allArgs.add("-Dbootcker_localrepo=" + getLocalRepo().getAbsolutePath());
        allArgs.add("-Dbootcker_project_version=" + System.getenv("PROJECT_VERSION"));
        allArgs.add("--stacktrace")
        allArgs.addAll(args.toList())
        return GradleRunner.create()
                .withProjectDir(projectDirectory.root)
                .withDebug(true) // always run inline to save memory, especially on CI
                .forwardOutput()
                .withTestKitDir(getTestKitDir())
                .withArguments(allArgs)
                .withGradleVersion(gradleVersion ?: GradleVersion.current().version)
    }

    BuildResult runWithVersion(String gradleVersion, String... args) {
        runner(gradleVersion, args).build()
    }

    BuildResult run(String... args) {
        runner(null, args).build()
    }

    private static File getTestKitDir() {
        def gradleUserHome = System.getenv('GRADLE_USER_HOME')
        if (!gradleUserHome) {
            gradleUserHome = new File(System.getProperty('user.home'), '.gradle').absolutePath
        }
        return new File(gradleUserHome, 'testkit')
    }

    File getLocalRepo() {
        new File(System.getenv('LOCAL_REPO'))
    }

    String getRootProjectName() {
        return projectDirectory.projectName
    }
}


class TemporaryDirectory extends ExternalResource {
    private final TestName name;
    private final File parentFolder;
    File root;
    String projectName;

    TemporaryDirectory(TestName name) {
        this(new File(System.getenv("LOCAL_BUILD_DIR"), "functionalTestTemp"), name);
    }

    TemporaryDirectory(File parentFolder, TestName name) {
        this.parentFolder = parentFolder;
        this.name = name
    }

    @Override
    protected void before() throws Throwable {
        projectName = name.methodName.replaceAll("\\s", "_")
        projectName = projectName.substring(0, Math.min(16, projectName.length()))

        root = new File(parentFolder, projectName)
        if (root.exists()) {
            delete()
        }
        root.mkdirs()

    }

    void delete() {
        if (root != null) {
            recursiveDelete(root);
        }
    }

    private void recursiveDelete(File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File each : files) {
                recursiveDelete(each);
            }
        }
        file.delete();
    }
}