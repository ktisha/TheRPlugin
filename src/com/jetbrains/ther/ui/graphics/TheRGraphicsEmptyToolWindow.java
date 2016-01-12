package com.jetbrains.ther.ui.graphics;

import com.intellij.openapi.ui.SimpleToolWindowPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

class TheRGraphicsEmptyToolWindow extends SimpleToolWindowPanel {

  public TheRGraphicsEmptyToolWindow(@NotNull final String message) {
    super(true, true);

    final JPanel panel = new JPanel();
    panel.add(new JLabel(message));

    setContent(panel);
  }
}
