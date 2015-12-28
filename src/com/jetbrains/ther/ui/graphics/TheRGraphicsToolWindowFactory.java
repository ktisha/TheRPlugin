package com.jetbrains.ther.ui.graphics;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

public class TheRGraphicsToolWindowFactory implements ToolWindowFactory {

  @Override
  public void createToolWindowContent(@NotNull final Project project, @NotNull final ToolWindow toolWindow) {
    final ContentManager contentManager = toolWindow.getContentManager();

    final Content content = contentManager.getFactory().createContent(
      new TheRGraphicsToolWindow(new TheRGraphicsState(project)),
      null,
      false
    );

    contentManager.addContent(content);
  }
}
