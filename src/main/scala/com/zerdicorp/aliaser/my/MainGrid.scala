package com.zerdicorp.aliaser.my

import scalafx.geometry.HPos
import scalafx.scene.layout.{ColumnConstraints, GridPane, Priority}

final case class MainGrid(
    node: scalafx.scene.layout.GridPane,
    defaultStyleClass: String,
) extends Node {
  def greenlight(): Unit = ad("main-grid-greenlight")
  def lightOff(): Unit = rm("main-grid-greenlight")
}

object MainGrid {
  private val defaultStyleClass = "main-grid"
  def apply(
      recordScroll: RecordScroll,
      addButton: AddButton,
  ): MainGrid = {
    val gridPane = new scalafx.scene.layout.GridPane {
      private val colConst = new ColumnConstraints
      colConst.percentWidth = 100
      columnConstraints.addAll(colConst)

      vgap = 10

      GridPane.setVgrow(recordScroll, Priority.Always)
      GridPane.setHalignment(recordScroll, HPos.Center)
      GridPane.setHalignment(addButton, HPos.Center)

      add(recordScroll, 0, 0)
      add(addButton, 0, 1)

      this.getStyleClass.add(defaultStyleClass)
    }
    MainGrid(
      node = gridPane,
      defaultStyleClass,
    )
  }
}
