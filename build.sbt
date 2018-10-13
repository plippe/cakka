scalafmtOnCompile := true

libraryDependencies += "com.amazonaws" % "aws-java-sdk-lambda" % "1.11.427"

val circeVersion = "0.10.0"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)
