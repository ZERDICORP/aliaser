package com.zerdicorp.aliaser

import cats.implicits.catsSyntaxOptionId

object OSUtils {
  lazy val shell: Option[Shell] =
    sys.env.get("SHELL") match {
      case Some(value) if value.endsWith("zsh") => Shell.Zsh().some
      case Some(value) if value.endsWith("bash") => Shell.Bash().some
      case _ => None
    }

  sealed trait Shell {
    val rcFile: String
  }
  object Shell {
    final case class Zsh(rcFile: String = ".zshrc") extends Shell
    final case class Bash(rcFile: String = ".bashrc") extends Shell
  }
}
