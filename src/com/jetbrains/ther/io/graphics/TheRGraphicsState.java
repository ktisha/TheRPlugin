package com.jetbrains.ther.io.graphics;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.vfs.impl.BulkVirtualFileListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import static com.jetbrains.ther.io.graphics.TheRGraphicsUtils.*;

public class TheRGraphicsState implements Disposable {

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
  private final TreeSet<Integer> mySnapshotIds;

  @Nullable
  private final VirtualFile mySnapshotDir;

  private int myCurrentId;

  public TheRGraphicsState(@NotNull final Project project) {
    mySnapshotIds = new TreeSet<Integer>();
    mySnapshotDir = getSnapshotDir(project);

    myCurrentId = -1;

    if (mySnapshotDir != null) {
      Disposer.register(project, this);

      project.getMessageBus().connect(this).subscribe(
        VirtualFileManager.VFS_CHANGES,
        new BulkVirtualFileListenerAdapter(new SnapshotDirListener())
      );

      LOGGER.info(STARTED_TO_LISTEN_FOR_NEW_SNAPSHOTS + ": " + mySnapshotDir.getPath());
    }
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

  @Override
  public void dispose() {
  }

  private void updateCurrentId(final boolean next) {
    final Integer newCurrentId = next ? mySnapshotIds.higher(myCurrentId) : mySnapshotIds.lower(myCurrentId);

    if (newCurrentId == null) {
      throw new NoSuchElementException(next ? NO_NEXT_SNAPSHOT : NO_PREVIOUS_SNAPSHOT);
    }

    myCurrentId = newCurrentId;
  }

  @NotNull
  private BufferedImage current() throws IOException {
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
      throw new IllegalStateException(); // TODO [ui][msg]
    }

    return image; // TODO [ui][resize]
  }

  private class SnapshotDirListener extends VirtualFileAdapter {

    @Override
    public void fileCreated(@NotNull final VirtualFileEvent event) {
      assert mySnapshotDir != null;

      final VirtualFile file = event.getFile();
      final String fileName = file.getName();

      if (isSnapshotName(fileName) && VfsUtilCore.isAncestor(mySnapshotDir, file, false)) {
        mySnapshotIds.add(extractSnapshotId(fileName));
      }
    }
  }
}
