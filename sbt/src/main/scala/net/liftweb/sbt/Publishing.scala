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
 * Provides publishing configurations for Lift based projects.
 *
 * @author Indrajit Raychaudhuri
 */
protected trait Publishing extends BasicManagedProject with Configuration {

  // Repository options
  // ------------------
  override def repositories = {
    import scala.collection.mutable.Set
    val extras: Set[Resolver] = Set.empty
    if (isSnapshot) extras += ScalaToolsSnapshots
    if (addMavenLocal) extras += DownloadRepositories.Local
    super.repositories ++ extras
  }

  // Publish options
  // ---------------
  override def defaultPublishRepository = {
    import DistributionRepositories._
    if (!enableRemotePublish) Some(local)
    else Some(if (isSnapshot) snapshot else release)
  }

  override def managedStyle = ManagedStyle.Maven

  // Make deliverAction in a downstream project resolve dependency to upstream project conveniently
  override def publishAction = super.publishAction dependsOn publishLocal

  // Add all configurations, not just the public ones
  // TODO: Eventually, we would be fine having just compile, provided, runtime and optional
  override def makePomConfiguration = 
    new MakePomConfiguration(
      deliverProjectDependencies, 
      Some(Configurations.defaultMavenConfigurations), 
      pomExtra, pomPostProcess, pomIncludeRepository) 

  override def pomExtra =
    super.pomExtra ++
      <name>{projectNameFormal.value}</name>
      <url>{projectLocation.value}</url>
      <inceptionYear>{projectInceptionyear.value}</inceptionYear>
      <organization>
        <name>{projectOrganizationFormal.value}</name>
        <url>{projectOrganizationLocation.value}</url>
      </organization>
      <licenses>
        <license>
          <name>{projectLicenseName.value}</name>
          <url>{projectLicenseLocation.value}</url>
          <distribution>{projectLicenseDistribution.value}</distribution>
          <comments>{projectNameFormal.value} is licensed under {projectLicenseName.value}</comments>
        </license>
      </licenses>

  // TODO:
  // override def pomPostProcess(pom: Node): Node

}
