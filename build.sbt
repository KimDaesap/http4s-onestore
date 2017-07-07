
val commonSettings = Seq(
  organization := "com.example",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := "2.12.2"
)

val Http4sVersion = "0.15.13a"
val circeVersion = "0.6.1"

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "http4s-quickstart",

    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server",
      "org.http4s" %% "http4s-blaze-client",
      "org.http4s" %% "http4s-scala-xml",
      "org.http4s" %% "http4s-circe",
      "org.http4s" %% "http4s-dsl"
    ).map(_ % Http4sVersion),

    libraryDependencies ++= Seq(
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-literal"
    ).map(_ % circeVersion),

    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick"            % "3.2.0",
      "com.h2database"      % "h2"               % "1.4.191",
      "ch.qos.logback"      % "logback-classic"  % "1.2.1"
    ),

    // Test library
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.1" % "test"
    )
  )