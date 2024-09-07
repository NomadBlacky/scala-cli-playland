//> using jvm zulu:17
//> using scala 3.5

@main def showJvmStartupDuration(): Unit = {
  val processStart = ProcessHandle.current().info().startInstant().get()
  val now = java.time.Instant.now()
  
  println(s"JVM startup duration: ${java.time.Duration.between(processStart, now).toMillis} ms")
}
