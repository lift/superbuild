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
 * Provides a simple way of doing checksum on generated artifacts. Currently supports SHA-1 only.
 *
 * @author Indrajit Raychaudhuri
 */
protected trait Checksum extends BasicScalaProject {

  trait ChecksumOption extends ActionOption
  def checksumOptions: Seq[ChecksumOption] = Nil

  object ChecksumDescriptions {
    import BasicScalaProject._
    val ChecksumDescription           = PackageDescription + checksumDescriptionSuffix
    val TestChecksumDescription       = TestPackageDescription + checksumDescriptionSuffix
    val DocChecksumDescription        = DocPackageDescription + checksumDescriptionSuffix
    val SourceChecksumDescription     = SourcePackageDescription + checksumDescriptionSuffix
    val TestSourceChecksumDescription = TestSourcePackageDescription + checksumDescriptionSuffix
    val ProjectChecksumDescription    = ProjectPackageDescription + checksumDescriptionSuffix
    val PomChecksumDescription        = "Creates the pom file." + checksumDescriptionSuffix

    val checksumDescriptionSuffix = " Additionally, creates the corresponding checksum file."
  }

  protected def checksumAction        = checksumTask(jarPath, checksumOptions)
  protected def checksumTestAction    = checksumTask(packageTestJar, checksumOptions)
  protected def checksumDocsAction    = checksumTask(packageDocsJar, checksumOptions)
  protected def checksumSrcAction     = checksumTask(packageSrcJar, checksumOptions)
  protected def checksumTestSrcAction = checksumTask(packageTestSrcJar, checksumOptions)
  protected def checksumProjectAction = checksumTask(packageProjectZip, checksumOptions)
  protected def checksumPomAction     = checksumTask(pomPath, checksumOptions)

  def checksumTask(artifactPath: => Path, options: ChecksumOption*): Task =
    checksumTask(artifactPath, options)

  def checksumTask(artifactPath: => Path, options: => Seq[ChecksumOption]): Task =
    checksumTask(Path.fromFile(artifactPath.asFile.getParentFile), artifactPath.name, options)

  def checksumTask(outputDirectory: Path, artifactName: => String, options: ChecksumOption*): Task =
    checksumTask(outputDirectory, artifactName, options)  

  def checksumTask(outputDirectory: Path, artifactName: => String, options: => Seq[ChecksumOption]): Task =
    checksumTask(outputDirectory / artifactName, outputDirectory / (artifactName + ".sha1"), options)

  def checksumTask(artifactPath: => Path, hashPath: => Path, options: ChecksumOption*): Task =
    checksumTask(artifactPath, hashPath, options)

  def checksumTask(artifactPath: => Path, hashPath: => Path, options: => Seq[ChecksumOption]): Task =
    fileTask("checksum", hashPath from artifactPath) {
      val cs = Hash.toHex(Hash(artifactPath, log).right.get)
      FileUtilities.write(hashPath.asFile, cs, log)
    }

  import ChecksumDescriptions._
  // Inject checksum actions after corresponding package action
  override def packageAction        = checksumAction dependsOn super.packageAction describedAs ChecksumDescription
  override def packageTestAction    = checksumTestAction dependsOn super.packageTestAction describedAs TestChecksumDescription
  override def packageDocsAction    = checksumDocsAction dependsOn super.packageDocsAction describedAs DocChecksumDescription
  override def packageSrcAction     = checksumSrcAction dependsOn super.packageSrcAction describedAs SourceChecksumDescription
  override def packageTestSrcAction = checksumTestSrcAction dependsOn super.packageTestSrcAction describedAs TestSourceChecksumDescription
  override def packageProjectAction = checksumProjectAction dependsOn super.packageProjectAction describedAs ProjectChecksumDescription
  override def makePomAction        = checksumPomAction dependsOn super.makePomAction describedAs PomChecksumDescription

  // Add the corresponding checksum artifacts to the project
	override def artifacts = {
	  val sha = managedStyle match {
      case ManagedStyle.Maven => Seq(Artifact(artifactID, "sha1", "jar.sha1"), Artifact(artifactID, "sha1", "pom.sha1"))
      case _                  => Seq(Artifact(artifactID, "sha1", "jar.sha1"))
    }
    super.artifacts ++ sha
  }

}
