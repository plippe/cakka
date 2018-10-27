
lazy val commonSettings = Seq(
  scalafmtOnCompile := true,
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.7")
)

lazy val core = project
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % "0.10.0",
      "io.circe" %% "circe-generic" % "0.10.0",
      "io.circe" %% "circe-parser" % "0.10.0"
    )
  )

lazy val aws = project
  .dependsOn(core)
  .settings(commonSettings)
  .settings(
    libraryDependencies += "com.amazonaws" % "aws-java-sdk-lambda" % "1.11.427"
  )

lazy val example = project
  .dependsOn(aws)
  .settings(commonSettings)
