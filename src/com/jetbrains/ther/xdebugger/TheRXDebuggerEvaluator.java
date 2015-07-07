package com.jetbrains.ther.xdebugger;

import com.intellij.icons.AllIcons;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import com.jetbrains.ther.debugger.TheRDebuggerEvaluator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TheRXDebuggerEvaluator extends XDebuggerEvaluator {

  @NotNull
  private final TheRDebuggerEvaluator myEvaluator;

  public TheRXDebuggerEvaluator(@NotNull final TheRDebuggerEvaluator evaluator) {
    myEvaluator = evaluator;
  }

  // This method is overridden because XDebugSessionImpl.breakpointReached(XBreakpoint<?>, String, XSuspendContext) calls it anyway
  @Override
  public boolean evaluateCondition(@NotNull final String expression) {
    final boolean[] justResult = {false};

    myEvaluator.evalCondition(
      expression,
      new TheRDebuggerEvaluator.ConditionReceiver() {
        @Override
        public void receiveResult(@NotNull final Boolean result) {
          justResult[0] = result;
        }

        @Override
        public void receiveError(@NotNull final Exception e) {
          // TODO [xdbg][update]
        }

        @Override
        public void receiveError(@NotNull final String error) {
          // TODO [xdbg][update]
        }
      }
    );

    return justResult[0];
  }

  @Override
  public void evaluate(@NotNull final String expression,
                       @NotNull final XEvaluationCallback callback,
                       @Nullable final XSourcePosition expressionPosition) {
    myEvaluator.evalExpression(
      expression,
      new TheRDebuggerEvaluator.ExpressionReceiver() {
        @Override
        public void receiveResult(@NotNull final String result) {
          callback.evaluated(
            new XValue() {
              @Override
              public void computePresentation(@NotNull final XValueNode node, @NotNull final XValuePlace place) {
                node.setPresentation(AllIcons.Debugger.Value, "type", result, false); // TODO [xdbg][update]
              }
            }
          );
        }

        @Override
        public void receiveError(@NotNull final Exception e) {
          callback.errorOccurred(e.getMessage());
        }

        @Override
        public void receiveError(@NotNull final String error) {
          callback.errorOccurred(error);
        }
      }
    );
  }
}
