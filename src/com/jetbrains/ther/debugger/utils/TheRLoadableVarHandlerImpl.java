package com.jetbrains.ther.debugger.utils;

import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class TheRLoadableVarHandlerImpl implements TheRLoadableVarHandler {

  @Override
  @Nullable
  public String handleType(@NotNull final TheRProcess process, @NotNull final String var, @NotNull final String type)
    throws IOException, InterruptedException {
    if (type.equals(TheRDebugConstants.FUNCTION_TYPE)) {
      if (var.startsWith(TheRDebugConstants.SERVICE_FUNCTION_PREFIX)) {
        return null;
      }
      else {
        TheRLoadableVarHandlerUtils.traceAndDebug(process, var);
      }
    }

    return type;
  }

  @Override
  @NotNull
  public String handleValue(@NotNull final String var,
                            @NotNull final String type,
                            @NotNull final String value) {
    if (type.equals(TheRDebugConstants.FUNCTION_TYPE)) {
      final int lastLineBegin = findLastLineBegin(value);

      if (value.startsWith(TheRDebugConstants.ENVIRONMENT, lastLineBegin + "<".length())) {
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
