name := "ssap"

version := "1.0"

scalaVersion := "2.11.8"

oneJarSettings

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "org.slf4j" % "slf4j-simple" % "1.7.5",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.3",
  "commons-io" % "commons-io" % "2.4",
  "org.ow2.asm" % "asm-all" % "5.1",
  "com.github.scopt" %% "scopt" % "3.4.0"
)

resolvers ++= Seq(
  Resolver.sonatypeRepo("public")
)