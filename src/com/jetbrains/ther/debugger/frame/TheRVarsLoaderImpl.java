package com.jetbrains.ther.debugger.frame;

import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.TheRDebuggerStringUtils;
import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.exception.TheRUnexpectedExecutionResultException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static com.jetbrains.ther.debugger.data.TheRCommands.*;
import static com.jetbrains.ther.debugger.data.TheRFunctionConstants.SERVICE_ENTER_FUNCTION_SUFFIX;
import static com.jetbrains.ther.debugger.data.TheRFunctionConstants.SERVICE_FUNCTION_PREFIX;
import static com.jetbrains.ther.debugger.data.TheRLanguageConstants.CLOSURE;
import static com.jetbrains.ther.debugger.data.TheRLanguageConstants.FUNCTION_TYPE;
import static com.jetbrains.ther.debugger.executor.TheRExecutionResultType.DEBUG_AT;
import static com.jetbrains.ther.debugger.executor.TheRExecutionResultType.RESPONSE;
import static com.jetbrains.ther.debugger.executor.TheRExecutorUtils.execute;

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
    final String type = handleType(
      var,
      execute(
        myExecutor,
        typeOfCommand(expressionOnFrameCommand(myFrameNumber, var)),
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

  @Nullable
  private String handleType(@NotNull final String var,
                            @NotNull final String type) {
    if (type.equals(FUNCTION_TYPE) && isService(var)) {
      return null;
    }

    return type;
  }

  @NotNull
  private String loadValue(@NotNull final String var,
                           @NotNull final String type) throws TheRDebuggerException {
    final TheRExecutionResult result = execute(myExecutor, valueCommand(var), myReceiver);

    switch (result.getType()) {
      case RESPONSE:
        return handleValue(
          type,
          result.getOutput()
        );
      case DEBUG_AT:
        return handleValue(
          type,
          execute(
            myExecutor,
            EXECUTE_AND_STEP_COMMAND,
            RESPONSE,
            myReceiver
          )
        );
      default:
        throw new TheRUnexpectedExecutionResultException(
          "Actual type is not the same as expected: " +
          "[" +
          "actual: " + result.getType() + ", " +
          "expected: " +
          "[" + RESPONSE + ", " + DEBUG_AT + "]" +
          "]"
        );
    }
  }

  private boolean isService(@NotNull final String var) {
    return var.startsWith(SERVICE_FUNCTION_PREFIX) && var.endsWith(SERVICE_ENTER_FUNCTION_SUFFIX);
  }

  @NotNull
  private String handleValue(@NotNull final String type,
                             @NotNull final String value) {
    if (type.equals(FUNCTION_TYPE)) {
      return TheRDebuggerStringUtils.handleFunctionValue(value);
    }
    else {
      return value;
    }
  }

  @NotNull
  private String valueCommand(@NotNull final String var) {
    final String globalVar = expressionOnFrameCommand(myFrameNumber, var);

    final String isFunction = typeOfCommand(globalVar) + " == \"" + CLOSURE + "\"";
    final String isDebugged = isDebuggedCommand(globalVar);

    return "if (" + isFunction + " && " + isDebugged + ") " + attrCommand(globalVar, "original") + " else " + globalVar;
  }
}
