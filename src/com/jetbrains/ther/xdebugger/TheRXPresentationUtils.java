package com.jetbrains.ther.xdebugger;

import com.intellij.icons.AllIcons;
import com.intellij.xdebugger.frame.XFullValueEvaluator;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.presentation.XValuePresentation;
import com.jetbrains.ther.debugger.data.TheRVar;
import com.jetbrains.ther.debugger.utils.TheRDebuggerUtils;
import org.jetbrains.annotations.NotNull;

// TODO [xdbg][test]
public class TheRXPresentationUtils {

  public static void computePresentation(@NotNull final TheRVar var,
                                         @NotNull final XValueNode node) {
    if (isOneLine(var.getValue())) {
      setPresentation(node, var);
    }
    else {
      computeMultilinePresentation(var, node);
    }
  }

  public static void computePresentation(@NotNull final String value, @NotNull final XValueNode node) {
    if (isOneLine(value)) {
      setPresentation(node, value);
    }
    else {
      computeMultilinePresentation(value, node);
    }
  }

  private static boolean isOneLine(@NotNull final String value) {
    return TheRDebuggerUtils.findNextLineBegin(value, 0) == value.length();
  }

  private static void setPresentation(@NotNull final XValueNode node, @NotNull final TheRVar var) {
    setPresentation(node, var, var.getValue());
  }

  private static void computeMultilinePresentation(@NotNull final TheRVar var,
                                                   @NotNull final XValueNode node) {
    final String value = var.getValue();

    setPresentation(node, var, calculateShortPresentation(value));
    setFullValueEvaluator(node, value);
  }

  private static void setPresentation(@NotNull final XValueNode node, @NotNull final String value) {
    final XValuePresentation presentation = new XValuePresentation() {
      @Override
      public void renderValue(@NotNull final XValueTextRenderer renderer) {
        renderer.renderValue(value);
      }
    };

    node.setPresentation(
      AllIcons.Debugger.Value,
      presentation,
      false
    );
  }

  private static void computeMultilinePresentation(@NotNull final String value,
                                                   @NotNull final XValueNode node) {
    setPresentation(node, calculateShortPresentation(value));
    setFullValueEvaluator(node, value);
  }

  @NotNull
  private static String calculateShortPresentation(@NotNull final String value) {
    return value.substring(
      0,
      TheRDebuggerUtils.findCurrentLineEnd(value, 0)
    );
  }

  private static void setPresentation(@NotNull final XValueNode node, @NotNull final TheRVar var, @NotNull final String presentation) {
    node.setPresentation(
      AllIcons.Debugger.Value,
      var.getType(),
      presentation,
      false
    );
  }

  private static void setFullValueEvaluator(@NotNull final XValueNode node, @NotNull final String value) {
    final XFullValueEvaluator evaluator = new XFullValueEvaluator() {
      @Override
      public void startEvaluation(@NotNull final XFullValueEvaluationCallback callback) {
        callback.evaluated(value);
      }
    };

    node.setFullValueEvaluator(evaluator);
  }
}
