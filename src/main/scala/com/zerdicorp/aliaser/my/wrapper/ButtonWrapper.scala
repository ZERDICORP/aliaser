package com.zerdicorp.aliaser.my.wrapper

import com.zerdicorp.aliaser.my.Button

import scala.language.implicitConversions

trait ButtonWrapper {
  val btn: Button
}

object ButtonWrapper {
  implicit def wrapper2node(wrapper: ButtonWrapper): scalafx.scene.control.Button = wrapper.btn.node
}
