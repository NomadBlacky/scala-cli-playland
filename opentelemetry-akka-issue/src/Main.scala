//> using jvm temurin:21
//> using scala 2.13
//> using dep com.typesafe.akka::akka-stream:2.6.20
//> using dep io.opentelemetry:opentelemetry-api:1.46.0
//> using javaOpt -javaagent:./opentelemetry-javaagent-2.9.0.jar
//> using javaProp otel.traces.exporter=logging,otel.metrics.exporter=none,otel.logs.exporter=none
//> using javaProp otel.instrumentation.common.default-enabled=false
//> using javaProp otel.instrumentation.opentelemetry-api.enabled=true
//> using javaProp otel.instrumentation.akka-actor.enabled=true

package scala_cli_playland.opentelemetry_akka_issue

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import io.opentelemetry.api.GlobalOpenTelemetry

import java.time.ZonedDateTime
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main {
  private var endOfMain: ZonedDateTime = _
  
  def main(args: Array[String]): Unit = {
    Runtime.getRuntime.addShutdownHook(new Thread {
      override def run(): Unit = {
        val now = ZonedDateTime.now()
        val duration = now.toEpochSecond - Main.endOfMain.toEpochSecond
        println(s"End of JVM: $now")
        println(s"Duration from the end of main to the end of the JVM: $duration seconds")
      }
    })
    
    val span = GlobalOpenTelemetry.getTracerProvider.get("test").spanBuilder("main").startSpan()

    implicit val system: ActorSystem = ActorSystem("system")
    try {
      val source = Source(1 to 10)
      source.runForeach(println)
    } finally {
      Await.ready(system.terminate(), Duration.Inf)
    }
    span.end()
    endOfMain = ZonedDateTime.now()
  }
}
