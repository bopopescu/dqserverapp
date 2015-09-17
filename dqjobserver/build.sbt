import AssemblyKeys._

name := "DQJobServer"

version := "0.1.0"

scalaVersion := "2.10.4"

organization := "com.sar"

// Do not append Scala versions to the generated artifacts
crossPaths := false

// This forbids including Scala related libraries into the dependency
autoScalaLibrary := false

val sparkVersion = sys.env.getOrElse("SPARK_VERSION", "1.4.1")
val sjsVersion = sys.env.getOrElse("SJS_VERSION", "0.5.2")

libraryDependencies ++= {
  	Seq(
		
  	    
  	    "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
		"org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
		"org.apache.hadoop" % "hadoop-client" % "2.6.0" % "provided",
		"com.typesafe" % "config" % "1.0.0" % "provided",
		"org.json4s"    	  %% "json4s-native"  % "3.2.10" % "provided",
   		"spark.jobserver" %% "job-server-api" % sjsVersion % "provided",
		"spark.jobserver" %% "job-server" % sjsVersion % "provided",
		"spark.jobserver" %% "job-server-extras" % sjsVersion % "provided",
		"mysql"    	  		  % "mysql-connector-java"  % "5.1.6",
   		"org.specs2" %% "specs2-core" % "2.3.7" % "test" ,
  	    "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
	  	 "junit" % "junit" % "4.8.1" % "test"
  	)
}

mainClass := Some("com.sar.spark.server.DQJobServer")

assemblySettings

assembleArtifact in packageScala := false

mainClass in assembly := Some("com.sar.spark.server.DQJobServer")

jarName  in assembly := "DQJobServer.jar"

 excludedJars  in assembly := { 
  val cp = (fullClasspath in assembly).value
  cp filter {x => x.data.getName == "slf4j-api-1.7.7.jar" || x.data.getName == "config-1.2.1.jar"}
 } 


test in assembly := {}

runMain in Compile <<= Defaults.runMainTask(fullClasspath in Compile, runner in (Compile, run))

resolvers ++= Seq("Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/",
    				"sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
    				"Job Server Bintray" at "https://dl.bintray.com/spark-jobserver/maven"
                )

val deployTask = TaskKey[Unit]("deploy", "Assembly and Copy files to Deploy folder of ec2launch")

deployTask <<= assembly map { (asm) => 
	val deploypath = "/sar/ec2launch/deploy.generic/root/dqguard/."
	println(s"Deploying ${asm.getPath} to ${deploypath} ") 
	Seq("cp", asm.getPath, deploypath ) !!
}

fullClasspath in Revolver.reStart := (fullClasspath in Compile).value

mainClass in Revolver.reStart := Some("com.sar.spark.server.JobServer")

Revolver.settings