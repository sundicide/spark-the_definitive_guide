ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.11.8"

// Spark Information
val sparkVersion = "2.1.0"

//// allows us to include spark packages
//resolvers += "bintray-spark-packages" at
//  "https://dl.bintray.com/spark-packages/maven/"
//
//resolvers += "Typesafe Simple Repository" at
//  "http://repo.typesafe.com/typesafe/simple/maven-releases/"
//
//resolvers += "MavenRepository" at
//  "https://mvnrepository.com/"

lazy val root = (project in file("."))
  .settings(
    name := "spark-the_definitive_guide",
    resolvers ++= Seq(
      "bintray-spark-packages" at "https://dl.bintray.com/spark-packages/maven/",
      "Typesafe Simple Repository" at "https://repo.typesafe.com/typesafe/simple/maven-releases/",
      "MavenRepository" at "https://mvnrepository.com/",
      "Spark Packages Repo" at "https://repos.spark-packages.org/"
    ),
    libraryDependencies ++= Seq(
      // spark core
      "org.apache.spark" %% "spark-core" % sparkVersion,
      "org.apache.spark" %% "spark-sql" % sparkVersion,

      // spark-modules
      "org.apache.spark" %% "spark-graphx" % sparkVersion,
      "org.apache.spark" %% "spark-mllib" % sparkVersion,

      // spark packages
      "graphframes" % "graphframes" % "0.4.0-spark2.1-s_2.11",

      // testing
      "org.scalatest" %% "scalatest" % "2.2.4" % "test",
      "org.scalacheck" %% "scalacheck" % "1.12.2" % "test",

      // logging
      "org.apache.logging.log4j" % "log4j-api" % "2.4.1",
      "org.apache.logging.log4j" % "log4j-core" % "2.4.1"
    )
  )
