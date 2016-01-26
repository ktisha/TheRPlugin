package com.jetbrains.ther.run.run;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultCalculator;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultType;
import org.jetbrains.annotations.NotNull;

// TODO [run][test]
public class TheRRunExecutionResultCalculator implements TheRExecutionResultCalculator {

  @NotNull
  private static final String PROMPT = "> ";

  @Override
  public boolean isComplete(@NotNull final CharSequence output) {
    return StringUtil.endsWith(output, PROMPT) && StringUtil.isLineBreak(output.charAt(output.length() - PROMPT.length() - 1));
  }

  @NotNull
  @Override
  public TheRExecutionResult calculate(@NotNull final CharSequence output, @NotNull final String error) {
    final String outputString = output.toString();

    return new TheRExecutionResult(
      outputString,
      TheRExecutionResultType.RESPONSE,
      TextRange.allOf(outputString),
      error
    );
  }
}
