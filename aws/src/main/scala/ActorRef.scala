package com.github.plippe.cakka.aws

import cats._
import cats.implicits._
import com.amazonaws.services.lambda.AWSLambda
import com.amazonaws.services.lambda.model.{
  InvokeRequest,
  InvokeResult,
  InvocationType
}
import io.circe.Encoder
import io.circe.parser._
import io.circe.syntax._

import com.github.plippe.cakka.core._

class AwsLambdaActorRef[F[_]: MonadError[?[_], Throwable]](awsLambda: AWSLambda,
                                                           functionName: String)
    extends TellableActorRef[F]
    with AskableActorRef[F] {

  override def tell[A](msg: A)(implicit enc: Encoder[A]): F[Unit] = {
    val req = invokeRequest(msg, InvocationType.Event)
    invoke(req).map(_ => ())
  }

  override def ask[A](msg: A)(implicit enc: Encoder[A]): F[String] = {
    val req = invokeRequest(msg, InvocationType.RequestResponse)
    invoke(req)
      .map(res => new String(res.getPayload.array))
      .flatMap { payload =>
        parse(payload)
          .flatMap { _.hcursor.get[String]("errorMessage") }
          .fold(
            { _ =>
              MonadError[F, Throwable].pure(payload)
            }, { errorMessage =>
              MonadError[F, Throwable].raiseError(new Throwable(errorMessage))
            }
          )
      }
  }

  def invoke(req: InvokeRequest): F[InvokeResult] =
    MonadError[F, Throwable].catchNonFatal(awsLambda.invoke(req))

  def invokeRequest[A](msg: A, invocationType: InvocationType)(
      implicit enc: Encoder[A]): InvokeRequest =
    new InvokeRequest()
      .withFunctionName(functionName)
      .withPayload(msg.asJson.noSpaces)
      .withInvocationType(invocationType)
}
