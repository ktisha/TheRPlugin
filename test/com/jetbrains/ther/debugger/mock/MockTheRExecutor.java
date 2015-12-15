package com.jetbrains.ther.debugger.mock;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultType;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.ther.debugger.function.TheRTraceAndDebugUtilsTest.LS_FUNCTIONS_COMMAND;
import static com.jetbrains.ther.debugger.function.TheRTraceAndDebugUtilsTest.NO_FUNCTIONS_RESULT;

public abstract class MockTheRExecutor implements TheRExecutor {

  @NotNull
  public static final String LS_FUNCTIONS_ERROR = "error_ls";

  private int myCounter = 0;

  @NotNull
  @Override
  public TheRExecutionResult execute(@NotNull final String command) throws TheRDebuggerException {
    myCounter++;

    if (useNoFunctionsResult() && command.equals(LS_FUNCTIONS_COMMAND)) {
      return new TheRExecutionResult(
        NO_FUNCTIONS_RESULT,
        TheRExecutionResultType.RESPONSE,
        TextRange.allOf(NO_FUNCTIONS_RESULT),
        LS_FUNCTIONS_ERROR
      );
    }

    return doExecute(command);
  }

  public int getCounter() {
    return myCounter;
  }

  protected boolean useNoFunctionsResult() {
    return true;
  }

  @NotNull
  protected abstract TheRExecutionResult doExecute(@NotNull final String command) throws TheRDebuggerException;
}
