# CAkka

> This is a brain fart, please ignore ... for now

Serverless infrastructure are triggered via events: e.g. HTTP request, item added to queue, direct call. This resembles
the actor systems where actors are lambdas / functions, and messages are the events.

CAkka, inspired by [Akka](https://akka.io/), is a simple library to `tell`, or `ask` serverless infrastructure to
process messages.
