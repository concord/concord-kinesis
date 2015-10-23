lazy val root = (project in file(".")).
settings(
  name := "concord_kinesis_consumer",
  version := "0.1.0",
  scalaVersion := "2.11.6",
  scalacOptions ++= Seq("-feature", "-language:higherKinds"),
  libraryDependencies ++= Seq(
    "io.concord" % "concord" % "0.1.0",
    "io.concord" % "rawapi" % "0.1.0",
    "com.amazonaws" % "amazon-kinesis-client" % "1.6.1",
    "com.beust" % "jcommander" % "1.48"
    ),
  assemblyMergeStrategy in assembly := {
    case x if x.endsWith("project.clj") => MergeStrategy.discard // Leiningen build files
    case x if x.toLowerCase.startsWith("meta-inf") => MergeStrategy.discard // More bumf
    case _ => MergeStrategy.first
  },
  mainClass in Compile := Some("com.concord.kinesis.Consumer"),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("public"),
    "clojars" at "https://clojars.org/repo",
    "conjars" at "http://conjars.org/repo"
  )
)
