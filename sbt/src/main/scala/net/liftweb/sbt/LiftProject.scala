/*
 * Copyright 2011 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.liftweb.sbt

import java.util.jar.Attributes.Name._
import _root_.sbt._


/**
 * Pre-configured mixin for standard Lift based web application projects.
 */
trait LiftParentProject extends ParentProject with Credential with Dependency with Publishing {

  // Disable dependencies on sub-projects
  override def deliverProjectDependencies = Nil

}


/**
 * Pre-configured mixin for standard Lift based library projects.
 */
trait LiftDefaultProject extends DefaultProject with LiftScalaProject with Checksum with YuiCompressor {

  // Modify source jar suffix
  override def packageSrcJar = defaultJarPath("-sources.jar")

}


/**
 * Pre-configured mixin for standard Lift based web application projects.
 */
trait LiftDefaultWebProject extends DefaultWebProject with LiftWebScalaProject with WebChecksum with WebYuiCompressor {

  // Modify source jar suffix
  override def packageSrcJar = defaultJarPath("-sources.jar")

}


/**
 * Special mixin for generating aggregated scaladoc.
 *
 * <p>
 * When added as a sub-project of a <a href="LiftParentProject.html">LiftParentProject</a>, it generates
 * aggregated scaladoc for all sibling projects of type <a href="LiftDefaultProject.html">LiftDefaultProject</a>.
 * </p>
 */
trait LiftDefaultDocProject extends DefaultProject with LiftScalaProject {

  /**
   * Sibling of this project, that is, other <a href="LiftDefaultProject.html">LiftDefaultProject</a>s
   * having the same parent.
   */
  lazy val siblings =
    info.parent.get.projectClosure.flatMap {
      case c: LiftDefaultProject => Some(c)
      case _                     => None
    }

  // We modify the parameter that docAction and docTestAction takes instead of modifying the action itself
  override def mainSources  = concatPaths(siblings) { case p: ScalaPaths        => p.mainSources }
  override def testSources  = concatPaths(siblings) { case p: ScalaPaths        => p.testSources }
  override def docClasspath = concatPaths(siblings) { case p: BasicScalaProject => p.docClasspath }

  private def concatPaths[T](s: Seq[T])(f: PartialFunction[T, PathFinder]) = {
    def finder: T => PathFinder = f orElse { case _ => Path.emptyPathFinder }
    (Path.emptyPathFinder /: s) { _ +++ finder(_) }
  }

  // Nothing to compile, package, deliver or publish
  override def compileAction        = Empty
  override def testCompileAction    = Empty

  override def packageAction        = Empty
  override def packageTestAction    = Empty
  override def packageSrcAction     = Empty
  override def packageTestSrcAction = Empty

  override def publishLocalAction   = Empty
  override def deliverLocalAction   = Empty

  override def deliverAction        = Empty
  override def publishAction        = Empty

  override def makePomAction        = Empty

  // To avoid write collisions with outputDirectories of parent
  override def outputRootPath        = super.outputRootPath        / "apidoc"
  override def managedDependencyPath = super.managedDependencyPath / "apidoc"
   
}


trait LiftWebScalaProject extends BasicWebScalaProject with LiftScalaProject {

  // Initialize Boot by default
  override def consoleInit =
    """
      |import net.liftweb.common._
      |import bootstrap.liftweb.Boot
      |
      |val b = new Boot
      |b.boot
      |
    """.stripMargin

}


trait LiftScalaProject extends BasicScalaProject with Credential with Dependency with Publishing {

  // Auxillary artifacts
  // -------------------
  override def artifacts = super.artifacts ++ Seq(Artifact(artifactID, "sources"))

  // Dependencies
  // ------------
  // Add canonical test scope dependencies by default
  override def libraryDependencies = {
    import TestScope._
    super.libraryDependencies ++ Seq(specs, scalacheck)
  }

  override def managedClasspath(config: _root_.sbt.Configuration) =
    super.managedClasspath(config) filter { f => !blackListedLibs.contains(f.asFile.getName) }

  // Compile options
  // ---------------
  override def compileOptions =
    super.compileOptions ++
      compileOptions("-Xmigration", "-Xcheckinit", "-encoding", "utf8") ++
      { if (isSnapshot) Seq(ExplainTypes, Unchecked) else Seq(Optimize) }

  // Package options
  // ---------------
  override def packageOptions =
    specificationEntries :: implementationEntries :: super.packageOptions.toList

  lazy val specificationEntries =
    ManifestAttributes(
      (SPECIFICATION_TITLE,   projectNameFormal.value),
      (SPECIFICATION_VERSION, version.toString),
      (SPECIFICATION_VENDOR,  projectOrganizationFormal.value))

  lazy val implementationEntries =
    ManifestAttributes(
      (IMPLEMENTATION_TITLE,     projectNameFormal.value),
      (IMPLEMENTATION_VERSION,   version.toString),
      (IMPLEMENTATION_VENDOR_ID, organization),
      (IMPLEMENTATION_VENDOR,    projectOrganizationFormal.value),
      (IMPLEMENTATION_URL,       projectLocation.value.toString))

  // override def mainResources = super.mainResources +++ "LICENSE" +++ "NOTICE"

  // Make `package` depend on `test`
  override def packageAction = super.packageAction dependsOn { if (!enablePackageSkipTest) test else Empty }

  // Publish source packages too
  override def packageToPublishActions = super.packageToPublishActions ++ Seq(packageSrc)

  // Document options
  // ----------------
  override def documentOptions =
    documentTitle(docTitle) :: CompoundDocOption("-doc-version", version.toString) :: Nil

  lazy val docTitle = "%s %s API".format(projectNameFormal.value, version)

  // Additional system properties for convenience
  System.setProperty("derby.stream.error.file", outputPath / "derby.log" absolutePath)

}
