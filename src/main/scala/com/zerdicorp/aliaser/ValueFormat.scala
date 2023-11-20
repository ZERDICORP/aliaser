package com.zerdicorp.aliaser

object ValueFormat {
  implicit class StringOps(s: String) {
    def real: String = s.replace("<nl>", "\n").replace("<tb>", "\t")
    def pseudo: String = s.replace("\n", "<nl>").replace("\t", "<tb>")
  }
}
