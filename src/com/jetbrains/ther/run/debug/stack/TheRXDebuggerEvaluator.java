package com.jetbrains.ther.run.debug.stack;

import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.jetbrains.ther.debugger.evaluator.TheRDebuggerEvaluator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutorService;

// TODO [xdbg][test]
class TheRXDebuggerEvaluator extends XDebuggerEvaluator {

  @NotNull
  private final TheRDebuggerEvaluator myEvaluator;

  @NotNull
  private final ExecutorService myExecutor;

  public TheRXDebuggerEvaluator(@NotNull final TheRDebuggerEvaluator evaluator, @NotNull final ExecutorService executor) {
    myEvaluator = evaluator;
    myExecutor = executor;
  }

  @Override
  public void evaluate(@NotNull final String expression,
                       @NotNull final XEvaluationCallback callback,
                       @Nullable final XSourcePosition expressionPosition) {
    myExecutor.execute(
      new Runnable() {
        @Override
        public void run() {
          myEvaluator.evaluate(
            expression,
            new ExpressionReceiver(callback)
          );
        }
      }
    );
  }

  private static class ExpressionReceiver implements TheRDebuggerEvaluator.Receiver {

    @NotNull
    private final XEvaluationCallback myCallback;

    public ExpressionReceiver(@NotNull final XEvaluationCallback callback) {
      myCallback = callback;
    }

    @Override
    public void receiveResult(@NotNull final String result) {
      myCallback.evaluated(new TheRXValue(result));
    }

    @Override
    public void receiveError(@NotNull final Exception e) {
      receiveError(e.getMessage());
    }

    @Override
    public void receiveError(@NotNull final String error) {
      myCallback.errorOccurred(error);
    }
  }
}
