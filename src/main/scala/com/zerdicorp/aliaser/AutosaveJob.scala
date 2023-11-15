package com.zerdicorp.aliaser

import cats.implicits.catsSyntaxOptionId
import com.zerdicorp.aliaser.FileUtils.rewriteDotFile

import java.util.concurrent._

final case class AutosaveJob(
    asyncDude: AsyncDude,
    protected var job: Option[ScheduledFuture[_]] = None,
) {
  def exec(delayMs: Int, unit: TimeUnit = TimeUnit.MILLISECONDS)(f: U2U = identity): Unit = {
    job.foreach(_.cancel(true))
    job = asyncDude
      .doAfter(delayMs, unit) { _ =>
        rewriteDotFile(Storage.records.values.mkString("\n"))
        f()
      }
      .some
  }
}
