package com.jetbrains.ther.ui.graphics;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TheRGraphicsState {

  @NotNull
  private static final Logger LOGGER = Logger.getInstance(TheRGraphicsState.class);

  @NotNull
  private static final Pattern SNAPSHOT_NAME_PATTERN = Pattern.compile("^snapshot_(\\d+)\\.png$");

  @NotNull
  private static final String SNAPSHOT_COULD_NOT_BE_ENCODED = "Snapshot couldn't be encoded [name: %s]";

  @NotNull
  private static final String SNAPSHOT_HAS_BEEN_LOADED = "Snapshot has been loaded [name: %s]";

  @NotNull
  private static final String SNAPSHOT_HAS_BEEN_ADDED = "Snapshot has been added [id: %d, name: %s]";

  @NotNull
  private static final String SNAPSHOT_HAS_BEEN_REMOVED = "Snapshot has been removed [id: %d, name: %s]";

  @NotNull
  private static final String STATE_HAS_BEEN_RESET = "State has been reset";

  @NotNull
  private static final String NO_NEXT_SNAPSHOT_AFTER_CURRENT = "No next snapshot after current [current: %s]";

  @NotNull
  private static final String NO_PREVIOUS_SNAPSHOT_BEFORE_CURRENT = "No previous snapshot before current [current: %s]";

  @NotNull
  private static final String MOVED_FORWARD = "Moved forward [old: %d, new: %d]";

  @NotNull
  private static final String MOVED_BACKWARD = "Moved backward [old: %d, new: %d]";

  @NotNull
  private static final String SNAPSHOT_IS_NOT_FOUND = "Snapshot is not found [name: %s]";

  @NotNull
  private static final String ILLEGAL_SNAPSHOT_NAME = "Illegal snapshot name [name: %s]";

  @NotNull
  private static final String SNAPSHOT_NAME_FORMAT = "snapshot_%d.png";

  @NotNull
  private final TreeSet<Integer> mySnapshotIds;

  @NotNull
  private final VirtualFile mySnapshotDir;

  private int myCurrentId;

  public TheRGraphicsState(@NotNull final VirtualFile snapshotDir) {
    mySnapshotIds = new TreeSet<Integer>();
    mySnapshotDir = snapshotDir;

    myCurrentId = -1;
  }

  public boolean hasNext() {
    return mySnapshotIds.higher(myCurrentId) != null;
  }

  public boolean hasPrevious() {
    return mySnapshotIds.lower(myCurrentId) != null;
  }

  public void next() {
    advance(true);
  }

  public void previous() {
    advance(false);
  }

  @NotNull
  public BufferedImage current() throws IOException {
    final VirtualFile file = currentFile();
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

  public boolean isSnapshot(@NotNull final VirtualFile file) {
    return SNAPSHOT_NAME_PATTERN.matcher(file.getName()).matches() && VfsUtilCore.isAncestor(mySnapshotDir, file, false);
  }

  public boolean isCurrent(@NotNull final VirtualFile file) {
    return myCurrentId == calculateSnapshotId(file.getName());
  }

  public void add(@NotNull final VirtualFile file) {
    final String name = file.getName();
    final int id = calculateSnapshotId(name);

    if (mySnapshotIds.add(id)) {
      LOGGER.info(
        String.format(SNAPSHOT_HAS_BEEN_ADDED, id, name)
      );
    }
  }

  public void remove(@NotNull final VirtualFile file) {
    final String name = file.getName();
    final int id = calculateSnapshotId(name);

    if (mySnapshotIds.remove(id)) {
      LOGGER.info(
        String.format(SNAPSHOT_HAS_BEEN_REMOVED, id, name)
      );
    }
  }

  public void reset() {
    myCurrentId = -1;
    mySnapshotIds.clear();

    LOGGER.debug(STATE_HAS_BEEN_RESET);
  }

  private void advance(final boolean forward) {
    final Integer newCurrentId = forward ? mySnapshotIds.higher(myCurrentId) : mySnapshotIds.lower(myCurrentId);

    if (newCurrentId == null) {
      throw new NoSuchElementException(
        String.format(
          forward ? NO_NEXT_SNAPSHOT_AFTER_CURRENT : NO_PREVIOUS_SNAPSHOT_BEFORE_CURRENT,
          calculateSnapshotName(myCurrentId)
        )
      );
    }

    LOGGER.debug(
      String.format(
        forward ? MOVED_FORWARD : MOVED_BACKWARD,
        myCurrentId,
        newCurrentId
      )
    );

    myCurrentId = newCurrentId;
  }

  @NotNull
  private VirtualFile currentFile() throws FileNotFoundException {
    final String name = calculateSnapshotName(myCurrentId);
    final VirtualFile result = mySnapshotDir.findChild(name);

    if (result == null) {
      throw new FileNotFoundException(
        String.format(SNAPSHOT_IS_NOT_FOUND, name)
      );
    }

    return result;
  }

  private int calculateSnapshotId(@NotNull final String snapshotName) {
    final Matcher matcher = SNAPSHOT_NAME_PATTERN.matcher(snapshotName);

    if (matcher.find()) {
      return Integer.parseInt(matcher.group(1));
    }
    else {
      throw new IllegalArgumentException(
        String.format(ILLEGAL_SNAPSHOT_NAME, snapshotName)
      );
    }
  }

  @NotNull
  private String calculateSnapshotName(final int snapshotId) {
    return String.format(SNAPSHOT_NAME_FORMAT, snapshotId);
  }
}
