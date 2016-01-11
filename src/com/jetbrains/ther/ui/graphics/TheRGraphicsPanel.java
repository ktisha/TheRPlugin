package com.jetbrains.ther.ui.graphics;

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
  private static final String CURRENT_GRAPHICS_COULD_NOT_BE_RELOADED = "Current graphics couldn't be reloaded";

  @NotNull
  private static final String NEXT_GRAPHICS_COULD_NOT_BE_LOADED = "Next graphics couldn't be loaded";

  @NotNull
  private static final String PREVIOUS_GRAPHICS_COULD_NOT_BE_LOADED = "Previous graphics couldn't be loaded";

  @NotNull
  private final TheRGraphicsState myState;

  @NotNull
  private final JPanel myPanel;

  @NotNull
  private final JLabel myLabel;

  public TheRGraphicsPanel(@NotNull final TheRGraphicsState state) {
    myState = state;

    myPanel = new JPanel();
    myLabel = new JLabel();
    initLabel();

    myPanel.add(myLabel);
  }

  public void showNext() {
    handleGraphicsAction(GraphicsAction.NEXT);
  }

  public void showPrevious() {
    handleGraphicsAction(GraphicsAction.PREVIOUS);
  }

  public void updateCurrent() {
    handleGraphicsAction(GraphicsAction.CURRENT);
  }

  @NotNull
  public JPanel getPanel() {
    return myPanel;
  }

  private void initLabel() {
    if (myState.hasNext()) {
      handleGraphicsAction(GraphicsAction.NEXT);
    }
    else {
      myLabel.setText(NO_GRAPHICS);
    }
  }

  private void handleGraphicsAction(@NotNull final GraphicsAction action) {
    myLabel.setText("");

    try {
      myLabel.setIcon(new ImageIcon(loadLabelImage(action)));
    }
    catch (final IOException e) {
      myLabel.setText(calculateLabelText(action));

      LOGGER.error(e);
    }
  }

  @NotNull
  private BufferedImage loadLabelImage(@NotNull final GraphicsAction action) throws IOException {
    switch (action) {
      case NEXT:
        return myState.next();
      case CURRENT:
        return myState.current();
      case PREVIOUS:
        return myState.previous();
      default:
        throw new IllegalArgumentException("Unexpected graphics action: " + action);
    }
  }

  @NotNull
  private String calculateLabelText(@NotNull final GraphicsAction action) {
    switch (action) {
      case NEXT:
        return NEXT_GRAPHICS_COULD_NOT_BE_LOADED;
      case CURRENT:
        return CURRENT_GRAPHICS_COULD_NOT_BE_RELOADED;
      case PREVIOUS:
        return PREVIOUS_GRAPHICS_COULD_NOT_BE_LOADED;
      default:
        throw new IllegalArgumentException("Unexpected graphics action: " + action);
    }
  }

  private enum GraphicsAction {
    NEXT, CURRENT, PREVIOUS
  }
}
