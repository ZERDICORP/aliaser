package com.zerdicorp.aliaser

sealed trait FieldType
object FieldType {
  final case object KeyField extends FieldType
  final case object ValueField extends FieldType
}
