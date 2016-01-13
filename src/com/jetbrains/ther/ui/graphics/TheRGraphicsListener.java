package com.jetbrains.ther.ui.graphics;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.*;
import org.jetbrains.annotations.NotNull;

public class TheRGraphicsListener extends VirtualFileAdapter {

  @NotNull
  private static final Logger LOGGER = Logger.getInstance(TheRGraphicsListener.class);

  @NotNull
  private static final String RENAMED_SNAPSHOT_WILL_BE_REMOVED = "Renamed snapshot will be removed [name: %s]";

  @NotNull
  private static final String MOVED_SNAPSHOT_WILL_BE_REMOVED = "Moved snapshot will be removed [name: %s]";

  @NotNull
  private final TheRGraphicsState myState;

  public TheRGraphicsListener(@NotNull final TheRGraphicsState state) {
    myState = state;
  }

  @Override
  public void fileCreated(@NotNull final VirtualFileEvent event) {
    final VirtualFile file = event.getFile();

    if (myState.isSnapshot(file)) {
      myState.add(file);
    }
  }

  @Override
  public void fileDeleted(@NotNull final VirtualFileEvent event) {
    final VirtualFile file = event.getFile();

    if (myState.isSnapshot(file)) {
      myState.remove(file);
    }
  }

  @Override
  public void fileCopied(@NotNull final VirtualFileCopyEvent event) {
    // ignore
  }

  @Override
  public void beforePropertyChange(@NotNull final VirtualFilePropertyEvent event) {
    final VirtualFile file = event.getFile();

    if (event.getPropertyName().equals(VirtualFile.PROP_NAME) && myState.isSnapshot(file)) {
      LOGGER.warn(
        String.format(RENAMED_SNAPSHOT_WILL_BE_REMOVED, event.getFileName())
      );

      myState.remove(file);
    }
  }

  @Override
  public void beforeFileMovement(@NotNull final VirtualFileMoveEvent event) {
    final VirtualFile file = event.getFile();

    if (myState.isSnapshot(file)) {
      LOGGER.warn(
        String.format(MOVED_SNAPSHOT_WILL_BE_REMOVED, event.getFileName())
      );

      myState.remove(file);
    }
  }
}
