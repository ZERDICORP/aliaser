package com.zerdicorp.aliaser.my

import com.zerdicorp.aliaser.my.wrapper.LabelWrapper
import scalafx.scene.text.Font

final case class SavedLabel(lbl: Label) extends LabelWrapper

object SavedLabel {
  def apply(): SavedLabel = {
    val label = Label("saved", "saved-lbl").mod { lbl =>
      lbl.disable = true
      lbl.font = Font.font("Arial", 16)
      lbl.opacity = 0.0
    }
    SavedLabel(label)
  }
}
