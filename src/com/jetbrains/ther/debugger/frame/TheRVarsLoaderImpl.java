package com.jetbrains.ther.debugger.frame;

import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.TheRDebuggerUtils;
import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.exception.TheRUnexpectedExecutionResultTypeException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static com.jetbrains.ther.debugger.TheRDebuggerUtils.calculateRepresentation;
import static com.jetbrains.ther.debugger.TheRDebuggerUtils.calculateValueCommand;
import static com.jetbrains.ther.debugger.data.TheRCommands.*;
import static com.jetbrains.ther.debugger.data.TheRLanguageConstants.FUNCTION_TYPE;
import static com.jetbrains.ther.debugger.executor.TheRExecutionResultType.DEBUG_AT;
import static com.jetbrains.ther.debugger.executor.TheRExecutionResultType.RESPONSE;
import static com.jetbrains.ther.debugger.executor.TheRExecutorUtils.execute;

// TODO [dbg][upd_test]
class TheRVarsLoaderImpl implements TheRVarsLoader {

  @NotNull
  private final TheRExecutor myExecutor;

  @NotNull
  private final TheROutputReceiver myReceiver;

  @NotNull
  private final TheRValueModifier myModifier;

  private final int myFrameNumber;

  public TheRVarsLoaderImpl(@NotNull final TheRExecutor executor,
                            @NotNull final TheROutputReceiver receiver,
                            @NotNull final TheRValueModifier modifier,
                            final int frameNumber) {
    myExecutor = executor;
    myReceiver = receiver;
    myModifier = modifier;
    myFrameNumber = frameNumber;
  }

  @NotNull
  @Override
  public List<TheRVar> load() throws TheRDebuggerException {
    final String text = execute(
      myExecutor,
      lsCommand(myFrameNumber),
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

  @NotNull
  private List<String> calculateVariableNames(@NotNull final String text) {
    final List<String> result = new ArrayList<String>();

    for (final String line : StringUtil.splitByLines(text)) {
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
    final String type = execute(
      myExecutor,
      typeOfCommand(expressionOnFrameCommand(myFrameNumber, var)),
      RESPONSE,
      myReceiver
    );

    if (type.equals(FUNCTION_TYPE) && TheRDebuggerUtils.isServiceName(var)) {
      return null;
    }

    return new TheRVar(
      var,
      type,
      loadValue(var, type),
      myModifier
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

  @NotNull
  private String loadValue(@NotNull final String var,
                           @NotNull final String type) throws TheRDebuggerException {
    final TheRExecutionResult result = execute(myExecutor, calculateValueCommand(myFrameNumber, var), myReceiver);

    switch (result.getType()) {
      case RESPONSE:
        return calculateRepresentation(
          type,
          result.getOutput()
        );
      case DEBUG_AT:
        return calculateRepresentation(
          type,
          execute(
            myExecutor,
            EXECUTE_AND_STEP_COMMAND,
            RESPONSE,
            myReceiver
          )
        );
      default:
        throw new TheRUnexpectedExecutionResultTypeException(
          "Actual type is not the same as expected: " +
          "[" +
          "actual: " + result.getType() + ", " +
          "expected: " +
          "[" + RESPONSE + ", " + DEBUG_AT + "]" +
          "]"
        );
    }
  }
}
