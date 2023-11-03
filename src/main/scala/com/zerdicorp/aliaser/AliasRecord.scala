package com.zerdicorp.aliaser

import scala.util.matching.Regex

case class AliasRecord(key: String, value: String) {
  override def toString: String = s"alias $key='$value'"
}

object AliasRecord {
  private val aliasReg: Regex = "alias (.*)=(.*)".r
  private val `valueWith''Reg`: Regex = "'(.*)'".r
  private val valueReg: Regex = "(.*)".r

  def empty: AliasRecord = AliasRecord(key = "", value = "")

  def fromString(raw: String): AliasRecord =
    raw match {
      case aliasReg(key, `valueWith''Reg`(value)) => AliasRecord(key, value)
      case aliasReg(key, valueReg(value)) => AliasRecord(key, value)
    }
}
