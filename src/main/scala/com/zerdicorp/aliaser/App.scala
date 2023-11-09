package com.zerdicorp.aliaser

import cats.implicits.catsSyntaxOptionId
import com.zerdicorp.aliaser.FileUtils.{appendSourceToRcFile, initDotFile, readDotFile, rewriteDotFile}
import com.zerdicorp.aliaser.UiUtils.textChangeListener
import javafx.beans.value.ChangeListener
import scalafx.animation.FadeTransition
import scalafx.application.{JFXApp, Platform}
import scalafx.geometry.Pos.CenterRight
import scalafx.geometry.{HPos, Insets}
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
  private var firstSelectedRecord: AliasId = -1 // for changing order
  private var secondSelectedRecord: AliasId = -1 // for changing order
  private val scheduledExecutor = Executors.newScheduledThreadPool(16)
  private var autosaveJob: Option[ScheduledFuture[_]] = None
  private var statusChangingInProgress: Boolean = false
  private var gridPane: GridPane = new GridPane()
  private val defaultGridPaneStyle: String = "-fx-border-color: gray;" + "" +
    "-fx-border-radius: 5 5 5 5;"
  private var scrollPane: ScrollPane = new ScrollPane()
  private val defaultScrollPaneStyle: String = "-fx-background-color: transparent;" +
    "-fx-border-color: transparent transparent gray transparent;"
  private val selectBtnDefaultStyle: String = "-fx-cursor: hand;" +
    "-fx-background-color: #141414;" +
    "-fx-text-fill: gray;" +
    "-fx-border-color: gray;" +
    "-fx-background-radius: 5;" +
    "-fx-border-radius: 5;"

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

  private val swapButton = new Button {
    val defaultStyle: String = "-fx-cursor: hand;" +
      "-fx-background-color: white;" +
      "-fx-text-fill: #141414;" +
      "-fx-font-size: 10pt;" +
      "-fx-background-radius: 4;" +
      "-fx-pref-width: 80;" +
      "-fx-pref-height: 30;" +
      "-fx-font-weight: bold"
    text = "[ swap ]"
    style = defaultStyle
    onAction = _ => swapRecords()
    disable = true
  }

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

  def swapRecords(): Unit = {
    // swap in linked list
    val temp = records(firstSelectedRecord)
    records(firstSelectedRecord) = records(secondSelectedRecord)
    records(secondSelectedRecord) = temp
    // swap in records box
    autosave(
      delayMs = 0,
      _ => {
        Platform.runLater(() => {
          records.clear()
          recordsBox.children.clear()
          readDotFile().reverse.map(AliasRecord.fromString(_).some).foreach(addRecord)
          swapButton.disable = true
          firstSelectedRecord = -1
          secondSelectedRecord = -1
        })
      },
    )
  }

  def delayedJob(duration: Int, _type: TimeUnit)(f: => Unit): ScheduledFuture[_] =
    scheduledExecutor.schedule(
      new Runnable {
        def run(): Unit = f
      },
      duration,
      _type,
    )

  def autosave(delayMs: Int, f: Unit => Unit = identity): Unit = {
    autosaveJob.foreach(_.cancel(true))
    autosaveJob = delayedJob(delayMs, TimeUnit.MILLISECONDS) {
      rewriteDotFile(records.values.mkString("\n"))
      f()
      Platform.runLater(() => {
        statusDisplayTransition.play()
        gridPane.style = defaultGridPaneStyle + "-fx-border-color: green;"
        scrollPane.style = defaultScrollPaneStyle + "-fx-border-color: transparent transparent green transparent;"
      })
      if (!statusChangingInProgress) {
        statusChangingInProgress = true
        delayedJob(1500, TimeUnit.MILLISECONDS) {
          Platform.runLater(() => {
            statusDisappearanceTransition.play()
            gridPane.style = defaultGridPaneStyle
            scrollPane.style = defaultScrollPaneStyle
          })
          statusChangingInProgress = false
        }.some
      }
    }.some
  }

  def addRecord(recordOpt: Option[AliasRecord] = None): Unit = {
    val record = recordOpt.getOrElse(AliasRecord.empty)
    val recordId = records.size
    val box = new HBox {
      spacing = 10
    }
    val recordBox = new HBox {
//      style = "-fx-border-color: purple;"
    }
    val selectBtn = new Button {
      text = "::"
      style = selectBtnDefaultStyle
    }
    val keyField = new TextField {
      val defaultStyle: String = "-fx-border-radius: 5 0 0 5;" +
        "-fx-text-fill: white;" +
        "-fx-background-color: #141414;" +
        "-fx-border-color: gray;"
      style = defaultStyle
      this.setAlignment(CenterRight)
    }
    val valueField = new TextField {
      val defaultStyle: String = "-fx-border-radius: 0 5 5 0;" +
        "-fx-text-fill: white;" +
        "-fx-background-color: #141414;" +
        "-fx-border-color: gray;"
      style = defaultStyle
    }
    val removeBtn = new Button {
      text = "✖"
      style = "-fx-cursor: hand;" +
        "-fx-background-color: #141414;" +
        "-fx-text-fill: red;" +
        "-fx-border-color: red;" +
        "-fx-background-radius: 20;" +
        "-fx-border-radius: 20;"
    }

    keyField.setText(record.key)
    valueField.setText(record.value)

    HBox.setHgrow(valueField, Priority.Always)
    HBox.setHgrow(recordBox, Priority.Always)

    val changeFlow: (AliasRecord => String => AliasRecord) => FieldType => ChangeListener[String] = { f => fieldType =>
      textChangeListener { text =>
        def error(): Unit = {
          fieldType match {
            case FieldType.KeyField =>
              keyField.style = keyField.defaultStyle + "-fx-border-color: red;"
            case FieldType.ValueField =>
              valueField.style = valueField.defaultStyle + "-fx-border-color: red;"
          }
        }
        if (text.contains("'")) {
          error()
        } else {
          val curr = records(recordId)
          if (text.isEmpty) {
            error()
          } else {
            fieldType match {
              case FieldType.KeyField => keyField.style = keyField.defaultStyle
              case FieldType.ValueField => valueField.style = valueField.defaultStyle
            }
            records(recordId) = f(records(recordId))(text)
            if (curr.key.isEmpty || curr.value.isEmpty) {
              if (curr.key.isEmpty && fieldType != FieldType.KeyField)
                keyField.style = keyField.defaultStyle + "-fx-border-color: red;"
              if (curr.value.isEmpty && fieldType != FieldType.ValueField)
                valueField.style = valueField.defaultStyle + "-fx-border-color: red;"
            } else {
              val needAutosave = fieldType match {
                case FieldType.KeyField => keyField.text() != curr.key
                case FieldType.ValueField => valueField.text() != curr.value
              }
              if (needAutosave) autosave(delayMs = 500)
            }
          }
        }
      }
    }

    keyField.textProperty.addListener(changeFlow(r => t => r.copy(key = t))(FieldType.KeyField))
    valueField.textProperty.addListener(changeFlow(r => t => r.copy(value = t))(FieldType.ValueField))

    recordBox.children = List(keyField, valueField)
    box.children = List(selectBtn, recordBox, removeBtn)
    recordsBox.children.insert(0, box)

    selectBtn.onAction = _ => {
      if (firstSelectedRecord == recordId || secondSelectedRecord == recordId) {
        if (firstSelectedRecord == recordId) {
          firstSelectedRecord = -1
          selectBtn.style = selectBtnDefaultStyle
        } else if (secondSelectedRecord == recordId) {
          secondSelectedRecord = -1
          selectBtn.style = selectBtnDefaultStyle
        }
        swapButton.disable = true
      } else {
        if (firstSelectedRecord == -1) {
          firstSelectedRecord = recordId
          selectBtn.style = selectBtnDefaultStyle + "-fx-text-fill: cyan;" +
            "-fx-border-color: cyan;" +
            "-fx-font-weight: bold"
        } else {
          if (secondSelectedRecord == -1) {
            secondSelectedRecord = recordId
            selectBtn.style = selectBtnDefaultStyle + "-fx-text-fill: yellow;" +
              "-fx-border-color: yellow;" +
              "-fx-font-weight: bold"
          }
        }
        if (firstSelectedRecord != -1 && secondSelectedRecord != -1) {
          swapButton.disable = false
        }
      }
    }

    removeBtn.onAction = _ => {
      records.remove(recordId)
      recordsBox.children.remove(box)
      autosave(delayMs = 0)
    }

    prependRecord(recordId, record)
  }

  private def prependRecord(id: AliasId, record: AliasRecord): Unit = {
    val copy = records.toList
    records.clear
    records.put(id, record)
    records.addAll(copy)
  }

  stage = new JFXApp.PrimaryStage { selfStage =>
    title = "Aliaser"
    resizable = false
    scene = new Scene(500, 600) {
      private val borderPane = new BorderPane {
        private val vbox = new VBox() {
          spacing = 10
          padding = Insets(10)
          gridPane = new GridPane {
            vgap = 10
            scrollPane = new ScrollPane {
              padding = Insets(5)
              content = recordsBox
              style = defaultScrollPaneStyle
              this.setFitToHeight(true)
              this.setFitToWidth(true)
            }
            selfStage.setOnShown(_ => scrollPane.lookup(".viewport").setStyle("-fx-background-color: #141414;"))

            val colConst = new ColumnConstraints
            colConst.percentWidth = 100
            columnConstraints.addAll(colConst)

            GridPane.setVgrow(scrollPane, Priority.Always)
            GridPane.setHalignment(scrollPane, HPos.Center)
            GridPane.setHalignment(addButton, HPos.Center)
            GridPane.setHalignment(status, HPos.Center)

            add(scrollPane, 0, 0)
            add(addButton, 0, 1)
            add(status, 0, 2)
            style = defaultGridPaneStyle
          }
          VBox.setVgrow(gridPane, Priority.Always)
          children = List(swapButton, gridPane)
          style = "-fx-background-color: #141414;"
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
