organization := "io.bigbears"
name := "play-seed"
version := "1.0.0"

sources in(Compile, doc) := Seq.empty
publishArtifact in(Compile, packageDoc) := false

scalaVersion := "2.11.8"
//scalacOptions := Seq("-feature", "-deprecation")
scalacOptions := Seq("-unchecked", "-optimise", "-Yno-adapted-args", "-target:jvm-1.8")

lazy val root = (project in file(".")).enablePlugins(PlayScala)

routesGenerator := InjectedRoutesGenerator

libraryDependencies ++= Seq(
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.8.4"
  , "com.firebase" % "firebase-client" % "1.0.1"
  , "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
  , "de.svenkubiak" % "jBCrypt" % "0.4.1"
  , "net.codingwell" %% "scala-guice" % "4.1.0"
)

parallelExecution := true
parallelExecution in Test := true
libraryDependencies ++= Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

excludeDependencies ++= Seq(
  SbtExclusionRule("com.typesafe.play", "play-java")
  , SbtExclusionRule("com.typesafe.play", "play-json")
)

import com.typesafe.sbt.packager.universal.ZipHelper
packageBin in Universal := {
  val originalFileName = (packageBin in Universal).value
  val (base, ext) = originalFileName.baseAndExt
  val newFileName = file(originalFileName.getParent) / (base + "_dist." + ext)
  val extractedFiles = IO.unzip(originalFileName, file(originalFileName.getParent))
  val mappings: Set[(File, String)] = extractedFiles
    .map(f => (f, f.getAbsolutePath.substring(originalFileName.getParent.length + base.length + 2)))
  val binFiles = mappings.filter { case (file, path) => path.startsWith("bin/") }
  for (f <- binFiles) f._1.setExecutable(true)
  ZipHelper.zip(mappings, newFileName)
  IO.move(newFileName, originalFileName)
  IO.delete(file(originalFileName.getParent + "/" + originalFileName.base))
  originalFileName
}
