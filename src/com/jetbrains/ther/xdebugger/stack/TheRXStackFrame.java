package com.jetbrains.ther.xdebugger.stack;

import com.intellij.icons.AllIcons;
import com.intellij.ui.ColoredTextContainer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.*;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.frame.TheRStackFrame;
import com.jetbrains.ther.debugger.frame.TheRVar;
import com.jetbrains.ther.xdebugger.TheRXDebugRunner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

class TheRXStackFrame extends XStackFrame {

  @NotNull
  private final TheRStackFrame myFrame;

  @Nullable
  private final XSourcePosition myPosition;

  @Nullable
  private TheRXDebuggerEvaluator myEvaluator;

  public TheRXStackFrame(@NotNull final TheRStackFrame frame, @Nullable final XSourcePosition position) {
    myPosition = position;
    myFrame = frame;
    myEvaluator = null;
  }

  @Nullable
  @Override
  public XSourcePosition getSourcePosition() {
    return myPosition;
  }

  @NotNull
  @Override
  public XDebuggerEvaluator getEvaluator() {
    if (myEvaluator == null) {
      myEvaluator = new TheRXDebuggerEvaluator(myFrame.getEvaluator());
    }

    return myEvaluator;
  }

  @Override
  public void computeChildren(@NotNull final XCompositeNode node) {
    TheRXDebugRunner.SINGLE_EXECUTOR.execute(
      new Runnable() {
        @Override
        public void run() {
          try {
            node.addChildren(
              transform(
                myFrame.getLoader().load()
              ),
              true
            );
          }
          catch (final TheRDebuggerException e) {
            node.setErrorMessage(e.getMessage());
          }
        }
      }
    );
  }

  @Override
  public void customizePresentation(@NotNull final ColoredTextContainer component) {
    if (myPosition == null || myFrame.getLocation().getFunctionName().equals(TheRDebugConstants.MAIN_FUNCTION_NAME)) {
      super.customizePresentation(component);
    }
    else {
      component.append(getPresentationText(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
      component.setIcon(AllIcons.Debugger.StackFrame);
    }
  }

  @NotNull
  private XValueChildrenList transform(@NotNull final List<TheRVar> vars) {
    final XValueChildrenList result = new XValueChildrenList();

    for (final TheRVar var : vars) {
      result.add(new TheRXVar(var));
    }

    return result;
  }

  @NotNull
  private String getPresentationText() {
    assert myPosition != null; // see method usages

    return myFrame.getLocation().getFunctionName() + ", " + myPosition.getFile().getName() + ":" + (myPosition.getLine() + 1);
  }

  private static class TheRXVar extends XNamedValue {

    @NotNull
    private final TheRVar myVar;

    public TheRXVar(@NotNull final TheRVar var) {
      super(var.getName());

      myVar = var;
    }

    @Override
    public void computePresentation(@NotNull final XValueNode node, @NotNull final XValuePlace place) {
      TheRXPresentationUtils.computePresentation(myVar, node);
    }

    @Nullable
    @Override
    public XValueModifier getModifier() {
      if (!myVar.getModifier().isDisabled()) {
        return new TheRXValueModifier(myVar.getModifier(), myVar.getName());
      }
      else {
        return null;
      }
    }
  }
}
