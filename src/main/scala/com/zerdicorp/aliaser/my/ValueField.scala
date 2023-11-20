package com.zerdicorp.aliaser.my

import javafx.beans.value.ChangeListener
import scalafx.scene.control.TextField

final case class ValueField(
    node: scalafx.scene.control.TextField,
    defaultStyleClass: Seq[String],
) extends Node {
  def text: String = node.text.get()
  def wrong(): Unit = ad("value-field-wrong")
  def correct(): Unit = reset()
  def onChange(listener: ChangeListener[String]): Unit = node.textProperty.addListener(listener)
}

object ValueField {
  private val defaultStyleClass = "value-field"
  def apply(_text: String): ValueField = {
    val textField = new TextField {
      this.setText(_text)
      this.getStyleClass.add(defaultStyleClass)
    }
    ValueField(
      node = textField,
      textField.styleClass.toSeq,
    )
  }
}
