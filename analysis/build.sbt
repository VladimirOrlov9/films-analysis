name := "test1"

version := "0.1"

scalaVersion := "2.12.13"



libraryDependencies += "org.apache.spark" %% "spark-core" % "3.2.0"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.2.0"
libraryDependencies += "org.plotly-scala" % "plotly-render_2.12" % "0.8.0"
//
//resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
//
//libraryDependencies += "co.theasi" %% "plotly" % "0.3.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.mongodb.spark" %% "mongo-spark-connector" % "3.0.1"
)

dependencyOverrides += "org.json4s" %% "json4s-ast" % "3.5.3"
dependencyOverrides += "org.json4s" %% "json4s-native" % "3.5.3"
