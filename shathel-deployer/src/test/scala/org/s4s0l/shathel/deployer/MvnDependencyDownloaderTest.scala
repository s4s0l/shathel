package org.s4s0l.shathel.deployer

import java.io.File

import org.junit.runner.RunWith
import org.s4s0l.shathel.commons.core.MapParameters
import org.s4s0l.shathel.commons.core.dependencies.StackLocator
import org.s4s0l.shathel.commons.core.stack.StackReference
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FeatureSpec, GivenWhenThen}

/**
  * @author Marcin Wielgus
  */
@RunWith(classOf[JUnitRunner])
class MvnDependencyDownloaderTest extends FeatureSpec with GivenWhenThen {

  feature("Methods tests") {

    scenario("convertFromText") {
      Given("Local repo comes from tests in plugin project")
      val params = MapParameters.builder()
        .parameter("shathel.mvn.localRepo", "src/test/resources/localRepo")
        .build()
      val downloader = new MvnDependencyDownloader(params)

      When("Requested to download")
      downloader.download(
        new StackLocator("org.s4s0l.shathel.gradle.sample:simple-project:1.2.3-SNAPSHOT"),
        new File("build/MvnDependencyDownloaderTest"), true)

      Then("Downloads")
      assert(new File("build/MvnDependencyDownloaderTest/simple-project-1.2.3-SNAPSHOT-shathel.zip").exists())


      When("Requested to download")
      downloader.download(
        new StackLocator(new StackReference("org.s4s0l.shathel.gradle.sample2:simple-project2:DEVELOPER-SNAPSHOT")),
        new File("build/MvnDependencyDownloaderTest"), true)

      Then("Downloads")
      assert(new File("build/MvnDependencyDownloaderTest/simple-project2-DEVELOPER-SNAPSHOT-shathel.zip").exists())


    }
  }

}
