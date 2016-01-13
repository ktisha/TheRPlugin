package com.jetbrains.ther.ui.graphics;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.impl.BulkVirtualFileListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
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
      new BulkVirtualFileListenerAdapter(new DirListener())
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

  private class DirListener extends TheRGraphicsListener {

    public DirListener() {
      super(myState);
    }

    @Override
    public void contentsChanged(@NotNull final VirtualFileEvent event) {
      final VirtualFile file = event.getFile();

      if (myState.isSnapshot(file)) {
        LOGGER.debug(
          String.format(UPDATED_SNAPSHOT, event.getFileName())
        );

        if (isCurrentSnapshot(file)) {
          myPanel.refresh();
        }
      }
    }

    private boolean isCurrentSnapshot(@NotNull final VirtualFile file) {
      try {
        final String currentName = myState.current().getName();
        final String name = file.getName();

        return currentName.equals(name);
      }
      catch (final FileNotFoundException e) {
        LOGGER.error(e);

        return false;
      }
    }
  }
}
