package com.jetbrains.ther.debugger.mock;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultType;
import org.jetbrains.annotations.NotNull;

public class AlwaysSameResultTheRExecutor extends MockTheRExecutor {

  @NotNull
  private final String myText;

  @NotNull
  private final TheRExecutionResultType myType;

  @NotNull
  private final TextRange myOutputRange;

  @NotNull
  private final String myError;

  public AlwaysSameResultTheRExecutor(@NotNull final String text,
                                      @NotNull final TheRExecutionResultType type,
                                      @NotNull final TextRange outputRange,
                                      @NotNull final String error) {
    myText = text;
    myType = type;
    myOutputRange = outputRange;
    myError = error;
  }

  @NotNull
  @Override
  protected TheRExecutionResult doExecute(@NotNull final String command) throws TheRDebuggerException {
    return new TheRExecutionResult(myText, myType, myOutputRange, myError);
  }
}
