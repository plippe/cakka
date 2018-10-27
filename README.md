# CAkka

[![Build Status](https://app.wercker.com/status/5ca500b85572d470e27868179e76fcd3/s/master)](https://app.wercker.com/project/byKey/5ca500b85572d470e27868179e76fcd3)

> This is a brain fart, please ignore ... for now

Serverless infrastructure are triggered via events: e.g. HTTP request, item added to queue, direct call. This resembles
the actor systems where actors are lambdas / functions, and messages are the events.

CAkka, inspired by [Akka](https://akka.io/), is a simple library to `tell`, or `ask` serverless infrastructure to
process messages.
