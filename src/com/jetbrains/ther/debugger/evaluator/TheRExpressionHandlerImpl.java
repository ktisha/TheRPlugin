package com.jetbrains.ther.debugger.evaluator;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.ther.debugger.data.TheRCommands.*;
import static com.jetbrains.ther.debugger.data.TheRLanguageConstants.CLOSURE;

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
      return expressionOnFrameCommand(frameNumber, expression);
    }
  }

  @Override
  public void setMaxFrameNumber(final int maxFrameNumber) {
    myMaxFrameNumber = maxFrameNumber;
  }

  @NotNull
  private String handleIdentifier(final int frameNumber, @NotNull final String identifier) {
    final String globalIdentifier = expressionOnFrameCommand(frameNumber, identifier);

    final String isFunction = typeOfCommand(globalIdentifier) + " == \"" + CLOSURE + "\"";
    final String isDebugged = isDebuggedCommand(globalIdentifier);

    return "if (" + isFunction + " && " + isDebugged + ") " + attrCommand(globalIdentifier, "original") + " else " + globalIdentifier;
  }
}
