package com.zerdicorp.aliaser.my

import com.zerdicorp.aliaser.my.wrapper.ButtonWrapper

final case class AddButton(btn: Button) extends ButtonWrapper

object AddButton {
  def apply(action: Unit => Unit): AddButton = {
    val btn = Button("+ Добавить", "add-btn").mod {
      _.onAction = _ => action()
    }
    AddButton(btn)
  }
}
