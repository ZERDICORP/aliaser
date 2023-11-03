package com.zerdicorp.aliaser

import com.zerdicorp.aliaser.exception.NoRcFileException

import java.io.{File, FileWriter}
import java.nio.file.{Files, Paths}
import scala.io.Source

object FileUtils {
  private val sourceScript: String =
    """
      |if [ -f "$HOME/.aliaser" ]
      |then
      |    source "$HOME/.aliaser"
      |fi
      |""".stripMargin

  private lazy val home = System.getProperty("user.home")
  private lazy val dotFilePath = s"$home/.aliaser"
  private lazy val rcFilePath: String = OSUtils.shell match {
    case Some(shell) => s"$home/${shell.rcFile}"
    case _ => throw NoRcFileException()
  }

  def rewriteDotFile(content: String): Unit = {
    val writer = new FileWriter(dotFilePath)
    writer.write(content)
    writer.close()
  }

  def initDotFile(): Unit = {
    val file = new File(dotFilePath)
    if (!file.exists()) {
      file.createNewFile()
    }
  }

  def appendSourceToRcFile(): Unit = {
    val data = new String(Files.readAllBytes(Paths.get(rcFilePath)))
    if (!data.contains(sourceScript)) {
      val writer = new FileWriter(rcFilePath)
      writer.write(s"$data\n\n$sourceScript")
      writer.close()
    }
  }

  def readDotFile(): Seq[String] = {
    val source = Source.fromFile(dotFilePath)
    val lines = source.getLines().toList
    source.close()
    lines
  }
}
