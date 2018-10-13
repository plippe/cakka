package com.github.plippe.cakka.aws

import cats._
import cats.implicits._
import com.amazonaws.services.lambda.{AWSLambda => OfficialAwsLambda}
import com.amazonaws.services.lambda.model.InvokeRequest
import io.circe.Encoder
import io.circe.parser._
import io.circe.syntax._
import scala.concurrent.{ExecutionContext, Future}

import com.github.plippe.cakka.core._

class AwsLambdaActorRef[F[_]](awsLambda: AwsLambda[F], functionName: String)(
    implicit F: MonadError[F, Throwable])
    extends TellableActorRef
    with AskableActorRef[F] {

  override def tell[A](msg: A)(implicit enc: Encoder[A]): Unit = {
    val request = invokeRequest(msg)
    awsLambda.invoke(request)
    ()
  }

  override def ask[A](msg: A)(implicit enc: Encoder[A]): F[String] = {

    val request = invokeRequest(msg)
    awsLambda
      .invoke(request)
      .map(result => new String(result.getPayload.array))
      .flatMap { payload =>
        parse(payload)
          .flatMap { _.hcursor.get[String]("errorMessage") }
          .fold(
            { _ =>
              F.pure(payload)
            }, { errorMessage =>
              F.raiseError(new Throwable(errorMessage))
            }
          )
      }
  }

  def invokeRequest[A](msg: A)(implicit enc: Encoder[A]): InvokeRequest =
    new InvokeRequest()
      .withFunctionName(functionName)
      .withPayload(msg.asJson.noSpaces)
}

object AwsLambdaActorRef {
  def apply(aws: OfficialAwsLambda, functionName: String)(
      implicit ec: ExecutionContext): AwsLambdaActorRef[Future] = {
    val safeAws = AwsLambda(aws)
    new AwsLambdaActorRef[Future](safeAws, functionName)
  }
}
