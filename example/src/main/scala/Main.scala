package com.github.plippe.cakka

import cats.implicits._
import com.amazonaws.services.lambda.AWSLambdaClientBuilder

import com.github.plippe.cakka.aws._

object Main extends App {
  println("Hello World")

  val awsLambda = AWSLambdaClientBuilder.defaultClient
  val awsLambdaFunctionName = "aws-lambda-function-name"
  val actor = new AwsLambdaActorRef[Either[Throwable, ?]](awsLambda,
                                                          awsLambdaFunctionName)

  println("TELL - START")
  println(actor.!("TELL"))
  println("TELL - END")

  println("ASK - START")
  println(actor.?[String, Unit]("ASK"))
  println("ASK - END")
}
