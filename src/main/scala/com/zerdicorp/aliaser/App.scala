package com.zerdicorp.aliaser

import cats.implicits.catsSyntaxOptionId
import com.zerdicorp.aliaser.FileUtils.{appendSourceToRcFile, initDotFile, readDotFile}
import com.zerdicorp.aliaser.ValueFormat.StringOps
import javafx.scene.input.KeyCode
import scalafx.application.{JFXApp, Platform}
import scalafx.geometry.{HPos, Insets}
import scalafx.scene.Scene
import scalafx.scene.input.MouseButton
import scalafx.scene.layout._
import scalafx.stage.Stage

object App extends JFXApp { mainApp =>
  import Implicits._
  import Storage._

  private val recordSelector = RecordSelector()

  private val recordsVBox = new VBox { spacing = 5 }

  private val savedLbl = my.SavedLabel()
  private val swapButton = my.SwapButton(_ => swapRecords())
  private val addButton = my.AddButton(_ => addRecord())
  private val recordScroll = my.RecordScroll(recordsVBox)
  private val mainGrid = my.MainGrid(recordScroll, addButton)

  private val asyncDude = AsyncDude()
  private val autosaveJob = AutosaveJob(asyncDude)

  private val savedLblAnime = SavedLblAnime(savedLbl, asyncDude)(
    onAppearance = { _ =>
      Platform.runLater(() => {
        mainGrid.greenlight()
        recordScroll.greenlight()
      })
    },
    onDisappearance = { _ =>
      Platform.runLater(() => {
        mainGrid.lightOff()
        recordScroll.lightOff()
      })
    },
  )

  def swapRecords(): Unit = {
    val temp = records(recordSelector.first)
    records(recordSelector.first) = records(recordSelector.second)
    records(recordSelector.second) = temp
    autosave(
      delayMs = 0,
      _ => {
        records.clear()
        selects.clear()
        recordsVBox.children.clear()
        Thread.sleep(250)
        reload()
        swapButton.disable()
        recordSelector.reset()
      },
    )
  }

  def autosave(delayMs: Int, f: U2U = identity): Unit =
    autosaveJob.exec(delayMs) { _ =>
      Platform.runLater(() => {
        savedLblAnime.kawaii()
        f()
      })
    }

  def addRecord(recordOpt: Option[AliasRecord] = None): Unit = {
    val record = recordOpt.getOrElse(AliasRecord.empty)
    val recordId = records.size
    val box = new HBox { spacing = 10 }
    val recordBox = new HBox()

    val selectBtn = my.SelectButton(recordId, recordSelector, swapButton, selects)
    selects.addOne(selectBtn)

    val keyField = my.KeyField(record.key)
    val valueField = my.ValueField(record.value.pseudo)
    KeyValueChangeFlow(keyField, valueField, recordId, _ => autosave(500))

    valueField.onMouseClicked = { e =>
      if (e.getButton == MouseButton.Secondary.toString() || e.isMetaDown) {
        val stage = new Stage { outer =>
          title = keyField.text
          scene = new Scene(500, 600) { self =>
            stylesheets.add("main.css")
            self.setOnKeyPressed(e =>
              if (e.getCode eq KeyCode.ESCAPE) {
                savedLblAnime.reset()
                outer.close()
                mainApp.stage.show()
              },
            )
            root = new BorderPane {
              this.getStyleClass.add("modal-root")
              padding = Insets(25)
              private val scriptArea = my.ScriptArea(records(recordId).value)
              center = scriptArea
              savedLblAnime.mod(
                newOnAppearance = _ => {
                  Platform.runLater(() => {
                    scriptArea.greenlight()
                  })
                },
                newOnDisappearance = _ => {
                  Platform.runLater(() => {
                    scriptArea.lightOff()
                  })
                },
              )
              KeyValueChangeFlow.modal(
                scriptArea,
                recordId,
                _ => {
                  valueField.node.text = scriptArea.text().pseudo
                  autosave(500)
                },
              )
            }
          }
        }
        mainApp.stage.hide()
        stage.show()
      }
    }

    val removeBtn = my.RemoveButton { _ =>
      records.remove(recordId)
      recordsVBox.children.remove(box)
      autosave(delayMs = 0)
    }

    HBox.setHgrow(valueField, Priority.Always)
    HBox.setHgrow(recordBox, Priority.Always)

    recordBox.putChilds(keyField, valueField)
    box.putChilds(selectBtn, recordBox, removeBtn)
    recordsVBox.prependChild(box)

    prependRecord(recordId, record)
  }

  stage = new JFXApp.PrimaryStage {
    title = "Aliaser"
    resizable = false
    scene = new Scene(500, 600) { self =>
      self.setOnKeyPressed(e =>
        if (e.getCode eq KeyCode.ESCAPE) {
          mainApp.stage.close()
        },
      )
      stylesheets.add("main.css")
      private val borderPane = new BorderPane {
        private val vbox = new VBox() {
          spacing = 10
          padding = Insets(10)

          VBox.setVgrow(mainGrid, Priority.Always)

          private val panel = new GridPane {
            vgap = 10

            private val col1 = new ColumnConstraints()
            col1.setHgrow(Priority.Always)
            private val col2 = new ColumnConstraints()
            col2.setHgrow(Priority.Always)

            this.getColumnConstraints.addAll(col1, col2)

            GridPane.setHalignment(savedLbl, HPos.Right)

            add(swapButton, 0, 0)
            add(savedLbl, 1, 0)
          }

          this.putChilds(panel, mainGrid)
          this.getStyleClass.add("global-box")
        }
        center = vbox
      }
      root = borderPane
    }
  }

  override def stopApp(): Unit = {
    asyncDude.stfu()
    super.stopApp()
  }

  def reload(): Unit =
    AliasRecord.fromLines(readDotFile()).foreach(a => addRecord(a.some))

  def initialize(): Unit = {
    appendSourceToRcFile()
    initDotFile()
    reload()
  }

  initialize()
}
