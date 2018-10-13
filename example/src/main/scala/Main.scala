package com.github.plippe.cakka

import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

import com.github.plippe.cakka.aws._

object Main extends App {
  println("Hello World")

  val actor = AwsLambdaActorRef(AWSLambdaClientBuilder.defaultClient, "my-lambda")

  println("TELL - START")
  actor ! "TELL"
  println("TELL - END")

  println("ASK - START")
  val f = actor ? "ASK"
  println("ASK - END")

  println("ASK - WAITING")
  Await.result(f, Duration.Inf)
  println(s"ASK - $f")
}
