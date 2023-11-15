package com.zerdicorp.aliaser.my.wrapper

import com.zerdicorp.aliaser.my.Label

import scala.language.implicitConversions

trait LabelWrapper {
  val lbl: Label
}

object LabelWrapper {
  implicit def wrapper2node(wrapper: LabelWrapper): scalafx.scene.control.Label = wrapper.lbl.node
}
