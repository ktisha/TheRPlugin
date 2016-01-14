package com.jetbrains.ther.ui.graphics;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.*;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TheRGraphicsStateImpl implements TheRGraphicsState, Disposable {

  @NotNull
  private static final Logger LOGGER = Logger.getInstance(TheRGraphicsStateImpl.class);

  @NotNull
  private static final String STARTED_TO_LISTEN_FOR_NEW_SNAPSHOTS = "Started to listen for new snapshots [dir: %s]";

  @NotNull
  private static final String SNAPSHOT_IS_NOT_FOUND = "Snapshot is not found [name: %s]";

  @NotNull
  private static final String STATE_HAS_BEEN_RESET = "State has been reset";

  @NotNull
  private static final String STATE_HAS_BEEN_DISPOSED = "State has been disposed";

  @NotNull
  private static final String NO_NEXT_SNAPSHOT_AFTER_CURRENT = "No next snapshot after current [current: %s]";

  @NotNull
  private static final String NO_PREVIOUS_SNAPSHOT_BEFORE_CURRENT = "No previous snapshot before current [current: %s]";

  @NotNull
  private static final String MOVED_FORWARD = "Moved forward [old: %d, new: %d]";

  @NotNull
  private static final String MOVED_BACKWARD = "Moved backward [old: %d, new: %d]";

  @NotNull
  private static final String SNAPSHOT_NAME_FORMAT = "snapshot_%d.png";

  @NotNull
  private static final Pattern SNAPSHOT_NAME_PATTERN = Pattern.compile("^snapshot_(\\d+)\\.png$");

  @NotNull
  private static final String SNAPSHOT_HAS_BEEN_ADDED = "Snapshot has been added [id: %d, name: %s]";

  @NotNull
  private static final String SNAPSHOT_HAS_BEEN_REMOVED = "Snapshot has been removed [id: %d, name: %s]";

  @NotNull
  private static final String ILLEGAL_SNAPSHOT_NAME = "Illegal snapshot name [name: %s]";

  @NotNull
  private static final String UPDATED_SNAPSHOT = "Updated snapshot [name: %s]";

  @NotNull
  private static final String RENAMED_SNAPSHOT_WILL_BE_REMOVED = "Renamed snapshot will be removed [name: %s]";

  @NotNull
  private static final String MOVED_SNAPSHOT_WILL_BE_REMOVED = "Moved snapshot will be removed [name: %s]";

  @NotNull
  private final TreeSet<Integer> mySnapshotIds;

  @NotNull
  private final VirtualFile mySnapshotDir;

  @NotNull
  private final List<Listener> myListeners;

  private int myCurrentId;

  public TheRGraphicsStateImpl(@NotNull final VirtualFile snapshotDir) {
    mySnapshotIds = new TreeSet<Integer>();
    mySnapshotDir = snapshotDir;
    myListeners = new LinkedList<Listener>();

    myCurrentId = -1;

    VirtualFileManager.getInstance().addVirtualFileListener(
      new TheRGraphicsListener(),
      this
    );

    LOGGER.info(
      String.format(STARTED_TO_LISTEN_FOR_NEW_SNAPSHOTS, mySnapshotDir.getPath())
    );
  }

  @Override
  public boolean hasNext() {
    return mySnapshotIds.higher(myCurrentId) != null;
  }

  @Override
  public boolean hasPrevious() {
    return mySnapshotIds.lower(myCurrentId) != null;
  }

  @Override
  public void next() {
    advance(true);
  }

  @Override
  public void previous() {
    advance(false);
  }

  @Override
  @NotNull
  public VirtualFile current() throws FileNotFoundException {
    final String name = calculateSnapshotName(myCurrentId);
    final VirtualFile result = mySnapshotDir.findChild(name);

    if (result == null) {
      throw new FileNotFoundException(
        String.format(SNAPSHOT_IS_NOT_FOUND, name)
      );
    }

    return result;
  }

  @Override
  public void sync() {
    mySnapshotDir.refresh(true, true);
  }

  @Override
  public void reset() {
    myCurrentId = -1;
    mySnapshotIds.clear();

    LOGGER.debug(STATE_HAS_BEEN_RESET);

    for (final Listener listener : myListeners) {
      listener.onReset();
    }
  }

  @Override
  public void addListener(@NotNull final Listener listener) {
    myListeners.add(listener);
  }

  @Override
  public void removeListener(@NotNull final Listener listener) {
    myListeners.remove(listener);
  }

  @Override
  public void dispose() {
    LOGGER.info(STATE_HAS_BEEN_DISPOSED);
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

    for (final Listener listener : myListeners) {
      listener.onUpdate();
    }
  }

  @NotNull
  private String calculateSnapshotName(final int snapshotId) {
    return String.format(SNAPSHOT_NAME_FORMAT, snapshotId);
  }

  private boolean isSnapshot(@NotNull final VirtualFile file) {
    return SNAPSHOT_NAME_PATTERN.matcher(file.getName()).matches() && VfsUtilCore.isAncestor(mySnapshotDir, file, false);
  }

  private void add(@NotNull final VirtualFile file) {
    final String name = file.getName();
    final int id = calculateSnapshotId(name);

    if (mySnapshotIds.add(id)) {
      LOGGER.info(
        String.format(SNAPSHOT_HAS_BEEN_ADDED, id, name)
      );
    }
  }

  private void remove(@NotNull final VirtualFile file) {
    final String name = file.getName();
    final int id = calculateSnapshotId(name);

    if (mySnapshotIds.remove(id)) {
      LOGGER.info(
        String.format(SNAPSHOT_HAS_BEEN_REMOVED, id, name)
      );
    }
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

  private class TheRGraphicsListener extends VirtualFileAdapter {

    @Override
    public void contentsChanged(@NotNull final VirtualFileEvent event) {
      final VirtualFile file = event.getFile();

      if (isSnapshot(file)) {
        final String name = event.getFileName();

        LOGGER.debug(
          String.format(UPDATED_SNAPSHOT, name)
        );

        add(file);

        if (myCurrentId == calculateSnapshotId(name)) {
          for (final Listener listener : myListeners) {
            listener.onUpdate();
          }
        }
      }
    }

    @Override
    public void fileCreated(@NotNull final VirtualFileEvent event) {
      final VirtualFile file = event.getFile();

      if (isSnapshot(file)) {
        add(file);
      }
    }

    @Override
    public void fileDeleted(@NotNull final VirtualFileEvent event) {
      final VirtualFile file = event.getFile();

      if (isSnapshot(file)) {
        remove(file);
      }
    }

    @Override
    public void fileCopied(@NotNull final VirtualFileCopyEvent event) {
      // ignore
    }

    @Override
    public void beforePropertyChange(@NotNull final VirtualFilePropertyEvent event) {
      final VirtualFile file = event.getFile();

      if (event.getPropertyName().equals(VirtualFile.PROP_NAME) && isSnapshot(file)) {
        LOGGER.warn(
          String.format(RENAMED_SNAPSHOT_WILL_BE_REMOVED, event.getFileName())
        );

        remove(file);
      }
    }

    @Override
    public void beforeFileMovement(@NotNull final VirtualFileMoveEvent event) {
      final VirtualFile file = event.getFile();

      if (isSnapshot(file)) {
        LOGGER.warn(
          String.format(MOVED_SNAPSHOT_WILL_BE_REMOVED, event.getFileName())
        );

        remove(file);
      }
    }
  }
}
