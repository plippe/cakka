package com.github.plippe.cakka.aws

import com.amazonaws.services.lambda.AWSLambda
import com.amazonaws.services.lambda.model.{InvokeRequest, InvokeResult}
import scala.concurrent.{ExecutionContext, Future}

trait AwsLambda[F[_]] {
  def invoke(request: InvokeRequest): F[InvokeResult]
}

object AwsLambda {
  def apply(client: AWSLambda)(
      implicit ec: ExecutionContext): AwsLambda[Future] =
    new AwsLambda[Future] {
      def invoke(request: InvokeRequest) = Future(client.invoke(request))
    }
}
