package com.zerdicorp.aliaser.my

import scalafx.geometry.Insets

final case class RecordScroll(
    node: scalafx.scene.control.ScrollPane,
    defaultStyleClass: Seq[String],
) extends Node {
  def greenlight(): Unit = ad("record-scroll-greenlight")
  def lightOff(): Unit = reset()
}

object RecordScroll {
  private val defaultStyleClass = "record-scroll"
  def apply(recordsVBox: scalafx.scene.layout.VBox): RecordScroll = {
    val scrollPane = new scalafx.scene.control.ScrollPane {
      padding = Insets(5)
      content = recordsVBox
      this.setFitToHeight(true)
      this.setFitToWidth(true)
      this.getStyleClass.add(defaultStyleClass)
    }
    RecordScroll(
      node = scrollPane,
      scrollPane.styleClass.toSeq,
    )
  }
}
