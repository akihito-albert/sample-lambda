name := "Sample-Lambda"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
  "com.amazonaws" % "aws-lambda-java-events" % "1.1.0",
  "org.specs2" %% "specs2-core" % "3.7" % "test"
)