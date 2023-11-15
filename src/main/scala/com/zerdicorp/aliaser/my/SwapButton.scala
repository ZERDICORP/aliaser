package com.zerdicorp.aliaser.my

import com.zerdicorp.aliaser.my.wrapper.ButtonWrapper

final case class SwapButton(btn: Button) extends ButtonWrapper {
  def disable(): Unit = btn.disable()
  def enable(): Unit = btn.enable()
}

object SwapButton {
  def apply(action: Unit => Unit): SwapButton = {
    val btn = Button("[ swap ]", "swap-btn").mod { btn =>
      btn.onAction = _ => action()
      btn.disable = true
    }
    SwapButton(btn)
  }
}
