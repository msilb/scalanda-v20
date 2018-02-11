organization := "com.msilb"
name := "scalanda-v20"

scalaVersion := "2.12.2"

scalacOptions := Seq(
  "-target:jvm-1.8",
  "-encoding", "UTF-8",
  "-unchecked",
  "-deprecation",
  "-Xfuture",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ywarn-unused",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions"
)

libraryDependencies ++= {
  val circeV = "0.9.1"
  val akkaHttpV = "10.0.9"
  val scalaTestV = "3.0.1"
  val scalaMockV = "3.5.0"
  Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpV,
    "io.circe" %% "circe-core" % circeV,
    "io.circe" %% "circe-generic" % circeV,
    "io.circe" %% "circe-parser" % circeV,
    "io.circe" %% "circe-java8" % circeV,
    "io.circe" %% "circe-generic-extras" % circeV,
    "org.scalatest" %% "scalatest" % scalaTestV % Test,
    "org.scalamock" %% "scalamock-scalatest-support" % scalaMockV % Test
  )
}

resolvers += Resolver.sonatypeRepo("releases")
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
addCompilerPlugin("org.psywerx.hairyfotr" %% "linter" % "0.1.17")
