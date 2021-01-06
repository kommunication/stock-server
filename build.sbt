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
