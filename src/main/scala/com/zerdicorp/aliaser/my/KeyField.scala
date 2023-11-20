package com.zerdicorp.aliaser.my

import javafx.beans.value.ChangeListener
import scalafx.geometry.Pos.CenterRight
import scalafx.scene.control.TextField

final case class KeyField(
    node: scalafx.scene.control.TextField,
    defaultStyleClass: Seq[String],
) extends Node {
  def text: String = node.text.get()
  def wrong(): Unit = ad("key-field-wrong")
  def correct(): Unit = reset()
  def onChange(listener: ChangeListener[String]): Unit = node.textProperty.addListener(listener)
}

object KeyField {
  private val defaultStyleClass = "key-field"
  def apply(_text: String): KeyField = {
    val textField = new TextField {
      this.setAlignment(CenterRight)
      this.setText(_text)
      this.getStyleClass.add(defaultStyleClass)
    }
    KeyField(
      node = textField,
      textField.styleClass.toSeq,
    )
  }
}
