package com.jetbrains.ther.debugger.executor;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.exception.TheRUnexpectedExecutionResultTypeException;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.appendError;

public final class TheRExecutorUtils {

  @NotNull
  public static TheRExecutionResult execute(@NotNull final TheRExecutor executor,
                                            @NotNull final String command,
                                            @NotNull final TheRExecutionResultType expectedType) throws TheRDebuggerException {
    final TheRExecutionResult result = executor.execute(command);

    if (result.getType() != expectedType) {
      throw new TheRUnexpectedExecutionResultTypeException(
        "Actual type is not the same as expected: [actual: " + result.getType() + ", expected: " + expectedType + "]"
      );
    }

    return result;
  }

  @NotNull
  public static String execute(@NotNull final TheRExecutor executor,
                               @NotNull final String command,
                               @NotNull final TheRExecutionResultType expectedType,
                               @NotNull final TheROutputReceiver receiver) throws TheRDebuggerException {
    final TheRExecutionResult result = execute(executor, command, expectedType);

    appendError(result, receiver);

    return result.getOutput();
  }

  @NotNull
  public static TheRExecutionResult execute(@NotNull final TheRExecutor executor,
                                            @NotNull final String command,
                                            @NotNull final TheROutputReceiver receiver) throws TheRDebuggerException {
    final TheRExecutionResult result = executor.execute(command);

    appendError(result, receiver);

    return result;
  }
}
