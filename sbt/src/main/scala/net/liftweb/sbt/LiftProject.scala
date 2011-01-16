package net.liftweb.sbt

import java.net.URL
import java.util.jar.Attributes.Name._
import _root_.sbt._

trait LiftParentProject extends ParentProject with Configuration with ScalaProject with MavenStyleScalaPaths {

  // Disable dependencies on sub-projects
  override def deliverProjectDependencies = Nil

}


trait LiftDefaultProject extends DefaultProject with Configuration {

  // Dependencies options
  // --------------------
  override def libraryDependencies = {
    import Dependencies._
    super.libraryDependencies ++ Set(specs, scalacheck, junit)
  }

  // Compile options
  // ---------------
  override def compileOptions =
    super.compileOptions ++
      { if (isSnapshot) Seq(ExplainTypes, Unchecked) else Seq(Optimize) } ++
      Seq("-Xmigration", "-Xcheckinit", "-Xwarninit", "-encoding", "utf8").map(CompileOption)

  // Package options
  // ---------------
  override def packageOptions =
    super.packageOptions ++ Seq(specificationEntries, implementationEntries)

  lazy val specificationEntries =
    ManifestAttributes(
      (SPECIFICATION_TITLE, name),
      (SPECIFICATION_VERSION, version.toString),
      (SPECIFICATION_VENDOR, projectOrganizationFormal.value))

  lazy val implementationEntries =
    ManifestAttributes(
      (IMPLEMENTATION_TITLE, name),
      (IMPLEMENTATION_VERSION, version.toString),
      (IMPLEMENTATION_VENDOR_ID, organization),
      (IMPLEMENTATION_VENDOR, projectOrganizationFormal.value),
      (IMPLEMENTATION_URL, projectLocation.value.toString))

  // override def mainResources = super.mainResources +++ "LICENSE" +++ "NOTICE"

  // Make `package` depend on `test`
  override def packageAction = super.packageAction dependsOn test

  // Document options
  // ----------------
  override def documentOptions =
    documentTitle(docTitle) :: CompoundDocOption("-doc-version", version.toString) :: Nil

  lazy val docTitle = "%s %s API".format(projectNameFormal.value, version)

  // Publish options
  // ---------------
  override def packageDocsJar = defaultJarPath("-javadoc.jar")
  override def packageSrcJar  = defaultJarPath("-sources.jar")

  override def packageToPublishActions = super.packageToPublishActions ++ Seq(packageDocs, packageSrc)

}


trait LiftDocProject extends DefaultProject with Configuration {

  // We modify the parameter that docAction and docTestAction takes instead of modifying the action itself
  override def mainSources  = concatPaths(childrenProjects) { case p: ScalaPaths => p.mainSources }
  override def testSources  = concatPaths(childrenProjects) { case p: ScalaPaths => p.testSources }
  override def docClasspath = concatPaths(childrenProjects) { case p: BasicScalaProject => p.docClasspath }

	private def concatPaths[T](s: Seq[T])(f: PartialFunction[T, PathFinder]) = {
		def finder: T => PathFinder = (f orElse { case _ => Path.emptyPathFinder })
		(Path.emptyPathFinder /: s) { _ +++ finder(_) }
	}

  private def childrenProjects = 
    info.parent.get.projectClosure.flatMap {
      case c: LiftDefaultProject => Some(c)
      case _                     => None
    }

  // Nothing to compile, deliver or publish
	override def compileAction     = Empty
	override def testCompileAction = Empty
	override def makePomAction     = Empty

  override def publishLocalAction = Empty
  override def deliverLocalAction = Empty

  override def deliverAction = Empty
  override def publishAction = Empty

  // To avoid write collisions with outputDirectories of parent
	override def outputRootPath        = super.outputRootPath / "apidoc"
	override def managedDependencyPath = super.managedDependencyPath / "apidoc"

  // No packageAction for DocProject
  override def packageTask(sources: PathFinder, jarPath: => Path, options: => Seq[PackageOption]) = Empty 
   
}


protected trait Configuration extends BasicDependencyProject {

  // Dependencies
  // ------------
  val specsVersion = buildScalaVersion match {
    case "2.8.0" => "1.6.5"
    case _       => "1.6.7"
  }
  val scalacheckVersion = buildScalaVersion match {
    case "2.8.0" => "1.7"
    case _       => "1.8"
  }

  object Dependencies {

    // Compile scope: available in all classpath, transitive by default
    lazy val commons_codec      = "commons-codec" % "commons-codec" % "1.4"
    lazy val commons_fileupload = "commons-fileupload" % "commons-fileupload" % "1.2.2"
    lazy val commons_httpclient = "commons-httpclient" % "commons-httpclient" % "3.1"
    lazy val dispatch           = "net.databinder" %% "dispatch-http" % "0.7.8"
    lazy val javamail           = "javax.mail" % "mail" % "1.4.1"
    lazy val joda_time          = "joda-time" % "joda-time" % "1.6.2"
    lazy val htmlparser         = "nu.validator.htmlparser" % "htmlparser" % "1.2.1"
    lazy val mongo_driver       = "org.mongodb" % "mongo-java-driver" % "2.4"
    lazy val slf4j_api          = "org.slf4j" % "slf4j-api" % "1.6.1"
    lazy val slf4j_log4j12      = "org.slf4j" % "slf4j-log4j12" % "1.6.1" % "optional"
    lazy val paranamer          = "com.thoughtworks.paranamer" % "paranamer" % "2.3"
    lazy val scalajpa           = "org.scala-libs" %% "scalajpa" % "1.2"
    lazy val squeryl            = "org.squeryl" %% "squeryl" % "0.9.4-RC3"

    // Provided scope: provided by container, available only in compile and test classpath, non-transitive by default
    lazy val log4j           = "log4j" % "log4j" % "1.2.16" % "provided"
    lazy val logback         = "ch.qos.logback" % "logback-classic" % "0.9.27" % "provided"
    lazy val persistence_api = "javax.persistence" % "persistence-api" % "1.0" % "provided"
    lazy val servlet_api     = "javax.servlet" % "servlet-api" % "2.5" % "provided"

    // Runtime scope: provided in runtime, available only in runtime and test classpath, not compile classpath, non-transitive by default
    lazy val derby      = "org.apache.derby" % "derby" % "10.7.1.1" % "runtime" //% "optional"
    lazy val h2database = "com.h2database" % "h2" % "1.2.147" % "runtime" //% "optional"

    // Test scope: available only in test classpath, non-transitive by default
    lazy val jetty      = "org.mortbay.jetty" % "jetty" % "6.1.26" % "test"
    lazy val junit      = "junit" % "junit" % "4.7" % "test"
    lazy val jwebunit   = "net.sourceforge.jwebunit" % "jwebunit-htmlunit-plugin" % "2.5" % "test"
    lazy val scalacheck = "org.scala-tools.testing" %% "scalacheck" % scalacheckVersion % "test"
    lazy val specs      = "org.scala-tools.testing" %% "specs" % specsVersion % "test"
  }

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
  // Custom format for java.net.URL
  implicit lazy val urlFormat = new SimpleFormat[URL] { def fromString(s: String) = new URL(s) }

  // Additional user-defined properties that optionally can be defined for the project.
  lazy val projectNameFormal           = propertyOptional[String](name.split("-").map(_.capitalize).mkString(" "))
  lazy val projectLocation             = propertyOptional[URL](new URL("http://www.liftweb.net"), true)
  lazy val projectInceptionyear        = propertyOptional[Int](2006, true)
  lazy val projectOrganizationFormal   = propertyOptional[String]("WorldWide Conferencing, LLC", true)
  lazy val projectOrganizationLocation = propertyOptional[URL](projectLocation.value, true)

  lazy val projectLicenseName         = propertyOptional[String]("Apache License, Version 2.0", true)
  lazy val projectLicenseLocation     = propertyOptional[URL](new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"), true)
  lazy val projectLicenseDistribution = propertyOptional[String]("repo", true)

  // Custom flag to enable local maven repository
  lazy val mavenLocal = propertyOptional[Boolean](false, true)  

  // Custom flag to enable remote publishing
  lazy val publishRemote = propertyOptional[Boolean](false, true)

  // Repository options
  // ------------------
  override def repositories = {
    import scala.collection.mutable._
    val extras: Set[Resolver] = Set()
    if (isSnapshot) extras += ScalaToolsSnapshots
    if (mavenLocal.value) extras += DownloadRepositories.local
    super.repositories ++ extras
  }

  // Publish options
  // ---------------
  Credentials(Path.userHome / ".ivy2" / ".scalatools.credentials", log)

  override def managedStyle = ManagedStyle.Maven

  override def defaultPublishRepository = {
    import DistributionRepositories._
    if (!publishRemote.value) Some(local)
    else Some(if (isSnapshot) snapshot else release)
  }

  // override def ivyXML =
  //   super.ivyXML ++
  //   <info>
  //     <license name="{projectLicenseName.value}" url="{projectLicenseLocation.value}"></license>
  //   </info>

  // Add all configurations, not just the public ones
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

	override def toString = 
	  "Project: " + projectNameFormal.get.getOrElse("at " + environmentLabel)

  // Helpers
  // -------
  def isSnapshot = version.toString.endsWith("-SNAPSHOT")

}
