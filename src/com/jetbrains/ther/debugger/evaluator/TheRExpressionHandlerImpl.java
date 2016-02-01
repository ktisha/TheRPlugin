package com.jetbrains.ther.debugger.evaluator;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.ther.debugger.data.TheRCommands.*;
import static com.jetbrains.ther.debugger.data.TheRDebugConstants.CLOSURE;

public class TheRExpressionHandlerImpl implements TheRExpressionHandler {

  private int myMaxFrameNumber = 0;

  @NotNull
  @Override
  public String handle(final int frameNumber, @NotNull final String expression) {
    if (StringUtil.isJavaIdentifier(expression)) {
      return handleIdentifier(frameNumber, expression);
    }

    if (frameNumber == myMaxFrameNumber) {
      return expression;
    }
    else {
      return SYS_FRAME_COMMAND + "(" + frameNumber + ")$" + expression;
    }
  }

  @Override
  public void setMaxFrameNumber(final int maxFrameNumber) {
    myMaxFrameNumber = maxFrameNumber;
  }

  @NotNull
  private String handleIdentifier(final int frameNumber, @NotNull final String identifier) {
    final String globalIdentifier = SYS_FRAME_COMMAND + "(" + frameNumber + ")$" + identifier;

    final String isFunction = TYPEOF_COMMAND + "(" + globalIdentifier + ") == \"" + CLOSURE + "\"";
    final String isDebugged = IS_DEBUGGED_COMMAND + "(" + globalIdentifier + ")";

    return "if (" + isFunction + " && " + isDebugged + ") " +
           ATTR_COMMAND + "(" + globalIdentifier + ", \"original\")" +
           " else " +
           globalIdentifier;
  }
}
