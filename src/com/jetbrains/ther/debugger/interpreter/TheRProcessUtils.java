package com.jetbrains.ther.debugger.interpreter;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.exception.UnexpectedResponseException;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.appendError;

// TODO [dbg][test]
public final class TheRProcessUtils {

  @NotNull
  public static TheRProcessResponse execute(@NotNull final TheRProcess process,
                                            @NotNull final String command,
                                            @NotNull final TheRProcessResponseType expectedType) throws TheRDebuggerException {
    final TheRProcessResponse response = process.execute(command);

    if (response.getType() != expectedType) {
      throw new UnexpectedResponseException(
        "Actual response type is not the same as expected: [actual: " + response.getType() + ", expected: " + expectedType + "]"
      );
    }

    return response;
  }

  @NotNull
  public static String execute(@NotNull final TheRProcess process,
                               @NotNull final String command,
                               @NotNull final TheRProcessResponseType expectedType,
                               @NotNull final TheROutputReceiver receiver) throws TheRDebuggerException {
    final TheRProcessResponse response = execute(process, command, expectedType);

    appendError(response, receiver);

    return response.getOutput();
  }
}
