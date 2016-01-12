package com.jetbrains.ther.ui.graphics;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.vfs.impl.BulkVirtualFileListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class TheRGraphicsToolWindow extends SimpleToolWindowPanel {

  @NotNull
  private static final Logger LOGGER = Logger.getInstance(TheRGraphicsToolWindow.class);

  @NotNull
  private static final String STARTED_TO_LISTEN_FOR_NEW_SNAPSHOTS = "Started to listen for new snapshots [dir: %s]";

  @NotNull
  private static final String TOOL_WINDOW_HAS_BEEN_RESET = "Tool Window has been reset";

  @NotNull
  private static final String UPDATED_SNAPSHOT = "Updated snapshot [name: %s]";

  @NotNull
  private static final String RENAMED_SNAPSHOT_WILL_BE_REMOVED = "Renamed snapshot will be removed from tool window [name: %s]";

  @NotNull
  private static final String MOVED_SNAPSHOT_WILL_BE_REMOVED = "Moved snapshot will be removed from tool window [name: %s]";

  @NotNull
  private final VirtualFile mySnapshotDir;

  @NotNull
  private final TheRGraphicsState myState;

  @NotNull
  private final TheRGraphicsPanel myPanel;

  public TheRGraphicsToolWindow(@NotNull final Project project,
                                @NotNull final VirtualFile snapshotDir,
                                @NotNull final TheRGraphicsState state) {
    super(true, true);

    mySnapshotDir = snapshotDir;
    myState = state;

    myPanel = new TheRGraphicsPanel(myState);

    setToolbar(new TheRGraphicsToolbar(myState, new ToolbarListener()).getToolbar());
    setContent(myPanel.getPanel());

    project.getMessageBus().connect(project).subscribe(
      VirtualFileManager.VFS_CHANGES,
      new BulkVirtualFileListenerAdapter(new SnapshotDirListener())
    );

    LOGGER.info(
      String.format(STARTED_TO_LISTEN_FOR_NEW_SNAPSHOTS, mySnapshotDir.getPath())
    );
  }

  public void sync() {
    mySnapshotDir.refresh(true, true);
  }

  public void reset() {
    myPanel.reset();
    myState.reset();

    ApplicationManager.getApplication().runWriteAction(
      new Runnable() {
        @Override
        public void run() {
          for (final VirtualFile file : mySnapshotDir.getChildren()) {
            try {
              file.delete(TheRGraphicsToolWindow.this);
            }
            catch (final IOException e) {
              LOGGER.warn(e);
            }
          }
        }
      }
    );

    LOGGER.debug(TOOL_WINDOW_HAS_BEEN_RESET);
  }

  private class ToolbarListener implements TheRGraphicsToolbar.Listener {

    @Override
    public void next() {
      myState.next();
      myPanel.refresh();
    }

    @Override
    public void previous() {
      myState.previous();
      myPanel.refresh();
    }
  }

  private class SnapshotDirListener extends VirtualFileAdapter {

    @Override
    public void contentsChanged(@NotNull final VirtualFileEvent event) {
      final VirtualFile file = event.getFile();

      if (myState.isSnapshot(file)) {
        LOGGER.debug(
          String.format(UPDATED_SNAPSHOT, event.getFileName())
        );

        if (myState.isCurrent(file)) {
          myPanel.refresh();
        }
      }
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
}
