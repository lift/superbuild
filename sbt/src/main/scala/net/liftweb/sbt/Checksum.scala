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

  protected def checksumAction        = checksumTask(jarPath, checksumOptions) dependsOn `package` describedAs ChecksumDescription
  protected def checksumTestAction    = checksumTask(packageTestJar, checksumOptions) dependsOn packageTest describedAs ChecksumTestDescription
  protected def checksumDocsAction    = checksumTask(packageDocsJar, checksumOptions) dependsOn packageDocs describedAs ChecksumDocsDescription
  protected def checksumSrcAction     = checksumTask(packageSrcJar, checksumOptions) dependsOn packageSrc describedAs ChecksumSrcDescription
  protected def checksumTestSrcAction = checksumTask(packageTestSrcJar, checksumOptions) dependsOn packageTestSrc describedAs ChecksumTestSrcDescription
  protected def checksumPomAction     = checksumTask(pomPath, checksumOptions) dependsOn makePom describedAs ChecksumPomDescription

  // override def makePomAction = checksumTask(pomPath, checksumOptions) :: Nil
  
  lazy val checksum        = checksumAction
  lazy val checksumTest    = checksumTestAction
  lazy val checksumDocs    = checksumDocsAction
  lazy val checksumSrc     = checksumSrcAction
  lazy val checksumTestSrc = checksumTestSrcAction
  lazy val checksumPom     = checksumPomAction

  val ChecksumDescription        = "Creates a checksum file for given jar file."
  val ChecksumTestDescription    = "Creates a checksum file for given test jar file."
  val ChecksumDocsDescription    = "Creates a checksum file for given docs jar file."
  val ChecksumSrcDescription     = "Creates a checksum file for given source jar file."
  val ChecksumTestSrcDescription = "Creates a checksum file for given test source jar file."
  val ChecksumPomDescription     = "Creates a checksum file for given pom file."

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

}
