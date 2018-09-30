lazy val akkaCluster = "com.typesafe.akka" %% "akka-cluster" % "2.5.5"
lazy val pureConfig = "com.github.pureconfig" %% "pureconfig" % "0.9.2"
lazy val akkaClusterTools = "com.typesafe.akka" %% "akka-cluster-tools" % "2.5.16"

val root = (project in file("."))
  .settings(
    name := "Chat",
    version := "0.1",
    scalaVersion := "2.12.6"
  )
  .settings(
    libraryDependencies ++= Seq(
      akkaCluster,
      pureConfig,
      akkaClusterTools
    )
  )
