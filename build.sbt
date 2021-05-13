name := """RestClient"""
organization := "egnaro"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  guice,
  ws,
  ehcache,
  cacheApi,
  "commons-io" % "commons-io" % "2.8.0",
  "org.apache.httpcomponents" % "httpclient" % "4.5.13",
  "org.apache.httpcomponents" % "httpmime" % "4.5.9",
  "org.mongodb.morphia" % "morphia" % "1.1.1",
  "org.mongodb" % "mongo-java-driver" % "3.6.4"
)