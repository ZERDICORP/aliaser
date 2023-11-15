package com.zerdicorp.aliaser

import scalafx.scene.Node
import scalafx.scene.layout.Pane

object Implicits {
  implicit class NodeOps(node: Node) {
    def putChilds(childs: Node*): Unit =
      node.asInstanceOf[Pane].children = childs
    def prependChild(child: Node): Unit =
      node.asInstanceOf[Pane].children.insert(0, child)
  }
}
