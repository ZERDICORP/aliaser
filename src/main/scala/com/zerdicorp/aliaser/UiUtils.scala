package com.zerdicorp.aliaser

import javafx.beans.value.{ChangeListener, ObservableValue}

object UiUtils {
  def textChangeListener(f: String => Unit): ChangeListener[String] =
    new ChangeListener[String]() {
      override def changed(observableValue: ObservableValue[_ <: String], t: String, text: String): Unit =
        f(text)
    }
}
