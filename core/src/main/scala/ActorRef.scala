package com.github.plippe.cakka.core

import io.circe.{Decoder, Encoder}

trait TellableActorRef[F[_]] {

  def ![A](msg: A)(implicit enc: Encoder[A]): F[Unit] = tell(msg)
  def tell[A](msg: A)(implicit enc: Encoder[A]): F[Unit]

}

trait AskableActorRef[F[_]] {

  def ?[A, B](msg: A)(implicit enc: Encoder[A], dec: Decoder[B]): F[B] =
    ask(msg)
  def ask[A, B](msg: A)(implicit enc: Encoder[A], dec: Decoder[B]): F[B]

}
