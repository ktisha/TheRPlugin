package com.jetbrains.ther.run.run;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultCalculator;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultType;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.*;

public class TheRRunExecutionResultCalculator implements TheRExecutionResultCalculator {

  @Override
  public boolean isComplete(@NotNull final CharSequence output) {
    final int promptIndex = output.length() - TheRDebugConstants.PROMPT.length();

    return StringUtil.endsWith(output, TheRDebugConstants.PROMPT) &&
           promptIndex > 0 &&
           StringUtil.isLineBreak(output.charAt(promptIndex - 1));
  }

  @NotNull
  @Override
  public TheRExecutionResult calculate(@NotNull final CharSequence output, @NotNull final String error) {
    final String result = calculateResult(output);

    return new TheRExecutionResult(
      result,
      TheRExecutionResultType.RESPONSE,
      TextRange.allOf(result),
      error
    );
  }

  @NotNull
  private String calculateResult(@NotNull final CharSequence output) {
    final int leftBound = findNextLineBegin(output, 0);
    final int rightBound = findLastButOneLineEnd(output, findLastLineBegin(output));

    if (leftBound >= rightBound) {
      return "";
    }
    else {
      return output.subSequence(leftBound, rightBound).toString();
    }
  }
}
