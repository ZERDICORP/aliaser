package com.zerdicorp.aliaser

import com.zerdicorp.aliaser.UiUtils.textChangeListener
import com.zerdicorp.aliaser.ValueFormat.StringOps
import com.zerdicorp.aliaser.my.{KeyField, ScriptArea, ValueField}
import javafx.beans.value.ChangeListener

object KeyValueChangeFlow {
  import Storage._

  def modal(area: ScriptArea, recordId: AliasRecordId, onChange: U2U): Unit = {
    val changeFlow: ChangeListener[String] = {
      textChangeListener { text =>
        val curr = records(recordId)
        if (text.isEmpty) {
          area.wrong()
        } else {
          area.correct()
          records(recordId) = records(recordId).copy(value = text)
          if (curr.value.isEmpty) {
            area.wrong()
          } else {
            if (area.text != curr.value) onChange()
          }
        }
      }
    }
    area.onChange(changeFlow)
  }

  def apply(keyField: KeyField, valueField: ValueField, recordId: AliasRecordId, onChange: U2U): Unit = {
    val changeFlow: (AliasRecord => String => AliasRecord) => FieldType => ChangeListener[String] = { f => fieldType =>
      textChangeListener { text =>
        def error(): Unit = {
          fieldType match {
            case FieldType.KeyField => keyField.wrong()
            case FieldType.ValueField => valueField.wrong()
          }
        }

        val curr = records(recordId)
        if (text.isEmpty) {
          error()
        } else {
          fieldType match {
            case FieldType.KeyField => keyField.correct()
            case FieldType.ValueField => valueField.correct()
          }
          records(recordId) = f(records(recordId))(text.real)
          if (curr.key.isEmpty || curr.value.isEmpty) {
            if (curr.key.isEmpty && fieldType != FieldType.KeyField)
              keyField.wrong()
            if (curr.value.isEmpty && fieldType != FieldType.ValueField)
              valueField.wrong()
          } else {
            val changed = fieldType match {
              case FieldType.KeyField => keyField.text != curr.key
              case FieldType.ValueField => valueField.text != curr.value
            }
            if (changed) onChange()
          }
        }
      }
    }
    keyField.onChange(changeFlow(r => t => r.copy(key = t))(FieldType.KeyField))
    valueField.onChange(changeFlow(r => t => r.copy(value = t))(FieldType.ValueField))
  }
}
