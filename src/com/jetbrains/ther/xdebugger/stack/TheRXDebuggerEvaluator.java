package com.jetbrains.ther.xdebugger.stack;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import com.jetbrains.ther.debugger.evaluator.TheRDebuggerEvaluator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO [xdbg][test]
class TheRXDebuggerEvaluator extends XDebuggerEvaluator {

  @NotNull
  private static final Logger LOGGER = Logger.getInstance(TheRXDebuggerEvaluator.class);

  @NotNull
  private final TheRDebuggerEvaluator myEvaluator;

  public TheRXDebuggerEvaluator(@NotNull final TheRDebuggerEvaluator evaluator) {
    myEvaluator = evaluator;
  }

  // This method is overridden because XDebugSessionImpl.breakpointReached(XBreakpoint<?>, String, XSuspendContext) calls it anyway
  @Override
  public boolean evaluateCondition(@NotNull final String expression) {
    final ConditionReceiver receiver = new ConditionReceiver();

    myEvaluator.evalExpression(expression, receiver);

    return receiver.myResult;
  }

  @Override
  public void evaluate(@NotNull final String expression,
                       @NotNull final XEvaluationCallback callback,
                       @Nullable final XSourcePosition expressionPosition) {
    ApplicationManager.getApplication().executeOnPooledThread(
      new Runnable() {
        @Override
        public void run() {
          myEvaluator.evalExpression(
            expression,
            new ExpressionReceiver(callback)
          );
        }
      }
    );
  }

  private static class ConditionReceiver implements TheRDebuggerEvaluator.Receiver {

    private boolean myResult = false;

    @Override
    public void receiveResult(@NotNull final String result) {
      final int prefixLength = "[1] ".length();

      myResult = result.length() > prefixLength && Boolean.parseBoolean(result.substring(prefixLength));
    }

    @Override
    public void receiveError(@NotNull final Exception e) {
      LOGGER.info(e);
    }

    @Override
    public void receiveError(@NotNull final String error) {
      LOGGER.info(error);
    }
  }

  private static class ExpressionReceiver implements TheRDebuggerEvaluator.Receiver {

    @NotNull
    private final XEvaluationCallback myCallback;

    public ExpressionReceiver(@NotNull final XEvaluationCallback callback) {
      myCallback = callback;
    }

    @Override
    public void receiveResult(@NotNull final String result) {
      final XValue xvalue = new XValue() {
        @Override
        public void computePresentation(@NotNull final XValueNode node, @NotNull final XValuePlace place) {
          TheRXPresentationUtils.computePresentation(result, node);
        }
      };

      myCallback.evaluated(xvalue);
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
