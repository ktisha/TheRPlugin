package com.jetbrains.ther.ui.graphics;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import org.jetbrains.annotations.NotNull;

class TheRGraphicsToolWindow extends SimpleToolWindowPanel {

  @NotNull
  private static final Logger LOGGER = Logger.getInstance(TheRGraphicsToolWindow.class);

  @NotNull
  private static final String ATTEMPT_TO_OPEN_UNAVAILABLE_NEXT_SNAPSHOT = "Attempt to open next snapshot which is unavailable";

  @NotNull
  private static final String ATTEMPT_TO_OPEN_UNAVAILABLE_PREVIOUS_SNAPSHOT = "Attempt to open previous snapshot which is unavailable";

  @NotNull
  private final TheRGraphicsState myState;

  @NotNull
  private final TheRGraphicsPanel myPanel;

  public TheRGraphicsToolWindow(@NotNull final TheRGraphicsState state) {
    super(true, true);

    myState = state;
    myPanel = new TheRGraphicsPanel(myState);

    setToolbar(new TheRGraphicsToolbar(myState, new ToolbarListener()).getToolbar());
    setContent(myPanel.getPanel());

    myState.addListener(new StateListener());
  }

  private class ToolbarListener implements TheRGraphicsToolbar.Listener {

    @Override
    public void next() {
      if (myState.hasNext()) {
        myPanel.showNext();
      }
      else {
        LOGGER.warn(ATTEMPT_TO_OPEN_UNAVAILABLE_NEXT_SNAPSHOT);
      }
    }

    @Override
    public void previous() {
      if (myState.hasPrevious()) {
        myPanel.showPrevious();
      }
      else {
        LOGGER.warn(ATTEMPT_TO_OPEN_UNAVAILABLE_PREVIOUS_SNAPSHOT);
      }
    }
  }

  private class StateListener implements TheRGraphicsState.Listener {

    @Override
    public void currentUpdate() {
      myPanel.updateCurrent();
    }
  }
}
