package com.zerdicorp.aliaser

import scalafx.animation.FadeTransition
import scalafx.util.Duration

final case class SavedLblAnime(
    protected val asyncDude: AsyncDude,
    protected val disappear: FadeTransition,
    protected val appear: FadeTransition,
    protected val onAppearance: U2U,
    protected val onDisappearance: U2U,
    private var inProcess: Boolean = false,
) {
  def kawaii(): Unit = {
    synchronized {
      if (!inProcess) {
        inProcess = true
        appear.play()
        onAppearance()
        asyncDude.doAfter(1500) { _ =>
          disappear.play()
          onDisappearance()
          inProcess = false
        }
      }
    }
  }
}

object SavedLblAnime {
  def apply(savedLbl: my.SavedLabel, asyncDude: AsyncDude)(
      onAppearance: U2U,
      onDisappearance: U2U,
  ): SavedLblAnime = {
    val disappear = new FadeTransition(Duration(500), savedLbl)
    disappear.fromValue = 1.0
    disappear.toValue = 0.0
    val appear = new FadeTransition(Duration(250), savedLbl)
    appear.fromValue = 0.0
    appear.toValue = 1.0
    SavedLblAnime(asyncDude, disappear, appear, onAppearance, onDisappearance)
  }
}
