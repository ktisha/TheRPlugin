package com.jetbrains.ther.debugger.evaluator;

import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.TheRDebuggerUtils;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.ther.debugger.data.TheRCommands.expressionOnFrameCommand;

public class TheRExpressionHandlerImpl implements TheRExpressionHandler {

  private int myLastFrameNumber = 0;

  @NotNull
  @Override
  public String handle(final int frameNumber, @NotNull final String expression) {
    if (StringUtil.isJavaIdentifier(expression)) {
      return TheRDebuggerUtils.calculateValueCommand(frameNumber, expression);
    }

    if (frameNumber == myLastFrameNumber) {
      return expression;
    }
    else {
      return expressionOnFrameCommand(frameNumber, expression);
    }
  }

  @Override
  public void setLastFrameNumber(final int lastFrameNumber) {
    myLastFrameNumber = lastFrameNumber;
  }
}
