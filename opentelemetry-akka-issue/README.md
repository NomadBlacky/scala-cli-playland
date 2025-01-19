# opentelemetry-akka-issue

This is a minimal example to reproduce an issue with OpenTelemetry and Akka.

## Problem

When using OpenTelemetry Java agent v2.9.0 with Akka, it takes a long time from the end of the main method to the end of the JVM.

## How to reproduce

1. Install scala-cli using asdf

```shell
asdf install
```

2. Run the application

```shell
scala-cli run .
```

Below is the output example.

```
Compiling project (Scala 2.13.16, JVM (temurin:21))
Compiled project (Scala 2.13.16, JVM (temurin:21))
[hint] ./src/Main.scala:3:15
[hint] "akka-stream is outdated, update to 2.8.8"
[hint]      akka-stream 2.6.20 -> com.typesafe.akka::akka-stream:2.8.8
[hint] //> using dep com.typesafe.akka::akka-stream:2.6.20
[hint]               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
[otel.javaagent 2025-01-19 22:53:48:796 +0900] [main] INFO io.opentelemetry.javaagent.tooling.VersionLogger - opentelemetry-javaagent - version: 2.9.0
1
2
3
4
5
6
7
8
9
10
[INFO] [01/19/2025 22:53:49.848] [main] [CoordinatedShutdown(akka://system)] Running CoordinatedShutdown with reason [ActorSystemTerminateReason]
[otel.javaagent 2025-01-19 22:53:49:872 +0900] [main] INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'main' : 471e26862d793f7755c5fdc66e57f368 0412d6029ea90748 INTERNAL [tracer: test:] AttributesMap{data={thread.name=main, thread.id=1}, capacity=128, totalAddedValues=2}
End of JVM: 2025-01-19T22:59:49.890798+09:00[Asia/Tokyo]
Duration from the end of main to the end of the JVM: 360 seconds
```

You can see that it takes a long time from the end of the main method to the end of the JVM.

## Workaround

### Using the system property `otel.instrumentation.akka-actor.enabled=false`

Fix `Main.scala` as follows.

```diff
-//> using javaProp otel.instrumentation.akka-actor.enabled=true
+//> using javaProp otel.instrumentation.akka-actor.enabled=false
```

```
$ scala-cli run .
[hint] ./src/Main.scala:3:15
[hint] "akka-stream is outdated, update to 2.8.8"
[hint]      akka-stream 2.6.20 -> com.typesafe.akka::akka-stream:2.8.8
[hint] //> using dep com.typesafe.akka::akka-stream:2.6.20
[hint]               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
[otel.javaagent 2025-01-19 23:06:16:452 +0900] [main] INFO io.opentelemetry.javaagent.tooling.VersionLogger - opentelemetry-javaagent - version: 2.9.0
1
2
3
4
5
6
7
8
9
10
[INFO] [01/19/2025 23:06:17.436] [main] [CoordinatedShutdown(akka://system)] Running CoordinatedShutdown with reason [ActorSystemTerminateReason]
[otel.javaagent 2025-01-19 23:06:17:463 +0900] [main] INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'main' : 5a4ca7e47cabed7888a6c079d5d2402b a5f2479397067921 INTERNAL [tracer: test:] AttributesMap{data={thread.name=main, thread.id=1}, capacity=128, totalAddedValues=2}
End of JVM: 2025-01-19T23:06:17.466997+09:00[Asia/Tokyo]
Duration from the end of main to the end of the JVM: 0 seconds
```

However, this is not a valid workaround as it prevents Akka Actor context propagation.

### Using opentelemetry-javaagent v2.8.0 or earlier

Fix `Main.scala` as follows.

```diff
-//> using javaOpt -javaagent:./opentelemetry-javaagent-2.9.0.jar
+//> using javaOpt -javaagent:./opentelemetry-javaagent-2.8.0.jar
```

```
$ scala-cli run .
Compiling project (Scala 2.13.16, JVM (temurin:21))
Compiled project (Scala 2.13.16, JVM (temurin:21))
[hint] ./src/Main.scala:3:15
[hint] "akka-stream is outdated, update to 2.8.8"
[hint]      akka-stream 2.6.20 -> com.typesafe.akka::akka-stream:2.8.8
[hint] //> using dep com.typesafe.akka::akka-stream:2.6.20
[hint]               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
[otel.javaagent 2025-01-19 23:09:38:475 +0900] [main] INFO io.opentelemetry.javaagent.tooling.VersionLogger - opentelemetry-javaagent - version: 2.8.0
1
2
3
4
5
6
7
8
9
10
[INFO] [01/19/2025 23:09:39.575] [main] [CoordinatedShutdown(akka://system)] Running CoordinatedShutdown with reason [ActorSystemTerminateReason]
[otel.javaagent 2025-01-19 23:09:39:595 +0900] [main] INFO io.opentelemetry.exporter.logging.LoggingSpanExporter - 'main' : 8f7e0e32c1f59fa2096ac6e7aa8d1d41 dfd111bdb180a67a INTERNAL [tracer: test:] AttributesMap{data={thread.id=1, thread.name=main}, capacity=128, totalAddedValues=2}
End of JVM: 2025-01-19T23:09:39.597015+09:00[Asia/Tokyo]
Duration from the end of main to the end of the JVM: 0 seconds
```

This is a valid workaround, but it is not a solution because it uses an older version of the OpenTelemetry Java agent.
