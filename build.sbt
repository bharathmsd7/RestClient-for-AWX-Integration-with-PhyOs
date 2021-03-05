name := """RestClient"""
organization := "egnaro"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.12.4"

libraryDependencies += guice

libraryDependencies += ws

// https://mvnrepository.com/artifact/commons-io/commons-io
libraryDependencies += "commons-io" % "commons-io" % "2.8.0"
// https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient
libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.5.13"
// https://mvnrepository.com/artifact/org.apache.httpcomponents/httpmime
libraryDependencies += "org.apache.httpcomponents" % "httpmime" % "4.5.9"

libraryDependencies += ehcache

libraryDependencies ++= Seq(
  cacheApi
)