package com.zerdicorp.aliaser.my

import scala.language.implicitConversions

case class Button(
    node: scalafx.scene.control.Button,
    defaultStyleClass: String,
) extends Node {
  def disable(): Unit = node.disable = true
  def enable(): Unit = node.disable = false

  def mod(f: scalafx.scene.control.Button => Unit): Button = {
    f(node)
    this
  }
}

object Button {
  def apply(_text: String, defaultStyleClass: String): Button =
    Button(
      node = new scalafx.scene.control.Button {
        text = _text
        this.getStyleClass.add(defaultStyleClass)
      },
      defaultStyleClass,
    )
}
