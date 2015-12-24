package com.jetbrains.ther.io.graphics;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

public class TheRGraphicsToolWindowFactory implements ToolWindowFactory {

  @Override
  public void createToolWindowContent(@NotNull final Project project, @NotNull final ToolWindow toolWindow) {
    final ContentManager contentManager = toolWindow.getContentManager();
    final TheRGraphicsToolWindow graphicsToolWindow = new TheRGraphicsToolWindow(project);

    final Content content = contentManager.getFactory().createContent(graphicsToolWindow, null, false);
    contentManager.addContent(content);

    Disposer.register(project, graphicsToolWindow);
  }
}
