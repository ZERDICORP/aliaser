package com.zerdicorp.aliaser.my

import com.zerdicorp.aliaser.U2U
import com.zerdicorp.aliaser.my.wrapper.ButtonWrapper

final case class RemoveButton(btn: Button) extends ButtonWrapper

object RemoveButton {
  def apply(action: U2U): RemoveButton = {
    val btn = Button("✖", "remove-btn").mod { btn =>
      btn.onAction = _ => action()
    }
    RemoveButton(btn)
  }
}
