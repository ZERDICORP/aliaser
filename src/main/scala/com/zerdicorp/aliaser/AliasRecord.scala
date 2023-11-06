package com.zerdicorp.aliaser

import scala.util.matching.Regex

case class AliasRecord(key: String, value: String) {
  override def toString: String =
    if (value.contains("$")) {
      s"$key() { $value }"
    } else s"alias $key='$value'"
}

object AliasRecord {
  private val aliasReg: Regex = "alias (.*)='(.*)'".r
  private val fnReg: Regex = "(.*)\\(\\) \\{ (.*) }".r

  def empty: AliasRecord = AliasRecord(key = "", value = "")

  def fromString(raw: String): AliasRecord =
    raw match {
      case aliasReg(key, value) => AliasRecord(key, value)
      case fnReg(key, value) => AliasRecord(key, value)
    }
}
