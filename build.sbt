ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "Aliaser",
    description := "App to make command aliases easier",
    libraryDependencies ++= Seq(
      "org.scalafx" %% "scalafx" % "20.0.0-R31",
      "org.typelevel" %% "cats-core" % "2.10.0",
    ),
  )
  .enablePlugins(AssemblyPlugin)

assembly / assemblyJarName := "Aliaser.jar"
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", _*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}
