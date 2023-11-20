package com.zerdicorp.aliaser

import scala.collection.mutable
import scala.util.matching.Regex

case class AliasRecord(key: String, value: String) {
  override def toString: String = s"$key(){\n$value\n}"
}

object AliasRecord {
  private val fStart: Regex = "(.*)\\(\\)\\{".r
  private val fEnd: Regex = "}".r

  def empty: AliasRecord = AliasRecord(key = "", value = "")

  def fromLines(lines: Seq[String]): Seq[AliasRecord] = {
    val result = mutable.ArrayBuffer.empty[AliasRecord]
    var key = ""
    var value = ""
    lines.foreach {
      case fStart(fName) => key = fName
      case fEnd() =>
        result.addOne(AliasRecord(key, value.dropRight(1)))
        value = ""
        key = ""
      case line => value += line + "\n"
    }
    result.toSeq.reverse
  }
}
