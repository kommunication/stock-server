name := "lab-stock-market"

version := "0.1"

//scalaVersion := "2.13.4"
scalaVersion := "2.12.8"
//ThisBuild / useCoursier := false

val versions = new {
  val finatra = "20.12.0"
  val finagle = "20.12.0"
  val logback = "1.2.3"
  val guice = "4.0"
  val scalatest = "3.0.5"
  val mockito = "3.2.0"
}

libraryDependencies ++= Seq(
  "com.twitter" %% "finatra-http" % versions.finatra,
  "com.twitter" %% "finatra-slf4j" % "2.12.0", // % versions.finatra,
  "ch.qos.logback" % "logback-classic" % versions.logback,
  "com.twitter" %% "finagle-stats" % versions.finagle,
  "com.twitter" %% "inject-server" % versions.finatra,
  "com.twitter" %% "inject-app" % versions.finatra,
  "com.twitter" %% "inject-modules" % versions.finatra,

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
  //  "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
  //  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  //  "org.specs2" %% "specs2-mock" % "2.4.17" % "test",


  /* Testing */
  , "org.mockito" % "mockito-core" % versions.mockito % "test"
  , "org.scalactic" %% "scalactic" % versions.scalatest  % "test"
  , "org.scalatest" %% "scalatest" % versions.scalatest % "test"

//  //JSON
//  ,"com.lihaoyi" %% "upickle" % "0.7.1"
//  ,"com.lihaoyi" %% "os-lib" % "0.7.1"

)


enablePlugins(sbtdocker.DockerPlugin, JavaAppPackaging)


// Define the Dockerfile
dockerfile in docker := {
  val jarFile = artifactPath.in(Compile, packageBin).value
  val classpath = (managedClasspath in Compile).value
  val mainclass =  "com.komlan.lab.market.StockServer"
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
//      "-XX:+PrintGC",
//      "-XX:+PrintGCDetails",
//      "-XX:+PrintGCDateStamps",
//      "-Xloggc:/logs/YmsServer-${INSTANCE_NAME}.gc.log",
//      "-XX:+UseGCLogFileRotation",
//      "-XX:NumberOfGCLogFiles=10",
//      "-XX:GCLogFileSize=2M",
//      //"-Xlog:gc*:file=/logs/YmsService-${INSTANCE_NAME}.gc.log:utctime,uptime,level,tags:filecount=10,filesize=1024000",
//      "-XX:+HeapDumpOnOutOfMemoryError",
//      "-XX:HeapDumpPath=/logs",
//      "-Djava.rmi.server.hostname=${HOSTIP}",
//      "-Dcom.sun.management.jmxremote",
//      "-Dcom.sun.management.jmxremote.port=${RMI_PORT}",
//      "-Dcom.sun.management.jmxremote.rmi.port=${RMI_PORT}",
//      "-Dcom.sun.management.jmxremote.local.only=false",
//      "-Dcom.sun.management.jmxremote.authenticate=false",
//      "-Dcom.sun.management.jmxremote.ssl=false",
      "-cp", classpathString,  mainclass , "-kafka.state.dir=kafka-stream-state/$HOSTNAME")
  }

}