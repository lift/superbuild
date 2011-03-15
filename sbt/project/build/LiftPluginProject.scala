import sbt._

class LiftPluginProject(info: ProjectInfo) extends PluginProject(info) with MavenCredentials {

  // Set publish.remote=true to publish to remote repo, defaults to maven local repo
  lazy val publishRemote = propertyOptional[Boolean](systemOptional[Boolean]("publish.remote", false).value, true)

  val yuiCompressor = "com.yahoo.platform.yui" % "yuicompressor" % "2.4.2" withSources()

  // Set up publish repository
  object PublishRepositories {
    val local    = Resolver.file("Local Maven Distribution Repository", Path.userHome / ".m2" / "repository" asFile)
    val snapshot = "Scala-Tools Distribution Repository for Snapshots" at "http://nexus.scala-tools.org/content/repositories/snapshots/"
    val release  = "Scala-Tools Distribution Repository for Releases"  at "http://nexus.scala-tools.org/content/repositories/releases/"
  }

  lazy val publishTo =
    if (version.toString.endsWith("-SNAPSHOT")) PublishRepositories.snapshot
    else PublishRepositories.release
  
  // Tell SBT to publish to local Maven repository unless publish.remote=true
  override def defaultPublishRepository =
    if (!publishRemote.value) Some(PublishRepositories.local)
    else super.defaultPublishRepository

  // Add ScalaToolsSnapshots if this project is on snapshot
  override def repositories =
    super.repositories ++ {
      if (version.toString.endsWith("-SNAPSHOT")) Set(ScalaToolsSnapshots)
      else Set.empty
    }
}

protected trait MavenCredentials extends BasicDependencyProject {

  lazy val ivyCredentialsPath   = Path.userHome / ".ivy2" / ".scalatools.credentials"
  lazy val mavenCredentialsPath = Path.userHome / ".m2" / "settings.xml"

  lazy val scalaTools = ("Sonatype Nexus Repository Manager", "nexus.scala-tools.org")

  (ivyCredentialsPath.asFile, mavenCredentialsPath.asFile) match {
    case(ivy, _) if ivy.canRead => Credentials(ivy, log)
    case(_, mvn) if mvn.canRead => loadMavenCredentials(mvn)
    case _ => log.warn("Could not read any of the settings files %s or %s".format(ivyCredentialsPath, mavenCredentialsPath))
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

  // publishAction does publishLocal too
  override def publishAction = super.publishAction dependsOn publishLocal

}
