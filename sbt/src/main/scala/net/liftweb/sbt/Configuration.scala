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

import java.net.URL
import _root_.sbt._


/**
 * Provides standard configurations for all ManagedPorjects.
 *
 * @author Indrajit Raychaudhuri
 */
protected trait Configuration extends BasicManagedProject {

  // Repositories
  // ------------
  object DownloadRepositories {
    lazy val Local   = "Local Maven2 Repository" at "file://" + Resolver.userMavenRoot
  }

  object DistributionRepositories {
    lazy val local    = Resolver.file("Local Maven Distribution Repository", Path.userHome / ".m2" / "repository" asFile)
    lazy val snapshot = "Scala-Tools Distribution Repository for Snapshots" at "http://nexus.scala-tools.org/content/repositories/snapshots/"
    lazy val release  = "Scala-Tools Distribution Repository for Releases"  at "http://nexus.scala-tools.org/content/repositories/releases/"
  }

  // Properties
  // ----------
  /**
   * Custom property format for <code>java.net.URL</code>.
   */
  implicit lazy val urlFormat = new SimpleFormat[URL] { def fromString(s: String) = new URL(s) }

  // Additional user-defined properties that optionally can be defined for the project
  lazy val projectNameFormal           = propertyOptional[String](formalizeName(name))
  lazy val projectLocation             = propertyOptional[URL](new URL("http://www.liftweb.net"), true)
  lazy val projectInceptionyear        = propertyOptional[Int](2006, true)
  lazy val projectOrganizationFormal   = propertyOptional[String]("WorldWide Conferencing, LLC", true)
  lazy val projectOrganizationLocation = propertyOptional[URL](projectLocation.value, true)

  lazy val projectLicenseName          = propertyOptional[String]("Apache License, Version 2.0", true)
  lazy val projectLicenseLocation      = propertyOptional[URL](new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"), true)
  lazy val projectLicenseDistribution  = propertyOptional[String]("repo", true)

  final val mavenLocal                 = propertyOptional[Boolean](false, true)
  final val publishRemote              = propertyOptional[Boolean](false, true)

  final val packageSkipTest            = propertyOptional[Boolean](false, true)

  /**
   * Custom flag to enable local maven repository, the system property <code>maven.local</code> if available, wins.
   */
  def addMavenLocal = systemOptional[Boolean]("maven.local", mavenLocal.value).value

  /**
   * Custom flag to enable remote publishing, the system property <code>publish.remote</code> if available, wins.
   */
  def enableRemotePublish = systemOptional[Boolean]("publish.remote", publishRemote.value).value

  /**
   * Custom flag to enable skipping test tasks during package action, the system property <code>package.skip.test</code> if available, wins.
   */
  def enablePackageSkipTest = systemOptional[Boolean]("package.skip.test", packageSkipTest.value).value

  /**
   * Test if project is a SNAPSHOT build.
   */
  def isSnapshot = version.toString.endsWith("-SNAPSHOT")

  /**
   * Formalize given project name.
   */
  protected def formalizeName(name: String): String = name.split("-").map(_.capitalize).mkString(" ")

  /**
   * Enable setting <code>offline</code> mode via system property too.
   */
  override def ivyLocalOnly = systemOptional[Boolean]("offline", offline.value).value

  override def toString = "Project: " + projectNameFormal.get.getOrElse("at " + environmentLabel)

}
