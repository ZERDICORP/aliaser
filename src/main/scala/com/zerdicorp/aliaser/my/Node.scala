package com.zerdicorp.aliaser.my

import scala.language.implicitConversions

trait Node {
  val node: scalafx.scene.Node
  val defaultStyleClass: String

  def cnt(styleClass: String): Boolean =
    node.getStyleClass.contains(styleClass)

  def rm(styleClass: String): Unit = {
    node.getStyleClass.remove(styleClass)
    node.getStyleClass.add(defaultStyleClass)
  }

  def ad(styleClass: String): Unit = {
    node.getStyleClass.remove(defaultStyleClass)
    node.getStyleClass.add(styleClass)
  }
}

object Node {
  implicit def node2scalafx(node: Node): scalafx.scene.Node = node.node
}
