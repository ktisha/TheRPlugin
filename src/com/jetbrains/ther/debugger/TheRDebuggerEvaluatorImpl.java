package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

// TODO [dbg][test]
public class TheRDebuggerEvaluatorImpl implements TheRDebuggerEvaluator {

  @NotNull
  private final TheRProcess myProcess;

  public TheRDebuggerEvaluatorImpl(@NotNull final TheRProcess process) {
    myProcess = process;
  }

  @Override
  public void evalCondition(@NotNull final String condition, @NotNull final ConditionReceiver receiver) {
    try {
      receiver.receiveResult(
        parseTheRBoolean(
          evaluate(condition)
        )
      );
    }
    catch (final IOException e) {
      receiver.receiveError(e);
    }
    catch (final InterruptedException e) {
      receiver.receiveError(e);
    }
  }

  @Override
  public void evalExpression(@NotNull final String expression, @NotNull final ExpressionReceiver receiver) {
    try {
      receiver.receiveResult(
        evaluate(expression)
      );
    }
    catch (final IOException e) {
      receiver.receiveError(e);
    }
    catch (final InterruptedException e) {
      receiver.receiveError(e);
    }
  }

  @NotNull
  private String evaluate(@NotNull final String expression) throws IOException, InterruptedException {
    // TODO [dbg][impl]

    return "";
  }

  private boolean parseTheRBoolean(@NotNull final String text) {
    final int prefixLength = "[1] ".length();

    return text.length() > prefixLength && Boolean.parseBoolean(text.substring(prefixLength));
  }
}
