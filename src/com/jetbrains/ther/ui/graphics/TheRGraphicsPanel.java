package com.jetbrains.ther.ui.graphics;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

class TheRGraphicsPanel implements TheRGraphicsState.Listener {

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
  private static final String SNAPSHOT_COULD_NOT_BE_ENCODED = "Snapshot couldn't be encoded [name: %s]";

  @NotNull
  private static final String SNAPSHOT_HAS_BEEN_LOADED = "Snapshot has been loaded [name: %s]";

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

    myState.addListener(this);
  }

  @NotNull
  public JPanel getPanel() {
    return myPanel;
  }

  @Override
  public void onReset() {
    myLabel.setIcon(null);
    myLabel.setText(NO_GRAPHICS);

    LOGGER.debug(PANEL_HAS_BEEN_RESET);
  }

  @Override
  public void onUpdate() {
    try {
      myLabel.setText(null);
      myLabel.setIcon(new ImageIcon(loadCurrentGraphics()));

      LOGGER.debug(PANEL_HAS_BEEN_REFRESHED);
    }
    catch (final IOException e) {
      myLabel.setIcon(null);
      myLabel.setText(GRAPHICS_COULD_NOT_BE_LOADED);

      LOGGER.error(e);
    }
  }

  @NotNull
  private BufferedImage loadCurrentGraphics() throws IOException {
    final VirtualFile file = myState.current();
    final InputStream stream = file.getInputStream();

    try {
      final BufferedImage image = ImageIO.read(stream);

      if (image == null) {
        throw new IllegalStateException(
          String.format(SNAPSHOT_COULD_NOT_BE_ENCODED, file.getName())
        );
      }

      LOGGER.debug(
        String.format(SNAPSHOT_HAS_BEEN_LOADED, file.getName())
      );

      return image; // TODO [ui][resize]
    }
    finally {
      try {
        stream.close();
      }
      catch (final IOException e) {
        LOGGER.warn(e);
      }
    }
  }
}
