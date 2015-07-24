package com.jetbrains.ther.debugger.interpreter;

import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessCommandUtils.*;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessUtils.execute;

public class TheRLoadableVarHandlerImpl implements TheRLoadableVarHandler {

  @Override
  @Nullable
  public String handleType(@NotNull final TheRProcess process, @NotNull final String var, @NotNull final String type)
    throws TheRDebuggerException {
    if (type.equals(FUNCTION_TYPE)) {
      if (isService(var)) {
        return null;
      }
      else {
        execute(process, enterFunction(var), TheRProcessResponseType.EMPTY); // TODO [dbg][update]
        execute(process, exitFunction(var), TheRProcessResponseType.EMPTY); // TODO [dbg][update]
        execute(process, traceCommand(var), TheRProcessResponseType.RESPONSE); // TODO [dbg][update]
        execute(process, debugCommand(var), TheRProcessResponseType.EMPTY); // TODO [dbg][update]
      }
    }

    return type;
  }

  @Override
  @NotNull
  public String handleValue(@NotNull final String var,
                            @NotNull final String type,
                            @NotNull final String value) {
    if (type.equals(FUNCTION_TYPE)) {
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
    else {
      return value;
    }
  }

  private boolean isService(@NotNull final String var) {
    return var.startsWith(SERVICE_FUNCTION_PREFIX) &&
           (var.endsWith(SERVICE_ENTER_FUNCTION_SUFFIX) || var.endsWith(SERVICE_EXIT_FUNCTION_SUFFIX));
  }

  private int findLastLineBegin(@NotNull final String text) {
    int current = text.length() - 1;

    while (current > -1 && !StringUtil.isLineBreak(text.charAt(current))) {
      current--;
    }

    return current + 1;
  }

  private int findLastButOneLineEnd(@NotNull final String text, final int lastLineBegin) {
    int current = lastLineBegin - 1;

    while (current > -1 && StringUtil.isLineBreak(text.charAt(current))) {
      current--;
    }

    return current + 1;
  }
}
