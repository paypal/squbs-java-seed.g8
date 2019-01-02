
name := "$appname;format="norm"$"

version := "$version$"

organization in ThisBuild := "$organization$.$project;format="norm"$"

scalaVersion := "$scala_version$"

import Versions._

crossPaths := false
resolvers += Resolver.sonatypeRepo("snapshots")

Revolver.settings

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")
javacOptions in Compile += "-parameters" // This is needed for jackson-module-parameter-names.
testOptions in Test += Tests.Argument(TestFrameworks.JUnit, "-v", "-a")

// Jacoco instruments weird things we can't control, like synthetic methods and constructors.
// We can only go to 95% for most things measured by Jacoco.
jacocoReportSettings := JacocoReportSettings(
    "Jacoco Coverage Report",
    None,
    JacocoThresholds(
        instruction = 90,
        method = 90,
        line = 90),
    Seq(JacocoReportFormats.ScalaHTML),
    "utf-8")

// Scoverage controls. Much cleaner here. Just Scala only.
coverageMinimum := 90
coverageFailOnMinimum := true

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % Versions.logbackClassicV,
  "org.squbs" %% "squbs-unicomplex" % Versions.squbsV,
  "org.squbs" %% "squbs-actormonitor" % Versions.squbsV,
  "org.squbs" %% "squbs-actorregistry" % Versions.squbsV,
  "org.squbs" %% "squbs-httpclient" % Versions.squbsV,
  "org.squbs" %% "squbs-pattern" % Versions.squbsV,
  "org.squbs" %% "squbs-admin" % Versions.squbsV,
  "org.scala-lang.modules" %% "scala-java8-compat" % Versions.scalaJavaCompatV,
  "de.heikoseeberger" %% "akka-http-jackson" % Versions.akkaHttpJacksonV,
  "com.fasterxml.jackson.core" % "jackson-core" % Versions.jacksonV,
  "com.fasterxml.jackson.core" % "jackson-annotations" % Versions.jacksonV,
  "com.fasterxml.jackson.core" % "jackson-databind" % Versions.jacksonV,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % Versions.jacksonV,
  "com.fasterxml.jackson.module" % "jackson-module-parameter-names" % Versions.jacksonV,
  "org.squbs" %% "squbs-testkit" % Versions.squbsV % "test",
  "com.typesafe.akka" %% "akka-http-testkit" % Versions.akkaHttpV % "test",
  "junit" % "junit" % Versions.junitV % "test",
  "com.novocode" % "junit-interface" % Versions.junitInterfaceV % "test->default"
)

mainClass in (Compile, run) := Some("org.squbs.unicomplex.Bootstrap")

// enable scalastyle on compile
lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")

compileScalastyle := scalastyle.in(Compile).toTask("").value

(compile in Compile) := ((compile in Compile) dependsOn compileScalastyle).value

enablePlugins(PackPlugin)

packMain := Map("run" -> "org.squbs.unicomplex.Bootstrap")

enablePlugins(DockerPlugin)

dockerfile in docker := {
  val jarFile: File = sbt.Keys.`package`.in(Compile, packageBin).value
  val classpath = (managedClasspath in Compile).value
  val mainclass = "org.squbs.unicomplex.Bootstrap"
  val jarTarget = s"/app/\${jarFile.getName}"
  // Make a colon separated classpath with the JAR file
  val classpathString = classpath.files.map("/app/" + _.getName)
    .mkString(":") + ":" + jarTarget
  new Dockerfile {
    // Base image
    from("java")
    // Add all files on the classpath
    add(classpath.files, "/app/")
    // Add the JAR file
    add(jarFile, jarTarget)
    // On launch run Java with the classpath and the main class
    entryPoint("java", "-cp", classpathString, mainclass)
  }
}
