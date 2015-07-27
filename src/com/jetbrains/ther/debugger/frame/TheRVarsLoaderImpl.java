package com.jetbrains.ther.debugger.frame;

import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.data.TheRVar;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType.EMPTY;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType.RESPONSE;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessUtils.execute;

class TheRVarsLoaderImpl implements TheRVarsLoader {

  @NotNull
  private final TheRProcess myProcess;

  @NotNull
  private final TheROutputReceiver myReceiver;

  @NotNull
  private final String myFrame;

  private boolean myIsLast;

  public TheRVarsLoaderImpl(@NotNull final TheRProcess process,
                            @NotNull final TheROutputReceiver receiver,
                            final int frameNumber,
                            final boolean last) {
    myProcess = process;
    myReceiver = receiver;
    myFrame = SYS_FRAME_COMMAND + "(" + frameNumber + ")";
    myIsLast = last;
  }

  @NotNull
  @Override
  public List<TheRVar> load() throws TheRDebuggerException {
    final String text = execute(
      myProcess,
      LS_COMMAND + "(" + myFrame + ")",
      RESPONSE,
      myReceiver
    );

    final List<TheRVar> vars = new ArrayList<TheRVar>();

    for (final String variableName : calculateVariableNames(text)) {
      final TheRVar var = loadVar(variableName);

      if (var != null) {
        vars.add(var);
      }
    }

    return vars;
  }

  @Override
  public void markAsLast() {
    myIsLast = true;
  }

  @Override
  public void markAsNotLast() {
    myIsLast = false;
  }

  @NotNull
  private List<String> calculateVariableNames(@NotNull final String response) {
    final List<String> result = new ArrayList<String>();

    for (final String line : StringUtil.splitByLines(response)) {
      for (final String token : StringUtil.tokenize(new StringTokenizer(line))) {
        final String var = getVariableName(token);

        if (var != null) {
          result.add(var);
        }
      }
    }

    return result;
  }

  @Nullable
  private TheRVar loadVar(@NotNull final String var) throws TheRDebuggerException {
    final String type = handleType(
      var,
      execute(
        myProcess,
        TYPEOF_COMMAND + "(" + myFrame + "$" + var + ")",
        RESPONSE,
        myReceiver
      )
    );

    if (type == null) {
      return null;
    }

    return new TheRVar(
      var,
      type,
      loadValue(var, type)
    );
  }

  @Nullable
  private String getVariableName(@NotNull final String token) {
    final boolean isNotEmptyQuotedString = StringUtil.isQuotedString(token) && token.length() > 2;

    if (isNotEmptyQuotedString) {
      return token.substring(1, token.length() - 1);
    }
    else {
      return null;
    }
  }

  @Nullable
  private String handleType(@NotNull final String var,
                            @NotNull final String type)
    throws TheRDebuggerException {
    if (type.equals(FUNCTION_TYPE)) {
      if (isService(var)) {
        return null;
      }
      else if (myIsLast) {
        execute(myProcess, enterFunction(var), EMPTY, myReceiver);
        execute(myProcess, exitFunction(var), EMPTY, myReceiver);
        execute(myProcess, traceCommand(var), RESPONSE, myReceiver);
        execute(myProcess, debugCommand(var), EMPTY, myReceiver);
      }
    }

    return type;
  }

  @NotNull
  private String loadValue(@NotNull final String var,
                           @NotNull final String type) throws TheRDebuggerException {
    return handleValue(
      type,
      execute(
        myProcess,
        valueCommand(var, type),
        RESPONSE,
        myReceiver
      )
    );
  }

  private boolean isService(@NotNull final String var) {
    return var.startsWith(SERVICE_FUNCTION_PREFIX) &&
           (var.endsWith(SERVICE_ENTER_FUNCTION_SUFFIX) || var.endsWith(SERVICE_EXIT_FUNCTION_SUFFIX));
  }

  @NotNull
  private String enterFunction(@NotNull final String var) {
    return enterFunctionName(var) + " <- function() { print(\"" + var + "\") }";
  }

  @NotNull
  private String exitFunction(@NotNull final String var) {
    return exitFunctionName(var) + " <- function() { print(\"" + var + "\") }";
  }

  @NotNull
  private String traceCommand(@NotNull final String var) {
    return TRACE_COMMAND +
           "(" +
           var +
           ", " +
           enterFunctionName(var) +
           ", exit = " +
           exitFunctionName(var) +
           ", where = environment()" +
           ")";
  }

  @NotNull
  private String debugCommand(@NotNull final String var) {
    return DEBUG_COMMAND + "(" + var + ")";
  }

  @NotNull
  private String handleValue(@NotNull final String type,
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

  @NotNull
  private String valueCommand(@NotNull final String var, @NotNull final String type) {
    if (type.equals(FUNCTION_TYPE)) {
      return ATTR_COMMAND + "(" + myFrame + "$" + var + ", \"original\")";
    }
    else {
      return PRINT_COMMAND + "(" + myFrame + "$" + var + ")";
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

  @NotNull
  private String enterFunctionName(@NotNull final String var) {
    return SERVICE_FUNCTION_PREFIX + var + SERVICE_ENTER_FUNCTION_SUFFIX;
  }

  @NotNull
  private String exitFunctionName(@NotNull final String var) {
    return SERVICE_FUNCTION_PREFIX + var + SERVICE_EXIT_FUNCTION_SUFFIX;
  }
}
