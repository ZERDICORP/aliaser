package com.zerdicorp.aliaser.exception

case class NoRcFileException(msg: String = NoRcFileException.defaultMessage) extends RuntimeException

object NoRcFileException {
  val defaultMessage: String = "В вашей системе не найден rc file, нахуй windows :)"
}
