package com.jetbrains.ther.ui.graphics;

import com.intellij.openapi.ui.SimpleToolWindowPanel;
import org.jetbrains.annotations.NotNull;

class TheRGraphicsToolWindow extends SimpleToolWindowPanel implements TheRGraphicsState.Listener {

  @NotNull
  private final TheRGraphicsState myState;

  @NotNull
  private final TheRGraphicsPanel myPanel;

  public TheRGraphicsToolWindow(@NotNull final TheRGraphicsState state) {
    super(true, true);

    myState = state;

    if (!myState.hasCurrent()) {
      while (myState.hasNext()) {
        myState.next();
      }
    }

    myPanel = new TheRGraphicsPanel(myState);

    if (myState.hasCurrent()) {
      myPanel.refresh();
    }

    setToolbar(new TheRGraphicsToolbar(myState, new ToolbarListener()).getToolbar());
    setContent(myPanel.getPanel());

    myState.addListener(this);
  }

  @Override
  public void onAdd() {
    if (myState.hasNext()) {
      myState.next();
    }
  }

  @Override
  public void onCurrentChange() {
    myPanel.refresh();
  }

  @Override
  public void onReset() {
    myPanel.reset();
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
