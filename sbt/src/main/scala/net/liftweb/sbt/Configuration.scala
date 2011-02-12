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
    val local = "Local Maven Repository" at "file://" + Resolver.userMavenRoot
  }

  object DistributionRepositories {
    val local    = Resolver.file("Local Maven Distribution Repository", Path.userHome / ".m2" / "repository" asFile)
    val snapshot = "Scala-Tools Distribution Repository for Snapshots" at "http://nexus.scala-tools.org/content/repositories/snapshots/"
    val release  = "Scala-Tools Distribution Repository for Releases" at "http://nexus.scala-tools.org/content/repositories/releases/"
  }

  // Properties
  // ----------
  /** Custom property format for java.net.URL */
  implicit lazy val urlFormat = new SimpleFormat[URL] { def fromString(s: String) = new URL(s) }

  // Additional user-defined properties that optionally can be defined for the project
  lazy val projectNameFormal           = propertyOptional[String](name.split("-").map(_.capitalize).mkString(" "))
  lazy val projectLocation             = propertyOptional[URL](new URL("http://www.liftweb.net"), true)
  lazy val projectInceptionyear        = propertyOptional[Int](2006, true)
  lazy val projectOrganizationFormal   = propertyOptional[String]("WorldWide Conferencing, LLC", true)
  lazy val projectOrganizationLocation = propertyOptional[URL](projectLocation.value, true)

  lazy val projectLicenseName         = propertyOptional[String]("Apache License, Version 2.0", true)
  lazy val projectLicenseLocation     = propertyOptional[URL](new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"), true)
  lazy val projectLicenseDistribution = propertyOptional[String]("repo", true)

  /**
   * Custom flag to enable local maven repository, defaults to system property <code>maven.local</code> if available.
   */
  lazy val mavenLocal = propertyOptional[Boolean](systemOptional[Boolean]("maven.local", false).value, true)

  /**
   * Custom flag to enable remote publishing, defaults to system property <code>publish.remote</code> if available.
   */
  lazy val publishRemote = propertyOptional[Boolean](systemOptional[Boolean]("publish.remote", false).value, true)

  // Test if project is a SNAPSHOT
  def isSnapshot = version.toString.endsWith("-SNAPSHOT")

	override def toString = "Project: " + projectNameFormal.get.getOrElse("at " + environmentLabel)

}
