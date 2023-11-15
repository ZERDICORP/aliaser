package com.zerdicorp.aliaser

import scala.collection.mutable

object Storage {
  type Records = mutable.LinkedHashMap[AliasRecordId, AliasRecord]
  type Selects = mutable.ArrayBuffer[my.SelectButton]

  lazy val records: Storage.Records = new Storage.Records()
  lazy val selects: Storage.Selects = new Storage.Selects()

  def prependRecord(id: AliasRecordId, record: AliasRecord): Unit = {
    val copy = records.toList
    records.clear
    records.put(id, record)
    records.addAll(copy)
  }
}
