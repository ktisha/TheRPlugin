package com.jetbrains.ther.ui.graphics;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.vfs.impl.BulkVirtualFileListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import static com.jetbrains.ther.ui.graphics.TheRGraphicsUtils.*;

class TheRGraphicsState {

  @NotNull
  private static final Logger LOGGER = Logger.getInstance(TheRGraphicsState.class);

  @NotNull
  private static final String STARTED_TO_LISTEN_FOR_NEW_SNAPSHOTS = "Started to listen for new snapshots";

  @NotNull
  private static final String NO_NEXT_SNAPSHOT = "No next snapshot";

  @NotNull
  private static final String NO_PREVIOUS_SNAPSHOT = "No previous snapshot";

  @NotNull
  private static final String SNAPSHOT_IS_NOT_FOUND = "Snapshot is not found";

  @NotNull
  private static final String SNAPSHOT_COULD_NOT_BE_ENCODED = "Snapshot couldn't be encoded";

  @NotNull
  private static final String REMOVED_SNAPSHOT_AS_RENAMED = "Removed snapshot (as renamed)";

  @NotNull
  private static final String UPDATED_CURRENT_SNAPSHOT = "Updated current snapshot";

  @NotNull
  private static final String UPDATED_SNAPSHOT = "Updated snapshot";

  @NotNull
  private static final String CREATED_SNAPSHOT = "Created snapshot";

  @NotNull
  private static final String REMOVED_SNAPSHOT = "Removed snapshot";

  @NotNull
  private static final String REMOVED_SNAPSHOT_AS_MOVED = "Removed snapshot (as moved)";

  @NotNull
  private final TreeSet<Integer> mySnapshotIds;

  @Nullable
  private final VirtualFile mySnapshotDir;

  @NotNull
  private final List<Listener> myListeners;

  private int myCurrentId;

  public TheRGraphicsState(@NotNull final Project project) {
    mySnapshotIds = new TreeSet<Integer>();
    mySnapshotDir = getOrCreateSnapshotDir(project);
    myListeners = new ArrayList<Listener>();

    myCurrentId = -1;

    if (mySnapshotDir != null) {
      project.getMessageBus().connect(project).subscribe(
        VirtualFileManager.VFS_CHANGES,
        new BulkVirtualFileListenerAdapter(new SnapshotDirListener())
      );

      LOGGER.info(STARTED_TO_LISTEN_FOR_NEW_SNAPSHOTS + ": " + mySnapshotDir.getPath());
    }
  }

  public void addListener(@NotNull final Listener listener) {
    myListeners.add(listener);
  }

  public void removeListener(@NotNull final Listener listener) {
    myListeners.remove(listener);
  }

  public boolean hasNext() {
    return mySnapshotIds.higher(myCurrentId) != null;
  }

  public boolean hasPrevious() {
    return mySnapshotIds.lower(myCurrentId) != null;
  }

  @NotNull
  public BufferedImage next() throws IOException {
    updateCurrentId(true);

    return current();
  }

  @NotNull
  public BufferedImage previous() throws IOException {
    updateCurrentId(false);

    return current();
  }

  @NotNull
  public BufferedImage current() throws IOException {
    final InputStream stream = currentFile().getInputStream();

    try {
      return loadImage(stream);
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

  private void updateCurrentId(final boolean next) {
    final Integer newCurrentId = next ? mySnapshotIds.higher(myCurrentId) : mySnapshotIds.lower(myCurrentId);

    if (newCurrentId == null) {
      throw new NoSuchElementException(next ? NO_NEXT_SNAPSHOT : NO_PREVIOUS_SNAPSHOT);
    }

    myCurrentId = newCurrentId;
  }

  @NotNull
  private VirtualFile currentFile() throws FileNotFoundException {
    assert mySnapshotDir != null;

    final String snapshotName = calculateSnapshotName(myCurrentId);
    final VirtualFile currentSnapshot = mySnapshotDir.findChild(snapshotName);

    if (currentSnapshot == null) {
      throw new FileNotFoundException(SNAPSHOT_IS_NOT_FOUND + ": " + snapshotName);
    }

    return currentSnapshot;
  }

  @NotNull
  private BufferedImage loadImage(@NotNull final InputStream stream) throws IOException {
    final BufferedImage image = ImageIO.read(stream);

    if (image == null) {
      throw new IllegalStateException(SNAPSHOT_COULD_NOT_BE_ENCODED);
    }

    return image; // TODO [ui][resize]
  }

  interface Listener {

    void currentUpdate();
  }

  private class SnapshotDirListener extends VirtualFileAdapter {

    @Override
    public void contentsChanged(@NotNull final VirtualFileEvent event) {
      if (isSnapshotEvent(event)) {
        final int snapshotId = calculateSnapshotId(event.getFileName());

        if (myCurrentId == snapshotId) {
          LOGGER.debug(UPDATED_CURRENT_SNAPSHOT + ": " + snapshotId);

          for (final Listener listener : myListeners) {
            listener.currentUpdate();
          }
        }
        else {
          LOGGER.debug(UPDATED_SNAPSHOT + ": " + snapshotId);
        }
      }
    }

    @Override
    public void fileCreated(@NotNull final VirtualFileEvent event) {
      if (isSnapshotEvent(event)) {
        final int snapshotId = calculateSnapshotId(event.getFileName());

        if (mySnapshotIds.add(snapshotId)) {
          LOGGER.debug(CREATED_SNAPSHOT + ": " + snapshotId);
        }
      }
    }

    @Override
    public void fileDeleted(@NotNull final VirtualFileEvent event) {
      if (isSnapshotEvent(event)) {
        final int snapshotId = calculateSnapshotId(event.getFileName());

        if (mySnapshotIds.remove(snapshotId)) {
          LOGGER.info(REMOVED_SNAPSHOT + ": " + snapshotId);
        }
      }
    }

    @Override
    public void fileCopied(@NotNull final VirtualFileCopyEvent event) {
      // ignore
    }

    @Override
    public void beforePropertyChange(@NotNull final VirtualFilePropertyEvent event) {
      if (event.getPropertyName().equals(VirtualFile.PROP_NAME) && isSnapshotEvent(event)) {
        final int snapshotId = calculateSnapshotId((String)event.getOldValue());

        if (mySnapshotIds.remove(snapshotId)) {
          LOGGER.info(REMOVED_SNAPSHOT_AS_RENAMED + ": " + snapshotId);
        }
      }
    }

    @Override
    public void beforeFileMovement(@NotNull final VirtualFileMoveEvent event) {
      if (isSnapshotEvent(event)) {
        final int snapshotId = calculateSnapshotId(event.getFileName());

        if (mySnapshotIds.remove(snapshotId)) {
          LOGGER.info(REMOVED_SNAPSHOT_AS_MOVED + ": " + snapshotId);
        }
      }
    }

    private boolean isSnapshotEvent(@NotNull final VirtualFileEvent event) {
      assert mySnapshotDir != null;

      final VirtualFile file = event.getFile();
      final String fileName = file.getName();

      return isSnapshotName(fileName) && VfsUtilCore.isAncestor(mySnapshotDir, file, false);
    }
  }
}
