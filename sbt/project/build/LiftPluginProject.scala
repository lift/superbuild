import sbt._

class LiftPluginProject(info: ProjectInfo) extends PluginProject(info) {

  // Set publish.remote=true to publish to remote repo, defaults to maven local repo
  lazy val publishRemote = propertyOptional[Boolean](false, true)
  
  lazy val mavenLocal = "Local Maven Repository" at "file://" + Resolver.userMavenRoot
  
  // Set up publish repository
  object PublishRepositories {
    val local    = Resolver.file("Local Maven Distribution Repository", Path.userHome / ".m2" / "repository" asFile)
    val snapshot = "Scala-Tools Distribution Repository for Snapshots" at "http://nexus.scala-tools.org/content/repositories/snapshots/"
    val release  = "Scala-Tools Distribution Repository for Releases"  at "http://nexus.scala-tools.org/content/repositories/releases/"
  }

  lazy val publishTo =
    if (version.toString.endsWith("-SNAPSHOT")) PublishRepositories.snapshot
    else PublishRepositories.release
  
  Credentials(Path.userHome / ".ivy2" / ".scalatools.credentials", log)
  
  // Tell SBT to publish to local Maven repository unless publish.remote=true
  override def defaultPublishRepository =
    if (!publishRemote.value) Some(PublishRepositories.local)
    else super.defaultPublishRepository

  // Add ScalaToolsSnapshots if this project is on snapshot
  override def repositories =
    super.repositories ++ {
      if (version.toString.endsWith("-SNAPSHOT")) Set(ScalaToolsSnapshots)
      else Set()
    }
  // override def repositories =
  //   if (version.toString.endsWith("-SNAPSHOT")) super.repositories + PublishRepositories.local + ScalaToolsSnapshots
  //   else super.repositories

}
