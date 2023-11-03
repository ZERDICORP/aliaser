package com.zerdicorp.aliaser

import javax.swing._

object Notification {
  private def show(title: String, message: String, _type: Int): Unit =
    SwingUtilities.invokeLater(() => {
      JOptionPane.showMessageDialog(null, message, title, _type)
    })

  def initializationError(message: String): Unit =
    show("Initialization Error", message, JOptionPane.ERROR_MESSAGE)
}
