package com.jetbrains.ther.io.graphics;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

class TheRGraphicsPanel {

  @NotNull
  private static final Logger LOGGER = Logger.getInstance(TheRGraphicsPanel.class);

  @NotNull
  private static final String NO_GRAPHICS = "No graphics";

  @NotNull
  private static final String NEXT_GRAPHICS_COULD_NOT_BE_LOADED = "Next graphics couldn't be loaded";

  @NotNull
  private static final String PREVIOUS_GRAPHICS_COULDNOT_BE_LOADED = "Previous graphics couldn't be loaded";

  @NotNull
  private final TheRGraphicsToolWindowState myState;

  @NotNull
  private final JPanel myPanel;

  @NotNull
  private final JLabel myLabel;

  public TheRGraphicsPanel(@NotNull final TheRGraphicsToolWindowState state) {
    myState = state;

    myPanel = new JPanel();
    myLabel = new JLabel();
    initLabel();

    myPanel.add(myLabel);
  }

  public void showNext() {
    show(true);
  }

  public void showPrevious() {
    show(false);
  }

  @NotNull
  public JPanel getPanel() {
    return myPanel;
  }

  private void initLabel() {
    if (myState.hasNext()) {
      show(true);
    }
    else {
      myLabel.setText(NO_GRAPHICS);
    }
  }

  private void show(final boolean next) {
    myLabel.setText("");

    try {
      final BufferedImage image = next ? myState.next() : myState.previous();

      myLabel.setIcon(new ImageIcon(image));
    }
    catch (final IOException e) {
      final String text = next ? NEXT_GRAPHICS_COULD_NOT_BE_LOADED : PREVIOUS_GRAPHICS_COULDNOT_BE_LOADED;

      myLabel.setText(text);

      LOGGER.error(e);
    }
  }
}
