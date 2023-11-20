package com.zerdicorp.aliaser.my

case class Label(
    node: scalafx.scene.control.Label,
    defaultStyleClass: Seq[String],
) extends Node {
  def mod(f: scalafx.scene.control.Label => Unit): Label = {
    f(node)
    this
  }
}

object Label {
  def apply(_text: String, defaultStyleClass: String): Label = {
    val lbl = new scalafx.scene.control.Label {
      text = _text
      this.getStyleClass.add(defaultStyleClass)
    }
    Label(
      node = lbl,
      lbl.styleClass.toSeq,
    )
  }
}
