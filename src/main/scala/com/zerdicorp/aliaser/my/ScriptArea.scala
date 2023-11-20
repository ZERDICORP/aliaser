package com.zerdicorp.aliaser.my

import javafx.beans.value.ChangeListener

final case class ScriptArea(
    node: scalafx.scene.control.TextArea,
    defaultStyleClass: Seq[String],
) extends Node {
  def text(): String = node.text.get()
  def wrong(): Unit = ad("script-area-wrong")
  def correct(): Unit = reset()
  def greenlight(): Unit = ad("script-area-greenlight")
  def lightOff(): Unit = reset()
  def onChange(listener: ChangeListener[String]): Unit = node.textProperty.addListener(listener)
}

object ScriptArea {
  private val defaultStyleClass = "script-area"
  def apply(_text: String): ScriptArea = {
    val area = new scalafx.scene.control.TextArea {
      text = _text
      this.getStyleClass.add(defaultStyleClass)
    }
    ScriptArea(area, area.styleClass.toSeq)
  }
}
