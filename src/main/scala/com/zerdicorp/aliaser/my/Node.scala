package com.zerdicorp.aliaser.my

import scala.language.implicitConversions

trait Node {
  val node: scalafx.scene.Node
  val defaultStyleClass: Seq[String]

  def cnt(styleClass: String): Boolean = node.getStyleClass.contains(styleClass)
  def reset(): Unit = node.styleClass = defaultStyleClass
  def ad(styleClass: String): Unit =
    node.styleClass = defaultStyleClass.filter(!styleClass.startsWith(_)) ++ Seq(styleClass)
}

object Node {
  implicit def node2scalafx(node: Node): scalafx.scene.Node = node.node
}
