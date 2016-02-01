package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerFactory;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.findLastButOneLineEnd;
import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.findLastLineBegin;
import static com.jetbrains.ther.debugger.data.TheRCommands.*;
import static com.jetbrains.ther.debugger.data.TheRLanguageConstants.CLOSURE;
import static com.jetbrains.ther.debugger.data.TheRLanguageConstants.FUNCTION_TYPE;
import static com.jetbrains.ther.debugger.data.TheRResponseConstants.ENVIRONMENT;

public final class TheRDebuggerUtils {

  @NotNull
  public static String forciblyEvaluateFunction(@NotNull final TheRExecutor executor,
                                                @NotNull final TheRFunctionDebuggerFactory factory,
                                                @NotNull final TheROutputReceiver receiver) throws TheRDebuggerException {
    final TheRForcedFunctionDebuggerHandler handler = new TheRForcedFunctionDebuggerHandler(executor, factory, receiver);

    //noinspection StatementWithEmptyBody
    while (handler.advance()) {
    }

    return handler.getResult();
  }

  @NotNull
  public static String handleValue(@NotNull final String value) {
    final int lastLineBegin = findLastLineBegin(value);

    if (value.startsWith(ENVIRONMENT, lastLineBegin + "<".length())) {
      return value.substring(
        0,
        findLastButOneLineEnd(value, lastLineBegin)
      );
    }
    else {
      return value;
    }
  }

  @NotNull
  public static String handleValue(@NotNull final String type, @NotNull final String value) {
    if (type.equals(FUNCTION_TYPE)) {
      return handleValue(value);
    }
    else {
      return value;
    }
  }

  @NotNull
  public static String calculateValueCommand(final int frameNumber, @NotNull final String var) {
    final String globalVar = expressionOnFrameCommand(frameNumber, var);

    final String isFunction = typeOfCommand(globalVar) + " == \"" + CLOSURE + "\"";
    final String isDebugged = isDebuggedCommand(globalVar);

    return "if (" + isFunction + " && " + isDebugged + ") " + attrCommand(globalVar, "original") + " else " + globalVar;
  }
}
