package com.zerdicorp.aliaser

import cats.implicits.catsSyntaxOptionId
import com.zerdicorp.aliaser.FileUtils.{appendSourceToRcFile, initDotFile, readDotFile, rewriteDotFile}
import com.zerdicorp.aliaser.UiUtils.textChangeListener
import com.zerdicorp.aliaser.app.AliasId
import javafx.beans.value.ChangeListener
import scalafx.animation.FadeTransition
import scalafx.application.{JFXApp, Platform}
import scalafx.geometry.{HPos, Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, ScrollPane, TextField}
import scalafx.scene.layout._
import scalafx.scene.paint.Color
import scalafx.scene.text.Font
import scalafx.util.Duration

import java.util.concurrent.{Executors, ScheduledFuture, TimeUnit}
import scala.collection.mutable

object App extends JFXApp {
  private lazy val records = mutable.LinkedHashMap[AliasId, AliasRecord]()
  private val scheduledExecutor = Executors.newScheduledThreadPool(16)
  private var autosaveJob: Option[ScheduledFuture[_]] = None
  private var statusChangingInProgress: Boolean = false

  private val recordsBox = new VBox {
    spacing = 5
  }
  private val status = new Label {
    text = "saved"
    font = Font.font("Arial", 16)
    textFill = Color.Green
    opacity = 0.0
    style = "-fx-font-weight: bold"
  }
  private val statusDisappearanceTransition = new FadeTransition(Duration(500), status)
  statusDisappearanceTransition.fromValue = 1.0
  statusDisappearanceTransition.toValue = 0.0
  private val statusDisplayTransition = new FadeTransition(Duration(250), status)
  statusDisplayTransition.fromValue = 0.0
  statusDisplayTransition.toValue = 1.0

  private val addButton = new Button {
    text = "+ Добавить"
    style = "-fx-cursor: hand;" +
      "-fx-background-color: green;" +
      "-fx-text-fill: white;" +
      "-fx-font-size: 14pt;" +
      "-fx-background-radius: 20;" +
      "-fx-pref-width: 150;" +
      "-fx-pref-height: 50;"
    onAction = _ => addRecord()
  }

  def addRecord(recordOpt: Option[AliasRecord] = None): Unit = {
    val record = recordOpt.getOrElse(AliasRecord.empty)
    val recordId = records.size
    val box = new HBox {
      spacing = 5
    }
    val recordBox = new HBox {
//      style = "-fx-border-color: purple;"
    }
    val keyField = new TextField()
    val valueField = new TextField()
    val removeBtn = new Button {
      text = "✖"
      style = "-fx-cursor: hand;" +
        "-fx-background-color: white;" +
        "-fx-text-fill: red;" +
        "-fx-border-color: red;" +
        "-fx-background-radius: 20;" +
        "-fx-border-radius: 20;"
    }

    keyField.style = "-fx-background-radius: 5 0 0 5;"
    valueField.style = "-fx-background-radius: 0 5 5 0;"

    keyField.setText(record.key)
    valueField.setText(record.value)

    HBox.setHgrow(keyField, Priority.Always)
    HBox.setHgrow(valueField, Priority.Always)

    def delayedJob(duration: Int, _type: TimeUnit)(f: => Unit): ScheduledFuture[_] =
      scheduledExecutor.schedule(
        new Runnable { def run(): Unit = f },
        duration,
        _type,
      )

    def autosave(delayMs: Int): Unit = {
      autosaveJob.foreach(_.cancel(true))
      autosaveJob = delayedJob(delayMs, TimeUnit.MILLISECONDS) {
        rewriteDotFile(records.values.mkString("\n"))
        Platform.runLater(() => statusDisplayTransition.play())
        if (!statusChangingInProgress) {
          statusChangingInProgress = true
          delayedJob(1500, TimeUnit.MILLISECONDS) {
            Platform.runLater(() => statusDisappearanceTransition.play())
            statusChangingInProgress = false
          }.some
        }
      }.some
    }

    val changeFlow: (AliasRecord => String => AliasRecord) => ChangeListener[String] = { f =>
      textChangeListener { text =>
        val curr = records(recordId)
        if (text.isEmpty) {
          keyField.style = "-fx-border-color: red;"
          valueField.style = "-fx-border-color: red;"
        } else {
          keyField.style = "-fx-background-radius: 5 0 0 5;"
          valueField.style = "-fx-background-radius: 0 5 5 0;"
          records(recordId) = f(records(recordId))(text)
          if (curr.key.isEmpty || curr.value.isEmpty) {
            keyField.style = "-fx-border-color: red;"
            valueField.style = "-fx-border-color: red;"
          } else autosave(delayMs = 500)
        }
      }
    }

    keyField.textProperty.addListener(changeFlow(r => t => r.copy(key = t)))
    valueField.textProperty.addListener(changeFlow(r => t => r.copy(value = t)))

    recordBox.children = List(keyField, valueField)
    box.children = List(recordBox, removeBtn)
    recordsBox.children.insert(0, box)

    removeBtn.onAction = _ => {
      records.remove(recordId)
      recordsBox.children.remove(box)
      autosave(delayMs = 0)
    }

    prependRecord(recordId, record)
  }

  private def prependRecord(id: AliasId, record: AliasRecord): Unit = {
    val copy = records.toMap
    records.clear
    records.put(id, record)
    records.addAll(copy)
  }

  stage = new JFXApp.PrimaryStage {
    title = "Aliaser"
    scene = new Scene(400, 500) {
      private val borderPane = new BorderPane {
        private val vbox = new VBox() {
          spacing = 5
          padding = Insets(10)
          alignment = Pos.Center
          private val gridPane = new GridPane {
            vgap = 10
            private val scroll = new ScrollPane {
              padding = Insets(5)
              content = recordsBox
            }
            scroll.setFitToHeight(true)
            scroll.setFitToWidth(true)
            scroll.style = "-fx-background-color: transparent;" +
              "-fx-border-color: transparent transparent gray transparent;"

            val colConst = new ColumnConstraints
            colConst.percentWidth = 100
            columnConstraints.addAll(colConst)

            GridPane.setVgrow(scroll, Priority.Always)
            GridPane.setHalignment(scroll, HPos.Center)
            GridPane.setHalignment(addButton, HPos.Center)
            GridPane.setHalignment(status, HPos.Center)

            add(scroll, 0, 0)
            add(addButton, 0, 1)
            add(status, 0, 2)
//            style = "-fx-border-color: green;"
          }
          VBox.setVgrow(gridPane, Priority.Always)
          children = List(gridPane)
//          style = "-fx-border-color: red;"
        }
        center = vbox
      }
      root = borderPane
    }
  }

  override def stopApp(): Unit = {
    scheduledExecutor.shutdown()
    super.stopApp()
  }

  def initialize(): Unit = {
    Thread.setDefaultUncaughtExceptionHandler((_: Thread, e: Throwable) => {
      Notification.initializationError(e.getMessage)
    })

    initDotFile()
    readDotFile().reverse.map(AliasRecord.fromString(_).some).foreach(addRecord)
    appendSourceToRcFile()
  }

  initialize()
}
