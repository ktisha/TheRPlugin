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
  public String handleValue(@NotNull final TheRProcess process,
                            @NotNull final String var,
                            @NotNull final String type,
                            @NotNull final String value) {
    if (type.equals(TheRDebugConstants.FUNCTION_TYPE)) {
      final String[] lines = StringUtil.splitByLinesKeepSeparators(value);
      final StringBuilder sb = new StringBuilder();

      for (int i = 2; i < lines.length - 1; i++) {
        sb.append(lines[i]);
      }

      while (StringUtil.endsWithLineBreak(sb)) {
        sb.setLength(sb.length() - 1);
      }

      return sb.toString();
    }
    else {
      return value;
    }
  }
}
