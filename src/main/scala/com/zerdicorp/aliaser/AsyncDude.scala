package com.zerdicorp.aliaser

import java.util.concurrent._

final case class AsyncDude(
    protected val scheduledExecutor: ScheduledExecutorService,
) {
  def stfu(): Unit = scheduledExecutor.shutdown()
  def doAfter(duration: Int, unit: TimeUnit = TimeUnit.MILLISECONDS)(f: U2U): ScheduledFuture[_] =
    scheduledExecutor.schedule(
      new Runnable { def run(): Unit = f() },
      duration,
      unit,
    )
}

object AsyncDude {
  def apply(): AsyncDude = {
    val scheduledExecutor = Executors.newScheduledThreadPool(16)
    AsyncDude(scheduledExecutor)
  }
}
