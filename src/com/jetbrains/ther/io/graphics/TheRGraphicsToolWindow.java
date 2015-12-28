package com.jetbrains.ther.io.graphics;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import org.jetbrains.annotations.NotNull;

class TheRGraphicsToolWindow extends SimpleToolWindowPanel implements Disposable, TheRGraphicsToolbar.Listener {

  @NotNull
  private final TheRGraphicsState myState;

  @NotNull
  private final TheRGraphicsPanel myPanel;

  public TheRGraphicsToolWindow(@NotNull final Project project) {
    super(true, true);

    myState = new TheRGraphicsState(project);
    myPanel = new TheRGraphicsPanel(myState);

    setToolbar(new TheRGraphicsToolbar(myState, this).getToolbar());
    setContent(myPanel.getPanel());
  }

  @Override
  public void dispose() {
    myState.dispose();
  }

  @Override
  public void next() {
    if (myState.hasNext()) {
      myPanel.showNext();
    }
  }

  @Override
  public void previous() {
    if (myState.hasPrevious()) {
      myPanel.showPrevious();
    }
  }
}
