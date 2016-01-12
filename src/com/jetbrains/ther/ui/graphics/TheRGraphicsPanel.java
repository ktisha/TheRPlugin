package com.jetbrains.ther.ui.graphics;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;

class TheRGraphicsPanel {

  @NotNull
  private static final Logger LOGGER = Logger.getInstance(TheRGraphicsPanel.class);

  @NotNull
  private static final String NO_GRAPHICS = "No graphics";

  @NotNull
  private static final String PANEL_HAS_BEEN_REFRESHED = "Panel has been refreshed";

  @NotNull
  private static final String GRAPHICS_COULD_NOT_BE_LOADED = "Graphics couldn't be loaded";

  @NotNull
  private static final String PANEL_HAS_BEEN_RESET = "Panel has been reset";

  @NotNull
  private final TheRGraphicsState myState;

  @NotNull
  private final JLabel myLabel;

  @NotNull
  private final JPanel myPanel;

  public TheRGraphicsPanel(@NotNull final TheRGraphicsState state) {
    myState = state;

    myLabel = new JLabel(NO_GRAPHICS);

    myPanel = new JPanel();
    myPanel.add(myLabel);
  }

  public void refresh() {
    myLabel.setText(null);

    try {
      myLabel.setIcon(new ImageIcon(myState.current()));

      LOGGER.debug(PANEL_HAS_BEEN_REFRESHED);
    }
    catch (final IOException e) {
      myLabel.setIcon(null);
      myLabel.setText(GRAPHICS_COULD_NOT_BE_LOADED);

      LOGGER.error(e);
    }
  }

  public void reset() {
    myLabel.setIcon(null);
    myLabel.setText(NO_GRAPHICS);

    LOGGER.debug(PANEL_HAS_BEEN_RESET);
  }

  @NotNull
  public JPanel getPanel() {
    return myPanel;
  }
}
