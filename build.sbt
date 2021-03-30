name := "komlan-stock-market"

version := "0.1"

scalaVersion := "2.12.8"

val versions = new {
  val finatra = "20.12.0"
  val finagle = "20.12.0"
  val logback = "1.2.3"
  val guice = "4.0"
  val scalatest = "3.0.5"
  val mockito = "3.2.0"
  val akkaVersion = "2.6.13"
}

libraryDependencies ++= Seq(
  "com.twitter" %% "finatra-http" % versions.finatra,
  "com.twitter" %% "finatra-validation" % versions.finatra,
  "com.twitter" %% "finatra-slf4j" % "2.12.0", // % versions.finatra,
  "ch.qos.logback" % "logback-classic" % versions.logback,
  "com.twitter" %% "finagle-stats" % versions.finagle,
  "com.twitter" %% "inject-server" % versions.finatra,
  "com.twitter" %% "inject-app" % versions.finatra,
  "com.twitter" %% "inject-modules" % versions.finatra,
  //"com.typesafe.akka" %% "akka-stream" % versions.akkaVersion,

  //"ch.qos.logback" % "logback-classic" % versions.logback % Test,
  "com.twitter" %% "finatra-http" % versions.finatra % "test",
  "com.twitter" %% "finatra-jackson" % versions.finatra % "test",
  "com.twitter" %% "inject-server" % versions.finatra % "test",
  "com.twitter" %% "inject-app" % versions.finatra % "test",
  "com.twitter" %% "inject-core" % versions.finatra % "test",
  "com.twitter" %% "inject-modules" % versions.finatra % "test",
  "com.google.inject.extensions" % "guice-testlib" % versions.guice % "test",

  "com.twitter" %% "finatra-http" % versions.finatra % "test" classifier "tests",
  "com.twitter" %% "finatra-jackson" % versions.finatra % "test" classifier "tests",
  "com.twitter" %% "inject-server" % versions.finatra % "test" classifier "tests",
  "com.twitter" %% "inject-app" % versions.finatra % "test" classifier "tests",
  "com.twitter" %% "inject-core" % versions.finatra % "test" classifier "tests",
  "com.twitter" %% "inject-modules" % versions.finatra % "test" classifier "tests",

  "org.mockito" % "mockito-core" % "1.9.5" % "test"

  /* Jackson */
  ,"com.fasterxml.jackson.dataformat" % "jackson-dataformat-csv" % "2.11.2"

  /* Testing framework */
  , "org.mockito" % "mockito-core" % versions.mockito % "test"
  , "org.scalactic" %% "scalactic" % versions.scalatest  % "test"
  , "org.scalatest" %% "scalatest" % versions.scalatest % "test"


)


enablePlugins(sbtdocker.DockerPlugin, JavaAppPackaging)


// Define the Dockerfile
dockerfile in docker := {
  val jarFile = artifactPath.in(Compile, packageBin).value
  val classpath = (managedClasspath in Compile).value
  val mainClass =  "com.komlan.lab.market.StockServer"
  val jarTarget = s"/app/${jarFile.getName}"
  // Make a colon separated classpath with the JAR file
  val classpathString = classpath.files.map("/app/" + _.getName).mkString(":") + ":" + jarTarget

  new Dockerfile {
    // Base image
    from("openjdk:8u212-jre")

    // Add all files on the classpath
    add(classpath.files, "/app/")

    // Add the JAR file
    add(jarFile, jarTarget)

    //Expose the ports
    expose(8085)
    expose(8888) // API
    expose(9990) // Finagle Admin page

    // On launch run Java with the classpath and the main class
    entryPointShell("java",
      "${JAVA_OPTS}",
      "-cp", classpathString,  mainClass )
  }

}