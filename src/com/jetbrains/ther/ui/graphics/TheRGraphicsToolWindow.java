package com.jetbrains.ther.ui.graphics;

import com.intellij.openapi.ui.SimpleToolWindowPanel;
import org.jetbrains.annotations.NotNull;

class TheRGraphicsToolWindow extends SimpleToolWindowPanel {

  @NotNull
  private final TheRGraphicsState myState;

  public TheRGraphicsToolWindow(@NotNull final TheRGraphicsState state) {
    super(true, true);

    myState = state;

    setToolbar(new TheRGraphicsToolbar(myState, new ToolbarListener()).getToolbar());
    setContent(new TheRGraphicsPanel(myState).getPanel());
  }

  private class ToolbarListener implements TheRGraphicsToolbar.Listener {

    @Override
    public void next() {
      myState.next();
    }

    @Override
    public void previous() {
      myState.previous();
    }
  }
}
