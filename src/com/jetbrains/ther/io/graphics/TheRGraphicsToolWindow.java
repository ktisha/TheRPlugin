package com.jetbrains.ther.io.graphics;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;

public class TheRGraphicsToolWindow extends SimpleToolWindowPanel implements Disposable {

  @NotNull
  private final TheRGraphicsToolWindowState myState;

  @NotNull
  private final JLabel myGraphicsLabel;

  public TheRGraphicsToolWindow(@NotNull final Project project) {
    super(true, true);

    myState = new TheRGraphicsToolWindowState(project);
    myGraphicsLabel = createGraphicsLabel();

    final JPanel panel = new JPanel();
    panel.add(myGraphicsLabel);

    setToolbar(createToolbarPanel());
    setContent(panel);
  }

  @Override
  public void dispose() {
    myState.dispose();
  }

  @NotNull
  private JLabel createGraphicsLabel() {
    final JLabel label = new JLabel();

    if (myState.hasNext()) {
      try {
        label.setIcon(new ImageIcon(myState.next()));
      }
      catch (final IOException e) {
        // todo
      }
    }
    else {
      label.setText("No graphics");
    }

    return label;
  }

  @NotNull
  private JPanel createToolbarPanel() {
    final DefaultActionGroup actionGroup = new DefaultActionGroup();

    actionGroup.add(new PrevGraphicsAction());
    actionGroup.add(new NextGraphicsAction());

    final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("Graphics", actionGroup, true);

    return JBUI.Panels.simplePanel(actionToolbar.getComponent());
  }

  private class PrevGraphicsAction extends AnAction {

    public PrevGraphicsAction() {
      super("Previous graphics", "Previous graphics", AllIcons.Actions.Back);
    }

    @Override
    public void actionPerformed(final AnActionEvent e) {
      if (myState.hasPrevious()) {
        try {
          myGraphicsLabel.setIcon(new ImageIcon(myState.previous()));
        }
        catch (final IOException exception) {
          // todo
        }
      }
    }

    @Override
    public void update(final AnActionEvent e) {
      final boolean hasPrevious = myState.hasPrevious();

      if (e.getPresentation().isEnabled() != hasPrevious) {
        e.getPresentation().setEnabled(hasPrevious);
      }
    }
  }

  private class NextGraphicsAction extends AnAction {

    public NextGraphicsAction() {
      super("Next graphics", "Next graphics", AllIcons.Actions.Forward);
    }

    @Override
    public void actionPerformed(final AnActionEvent e) {
      if (myState.hasNext()) {
        try {
          myGraphicsLabel.setIcon(new ImageIcon(myState.next()));
        }
        catch (final IOException exception) {
          // todo
        }
      }
    }

    @Override
    public void update(final AnActionEvent e) {
      final boolean hasNext = myState.hasNext();

      if (e.getPresentation().isEnabled() != hasNext) {
        e.getPresentation().setEnabled(hasNext);
      }
    }
  }
}
