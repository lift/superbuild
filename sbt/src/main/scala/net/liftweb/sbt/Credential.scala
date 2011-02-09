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
 * Provides credentials for publishing to remote repositories.
 *
 * <p>
 * It first tries <code><a href="#ivyCredentials">ivyCredentials</a></code>, if the file is present and readable,
 * <code><a href="#mavenCredentials">mavenCredentials</a></code> otherwise.
 * </p>
 *
 * <ul>
 * <li><code>ivyCredentials</code> defaults to <code>~/.ivy2/.scalatools.credentials</code>.</li>
 * <li><code>mavenCredentials</code> defaults to <code>~/.m2/settings.xml</code>.</li>
 * </ul>
 *
 * @author Indrajit Raychaudhuri
 */
protected trait Credential extends BasicManagedProject {

  lazy val ivyCredentials   = Path.userHome / ".ivy2" / ".scalatools.credentials"
  lazy val mavenCredentials = Path.userHome / ".m2" / "settings.xml"

  lazy val scalaTools = ("Sonatype Nexus Repository Manager", "nexus.scala-tools.org")

  (ivyCredentials.asFile, mavenCredentials.asFile) match {
    case(ivy, _) if ivy.canRead =>
      log.debug("Loading credentials from %s".format(ivy))
      Credentials(ivy, log)
    case(_, mvn) if mvn.canRead =>
      log.debug("Loading credentials from %s".format(mvn))
      loadMavenCredentials(mvn)
    case _ =>
      log.warn("Could not read any of the settings files %s or %s".format(ivyCredentials, mavenCredentials))
  }

  protected def loadMavenCredentials(file: java.io.File) {
    try {
      xml.XML.loadFile(file) \ "servers" \ "server" foreach(s => {
        val host = (s \ "id").text
        val realm = if (host == scalaTools._2) scalaTools._1 else "Unknown"
        Credentials.add(realm, host, (s \ "username").text, (s \ "password").text)
      })
    } catch {
      case e => log.warn("Could not read the settings file %s [%s]".format(file, e.getMessage))
    }
  }

}
