package com.github.plippe.cakka.core

import io.circe.Encoder

trait TellableActorRef[F[_]] {

  def ![A](msg: A)(implicit enc: Encoder[A]): F[Unit] = tell(msg)
  def tell[A](msg: A)(implicit enc: Encoder[A]): F[Unit]

}

trait AskableActorRef[F[_]] {

  def ?[A](msg: A)(implicit enc: Encoder[A]): F[String] = ask(msg)
  def ask[A](msg: A)(implicit enc: Encoder[A]): F[String]

}
