package com.zerdicorp.aliaser.my

import com.zerdicorp.aliaser.my.wrapper.ButtonWrapper
import com.zerdicorp.aliaser.{AliasRecordId, RecordSelector}

import scala.collection.mutable

final case class SelectButton(btn: Button, recordId: AliasRecordId) extends ButtonWrapper {
  def active(): Unit = btn.ad("select-btn-active")
  def inactive(): Unit = btn.rm("select-btn-active")
  def block(): Unit = btn.ad("select-btn-blocked")
  def unblock(): Unit =
    if (btn.cnt("select-btn-blocked")) {
      btn.rm("select-btn-blocked")
    }
}

object SelectButton {
  def apply(
      recordId: AliasRecordId,
      selector: RecordSelector,
      swapButton: SwapButton,
      other: mutable.ArrayBuffer[SelectButton],
  ): SelectButton = {
    val btn = Button("::", "select-btn")
    val selectBtn = SelectButton(btn, recordId)
    btn.mod {
      _.onAction = { _ =>
        selector.select(recordId)(
          select = _ => selectBtn.active(),
          unselect = _ => {
            selectBtn.inactive()
            swapButton.disable()
            other.foreach(_.unblock())
          },
          finalize = _ => {
            swapButton.enable()
            other.filter { sb =>
              sb.recordId != selector.first &&
              sb.recordId != selector.second
            }.foreach(_.block())
          },
        )
      }
    }
    selectBtn
  }
}
