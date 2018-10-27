package com.github.plippe.cakka.aws

import cats._
import cats.implicits._
import com.amazonaws.services.lambda.AWSLambda
import com.amazonaws.services.lambda.model.{
  InvokeRequest,
  InvokeResult,
  InvocationType
}
import io.circe.{Json, Decoder, Encoder}
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import com.github.plippe.cakka.core._

case class AwsLambdaError(
    errorType: String,
    errorMessage: String,
)

class AwsLambdaActorRef[F[_]: MonadError[?[_], Throwable]](awsLambda: AWSLambda,
                                                           functionName: String)
    extends TellableActorRef[F]
    with AskableActorRef[F] {

  override def tell[A](msg: A)(implicit enc: Encoder[A]): F[Unit] = {
    val req = invokeRequest(msg, InvocationType.Event)
    invoke(req).map(_ => ())
  }

  override def ask[A, B](msg: A)(implicit enc: Encoder[A],
                                 dec: Decoder[B]): F[B] = {
    val req = invokeRequest(msg, InvocationType.RequestResponse)
    invoke(req)
      .map(res => new String(res.getPayload.array))
      .flatMap(decodePayload[B])
  }

  def invokeRequest[A](msg: A, invocationType: InvocationType)(
      implicit enc: Encoder[A]): InvokeRequest =
    new InvokeRequest()
      .withFunctionName(functionName)
      .withPayload(msg.asJson.noSpaces)
      .withInvocationType(invocationType)

  def invoke(req: InvokeRequest): F[InvokeResult] =
    MonadError[F, Throwable].catchNonFatal(awsLambda.invoke(req))

  def decodePayload[B](payload: String)(implicit dec: Decoder[B]): F[B] = {
    val json = parse(payload).getOrElse(Json.Null)
    json
      .as[AwsLambdaError]
      .fold(
        { _ =>
          MonadError[F, Throwable].fromEither(json.as[B])
        }, { error =>
          val message = s"${error.errorType}: ${error.errorMessage}"
          MonadError[F, Throwable].raiseError(new Throwable(message))
        }
      )
  }
}
