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
 * Provides centralized access all the common Dependencies used by Lift or a Lift based projects.
 *
 * <p>
 * All Lift based projects are encouraged to use this mixing to ensure same library dependencies consistently.
 * This also reduces the possiblility of conflicting libraries being included transitively in downstream projects.
 * </p>
 *
 * @author Indrajit Raychaudhuri
 */
protected trait Dependency extends BasicManagedProject with Configuration {

  def selectDynamic(default: String, alternatives: (String, String)*)(scalaVersion: String) =
    Map(alternatives: _*).getOrElse(scalaVersion, default)

  // Add all the Scala version specific variations here
  lazy val scalazVersion = Map("2.8.0" -> "5.0", "2.8.1" -> "5.0", "2.8.2" -> "5.0").getOrElse(buildScalaVersion, "6.0.3")
  lazy val specsVersion = Map("2.8.0" -> "1.6.5", "2.9.1" -> "1.6.9").getOrElse(buildScalaVersion, "1.6.8")
  lazy val scalacheckVersion = Map("2.8.0" -> "1.7", "2.8.1" -> "1.8", "2.8.2" -> "1.8").getOrElse(buildScalaVersion, "1.9")

  // Use newer version of javamail iff JavaNet repo is defined, else fallback to the one available in Maven central
  lazy val javamailVersion = if (repositories.contains(DownloadRepositories.JavaNet)) "1.4.4" else "1.4.1"

  lazy val scalazOrganisationId = scalazVersion match {
    case "5.0" => "com.googlecode.scalaz"
    case _     => "org.scalaz"
  }

  lazy val scalazArtifact = scalazVersion match {
    case "5.0" => "scalaz-core_2.8.0"
    case _     => "scalaz-core_2.9.1"
  }

  lazy val scalajpaArtifact = buildScalaVersion match {
    case "2.8.2" => "scalajpa_2.8.1"
    case v       => "scalajpa_" + v
  }

  lazy val squerylArtifact = buildScalaVersion match {
    case "2.8.2" => "squeryl_2.8.1"
    case v       => "squeryl_" + v
  }

  lazy val scalacheckArtifact = buildScalaVersion match {
    case "2.8.2" => "scalacheck_2.8.1"
    case v       => "scalacheck_" + v
  }

  lazy val specsArtifact = buildScalaVersion match {
    case "2.8.2" => "specs_2.8.1"
    case v       => "specs_" + v
  }

  def blackListedLibs: Seq[String] =
    "commons-codec-1.2.jar" :: "commons-codec-1.3.jar" ::
    "servlet-api-2.5-20081211.jar" ::
    "slf4j-api-1.5.6.jar" :: "slf4j-api-1.5.10.jar" ::
    Nil

  /**
   * Scope available in all classpath, transitive by default.
   */
  object CompileScope {
    lazy val amqp_client          = "com.rabbitmq"               % "amqp-client"          % "1.7.2"
    lazy val commons_codec        = "commons-codec"              % "commons-codec"        % "1.4"
    lazy val commons_fileupload   = "commons-fileupload"         % "commons-fileupload"   % "1.2.2"
    lazy val commons_httpclient   = "commons-httpclient"         % "commons-httpclient"   % "3.1"
    lazy val dispatch_http        = "net.databinder"            %% "dispatch-http"        % "0.7.8"
    lazy val javamail             = "javax.mail"                 % "mail"                 % javamailVersion
    lazy val joda_time            = "joda-time"                  % "joda-time"            % "1.6.2"
    lazy val htmlparser           = "nu.validator.htmlparser"    % "htmlparser"           % "1.2.1"
    lazy val mongo_java_driver    = "org.mongodb"                % "mongo-java-driver"    % "2.6.5"
    lazy val openid4java_consumer = "org.openid4java"            % "openid4java-consumer" % "0.9.5"
    lazy val paranamer            = "com.thoughtworks.paranamer" % "paranamer"            % "2.4.1"
    lazy val sanselan             = "org.apache.sanselan"        % "sanselan"             % "0.97-incubator"
    lazy val scalajpa             = "org.scala-libs"             % scalajpaArtifact       % "1.4"
    lazy val scalap               = "org.scala-lang"             % "scalap"               % buildScalaVersion
    lazy val scalate_core         = "org.fusesource.scalate"     % "scalate-core"         % "1.4.1"
    lazy val scalaz               = scalazOrganisationId         % scalazArtifact         % scalazVersion
    lazy val slf4j_api            = "org.slf4j"                  % "slf4j-api"            % "1.6.4"
    lazy val smackx               = "jivesoftware"               % "smack"                % "3.1.0"
    lazy val squeryl              = "org.squeryl"                % squerylArtifact        % "0.9.4"

    // Aliases
    lazy val openid4java  = openid4java_consumer
    lazy val mongo_driver = mongo_java_driver
    lazy val mongodb      = mongo_java_driver
  }

  /**
   * Scope provided by container, available only in compile and test classpath, non-transitive by default.
   */
  object ProvidedScope {
    lazy val atomikos_api    = "com.atomikos"      % "transactions-api"        % "3.2.3"    % "provided"
    lazy val atomikos_jta    = "com.atomikos"      % "transactions-jta"        % "3.2.3"    % "provided"
    lazy val atomikos_txn    = "com.atomikos"      % "transactions"            % "3.2.3"    % "provided"
    lazy val atomikos_util   = "com.atomikos"      % "atomikos-util"           % "3.2.3"    % "provided"
    lazy val hibernate_em    = "org.hibernate"     % "hibernate-entitymanager" % "3.4.0.GA" % "provided"
    lazy val logback         = "ch.qos.logback"    % "logback-classic"         % "1.0.0"    % "provided"
    lazy val log4j           = "log4j"             % "log4j"                   % "1.2.16"   % "provided"
    lazy val slf4j_log4j12   = "org.slf4j"         % "slf4j-log4j12"           % "1.6.4"    % "provided"
    lazy val persistence_api = "javax.persistence" % "persistence-api"         % "1.0"      % "provided"
    lazy val servlet_api     = "javax.servlet"     % "servlet-api"             % "2.5"      % "provided"
    lazy val transaction_api = "javax.transaction" % "transaction-api"         % "1.1"      % "provided"

    // Aliases
    lazy val hibernate   = hibernate_em
    lazy val jta         = transaction_api
    lazy val jta_api     = transaction_api
    lazy val transaction = transaction_api
    lazy val persistence = persistence_api
    lazy val servlet     = servlet_api
  }

  /**
   * Scope provided in runtime, available only in runtime and test classpath, not compile classpath, non-transitive by default.
   */
  object RuntimeScope {
    lazy val derby                = "org.apache.derby" % "derby"                % "10.7.1.1"      % "runtime" //% "optional"
    lazy val h2                   = "com.h2database"   % "h2"                   % "1.2.147"       % "runtime" //% "optional"
    // lazy val h2                   = "com.h2database"   % "h2"                   % "1.3.151"       % "runtime" //% "optional"
    lazy val postgresql           = "postgresql"       % "postgresql"           % "8.4-701.jdbc3" % "runtime" //% "optional"
    lazy val mysql_connector_java = "mysql"            % "mysql-connector-java" % "5.1.15"        % "runtime" //% "optional"

    // Aliases
    lazy val h2database = h2
    lazy val mysql      = mysql_connector_java
  }

  /**
   * Scope available only in test classpath, non-transitive by default.
   */
  object TestScope {
    lazy val apacheds      = "org.apache.directory.server" % "apacheds-server-integ"    % "1.5.5"           % "test" // TODO: See if something alternate with lesser footprint can be used
    lazy val jetty6        = "org.mortbay.jetty"           % "jetty"                    % "6.1.26"          % "test"
    lazy val jetty_webapp7 = "org.eclipse.jetty"           % "jetty-webapp"             % "7.2.2.v20101205" % "test"
    lazy val jetty_webapp8 = "org.eclipse.jetty"           % "jetty-webapp"             % "8.0.0.M2"        % "test"
    lazy val junit         = "junit"                       % "junit"                    % "4.7"             % "test"
    lazy val jwebunit      = "net.sourceforge.jwebunit"    % "jwebunit-htmlunit-plugin" % "2.5"             % "test"
    lazy val mockito_all   = "org.mockito"                 % "mockito-all"              % "1.8.5"           % "test"
    lazy val scalacheck    = "org.scala-tools.testing"     % scalacheckArtifact         % scalacheckVersion % "test"
    lazy val specs         = "org.scala-tools.testing"     % specsArtifact              % specsVersion      % "test"

    // Aliases
    lazy val jetty   = jetty6
    lazy val jetty7  = jetty_webapp7
    lazy val jetty8  = jetty_webapp8
    lazy val mockito = mockito_all
  }

}
