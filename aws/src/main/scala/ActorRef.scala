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

  override def tell[A: Encoder](msg: A): F[Unit] = {
    val req = invokeRequest(msg, InvocationType.Event)
    invoke(req).map(_ => ())
  }

  override def ask[A: Encoder, B: Decoder](msg: A): F[B] = {
    val req = invokeRequest(msg, InvocationType.RequestResponse)
    invoke(req)
      .map(res => new String(res.getPayload.array))
      .flatMap(decodePayload[B])
  }

  def invokeRequest[A: Encoder](msg: A,
                                invocationType: InvocationType): InvokeRequest =
    new InvokeRequest()
      .withFunctionName(functionName)
      .withPayload(msg.asJson.noSpaces)
      .withInvocationType(invocationType)

  def invoke(req: InvokeRequest): F[InvokeResult] =
    MonadError[F, Throwable].catchNonFatal(awsLambda.invoke(req))

  def decodePayload[B: Decoder](payload: String): F[B] = {
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
