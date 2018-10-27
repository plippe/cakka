package com.github.plippe.cakka.core

import io.circe.{Decoder, Encoder}

trait TellableActorRef[F[_]] {

  def ![A: Encoder](msg: A): F[Unit] = tell(msg)
  def tell[A: Encoder](msg: A): F[Unit]

}

trait AskableActorRef[F[_]] {

  def ?[A: Encoder, B: Decoder](msg: A): F[B] = ask(msg)
  def ask[A: Encoder, B: Decoder](msg: A): F[B]

}
