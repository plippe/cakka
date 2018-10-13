import com.amazonaws.services.lambda.{AWSLambda, AWSLambdaClientBuilder}
import com.amazonaws.services.lambda.model.{InvokeRequest, InvokeResult}
import io.circe.Encoder
import io.circe.parser._
import io.circe.syntax._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

trait ActorRef {
  def ![A](msg: A)(implicit enc: Encoder[A]): Unit = tell(msg)
  def tell[A](msg: A)(implicit enc: Encoder[A]): Unit

  def ?[A](msg: A)(implicit enc: Encoder[A],
                   ec: ExecutionContext): Future[String] = ask(msg)
  def ask[A](msg: A)(implicit enc: Encoder[A],
                     ec: ExecutionContext): Future[String]
}

class AwsLambda(awsLambdaClient: AWSLambda, functionName: String)
    extends ActorRef {

  def tell[A](msg: A)(implicit enc: Encoder[A]): Unit = {
    invoke(invokeRequest(msg))
    ()
  }

  def ask[A](msg: A)(implicit enc: Encoder[A],
                     ec: ExecutionContext): Future[String] = {

    val request = invokeRequest(msg)
    Future(invoke(request))
      .map { res =>
        new String(res.getPayload.array)
      }
      .flatMap { payload =>
        parse(payload)
          .flatMap { _.hcursor.get[String]("errorMessage") }
          .fold(
            { _ =>
              Future.successful(payload)
            }, { errorMessage =>
              Future.failed(new Throwable(errorMessage))
            }
          )
      }
  }

  def invoke(invokeRequest: InvokeRequest): InvokeResult =
    awsLambdaClient.invoke(invokeRequest)

  def invokeRequest[A](msg: A)(implicit enc: Encoder[A]): InvokeRequest =
    new InvokeRequest()
      .withFunctionName(functionName)
      .withPayload(msg.asJson.noSpaces)
}

object Main extends App {
  println("Hello World")

  val actor = new AwsLambda(AWSLambdaClientBuilder.defaultClient, "my-lambda")

  println("TELL - START")
  actor ! "TELL"
  println("TELL - END")

  import scala.concurrent.ExecutionContext.Implicits.global
  println("ASK - START")
  val f = actor ? "ASK"
  println("ASK - END")

  println("ASK - WAITING")
  Await.result(f, Duration.Inf)
  println(s"ASK - $f")
}
