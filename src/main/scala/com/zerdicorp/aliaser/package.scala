package com.zerdicorp

package object aliaser {
  type AliasRecordId = Int
  type U2U = Unit => Unit

  Thread.setDefaultUncaughtExceptionHandler((_: Thread, e: Throwable) => {
    Notification.initializationError(e.getMessage)
  })
}
