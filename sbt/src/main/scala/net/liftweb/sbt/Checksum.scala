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

import _root_.sbt._


/**
 * Provides a simple way of doing checksum on generated artifacts. Currently supports SHA-1 only.
 *
 * @author Indrajit Raychaudhuri
 */
protected trait Checksum extends BasicScalaProject {

  trait ChecksumOption extends ActionOption
  def checksumOptions: Seq[ChecksumOption] = Nil

  def checksumPackage        = jarPath
  def checksumTestPackage    = packageTestJar
  def checksumDocsPackage    = packageDocsJar
  def checksumSrcPackage     = packageSrcJar
  def checksumTestSrcPackage = packageTestSrcJar
  def checksumProjectPackage = packageProjectZip

  object ChecksumDescriptions {
    import BasicScalaProject._
    lazy val checksumDescriptionSuffix = " Additionally, creates the corresponding checksum file."

    lazy val ChecksumDescription           = PackageDescription           + checksumDescriptionSuffix
    lazy val TestChecksumDescription       = TestPackageDescription       + checksumDescriptionSuffix
    lazy val DocChecksumDescription        = DocPackageDescription        + checksumDescriptionSuffix
    lazy val SourceChecksumDescription     = SourcePackageDescription     + checksumDescriptionSuffix
    lazy val TestSourceChecksumDescription = TestSourcePackageDescription + checksumDescriptionSuffix
    lazy val ProjectChecksumDescription    = ProjectPackageDescription    + checksumDescriptionSuffix
    lazy val PomChecksumDescription        = "Creates the pom file."      + checksumDescriptionSuffix
  }

  protected def checksumAction        = checksumTask(checksumPackage,        checksumOptions)
  protected def checksumTestAction    = checksumTask(checksumTestPackage,    checksumOptions)
  protected def checksumDocsAction    = checksumTask(checksumDocsPackage,    checksumOptions)
  protected def checksumSrcAction     = checksumTask(checksumSrcPackage,     checksumOptions)
  protected def checksumTestSrcAction = checksumTask(checksumTestSrcPackage, checksumOptions)
  protected def checksumProjectAction = checksumTask(checksumProjectPackage, checksumOptions)
  protected def checksumPomAction     = checksumTask(pomPath,                checksumOptions)

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
  override def packageAction        = checksumAction        dependsOn super.packageAction        describedAs ChecksumDescription
  override def packageTestAction    = checksumTestAction    dependsOn super.packageTestAction    describedAs TestChecksumDescription
  override def packageDocsAction    = checksumDocsAction    dependsOn super.packageDocsAction    describedAs DocChecksumDescription
  override def packageSrcAction     = checksumSrcAction     dependsOn super.packageSrcAction     describedAs SourceChecksumDescription
  override def packageTestSrcAction = checksumTestSrcAction dependsOn super.packageTestSrcAction describedAs TestSourceChecksumDescription
  override def packageProjectAction = checksumProjectAction dependsOn super.packageProjectAction describedAs ProjectChecksumDescription
  override def makePomAction        = checksumPomAction     dependsOn super.makePomAction        describedAs PomChecksumDescription

  // Add the corresponding checksum artifacts to the project
  // TODO: The `type` should have been "sha1" ideally. But keep it this way if you are using SBT 0.75.RC0 or lower.
  override def artifacts = {
    val sa = super.artifacts
    sa ++ sa.map(a => Artifact(a.name, a.`type`, a.extension + ".sha1", a.classifier, Nil, None))
  }

}


protected trait WebChecksum extends BasicWebScalaProject with Checksum {
  override def checksumPackage = warPath
}
