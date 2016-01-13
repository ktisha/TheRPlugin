package com.jetbrains.ther.ui.graphics;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TheRGraphicsToolWindowFactory implements ToolWindowFactory {

  @Override
  public void createToolWindowContent(@NotNull final Project project, @NotNull final ToolWindow toolWindow) {
    final ContentManager contentManager = toolWindow.getContentManager();

    final Content content = contentManager.getFactory().createContent(
      createToolWindow(project, TheRGraphicsUtils.findOrCreateSnapshotDir(project)),
      null,
      false
    );

    contentManager.addContent(content);
  }

  @NotNull
  private SimpleToolWindowPanel createToolWindow(@NotNull final Project project, @Nullable final VirtualFile snapshotDir) {
    if (snapshotDir == null) {
      return new TheRGraphicsEmptyToolWindow("Snapshot directory is not available");
    }
    else {
      return new TheRGraphicsToolWindow(project, snapshotDir, TheRGraphicsUtils.getGraphicsState(snapshotDir));
    }
  }
}
