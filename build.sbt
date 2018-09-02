name := "gondola"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed"         % "2.5.16",
  "org.scalactic"     %% "scalactic"                % "3.0.5"  % Test,
  "org.scalatest"     %% "scalatest"                % "3.0.5"  % Test,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % "2.5.16" % Test
)