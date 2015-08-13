package com.jetbrains.ther.xdebugger.stack;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.util.NotNullFunction;
import com.intellij.xdebugger.frame.XFullValueEvaluator;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.presentation.XValuePresentation;
import com.jetbrains.ther.debugger.frame.TheRVar;
import com.jetbrains.ther.debugger.mock.IllegalTheRValueModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.Assert.*;

public class TheRXPresentationUtilsTest {

  @Test
  public void oneLineVar() {
    final OneLineVarNode node = new OneLineVarNode();

    TheRXPresentationUtils.computePresentation(
      new TheRVar("nm", "tp", "vl", new IllegalTheRValueModifier()),
      node
    );

    assertTrue(node.check());
  }

  @Test
  public void multilineVar() {
    final MultilineVarNode node = new MultilineVarNode();

    TheRXPresentationUtils.computePresentation(
      new TheRVar("nm", "tp", " m  l   t    \nvl", new IllegalTheRValueModifier()),
      node
    );

    assertTrue(node.check());
  }

  @Test
  public void oneLineValue() {
    final OneLineNode node = new OneLineNode();

    TheRXPresentationUtils.computePresentation(
      "vl",
      node
    );

    assertTrue(node.check());
  }

  @Test
  public void multilineValue() {
    final MultilineNode node = new MultilineNode();

    TheRXPresentationUtils.computePresentation(
      " m  l   t    \nvl",
      node
    );

    assertTrue(node.check());
  }

  private static class IllegalXValueNode implements XValueNode {

    @Override
    public void setPresentation(@Nullable final Icon icon,
                                @Nullable final String type,
                                @NotNull final String value,
                                final boolean hasChildren) {
      throw new IllegalStateException("SetPresentation shouldn't be called");
    }

    @Override
    public void setPresentation(@Nullable final Icon icon, @NotNull final XValuePresentation presentation, final boolean hasChildren) {
      throw new IllegalStateException("SetPresentation shouldn't be called");
    }

    @Override
    public void setPresentation(@Nullable final Icon icon,
                                @Nullable final String type,
                                @NotNull final String separator,
                                @Nullable final String value,
                                final boolean hasChildren) {
      throw new IllegalStateException("SetPresentation shouldn't be called");
    }

    @Override
    public void setPresentation(@Nullable final Icon icon,
                                @Nullable final String type,
                                @NotNull final String value,
                                @Nullable final NotNullFunction<String, String> valuePresenter,
                                final boolean hasChildren) {
      throw new IllegalStateException("SetPresentation shouldn't be called");
    }

    @Override
    public void setPresentation(@Nullable final Icon icon,
                                @Nullable final String type,
                                @NotNull final String separator,
                                @NotNull final String value,
                                @Nullable final NotNullFunction<String, String> valuePresenter,
                                final boolean hasChildren) {
      throw new IllegalStateException("SetPresentation shouldn't be called");
    }

    @Override
    public void setFullValueEvaluator(@NotNull final XFullValueEvaluator fullValueEvaluator) {
      throw new IllegalStateException("SetFullValueEvaluator shouldn't be called");
    }

    @Override
    public boolean isObsolete() {
      throw new IllegalStateException("IsObsolete shouldn't be called");
    }
  }

  private static class IllegalXValueTextRenderer implements XValuePresentation.XValueTextRenderer {

    @Override
    public void renderValue(@NotNull final String value) {
      throw new IllegalStateException("RenderValue shouldn't be called");
    }

    @Override
    public void renderStringValue(@NotNull final String value) {
      throw new IllegalStateException("RenderStringValue shouldn't be called");
    }

    @Override
    public void renderNumericValue(@NotNull final String value) {
      throw new IllegalStateException("RenderNumericValue shouldn't be called");
    }

    @Override
    public void renderKeywordValue(@NotNull final String value) {
      throw new IllegalStateException("RenderKeywordValue shouldn't be called");
    }

    @Override
    public void renderValue(@NotNull final String value, @NotNull final TextAttributesKey key) {
      throw new IllegalStateException("RenderValue shouldn't be called");
    }

    @Override
    public void renderStringValue(@NotNull final String value,
                                  @Nullable final String additionalSpecialCharsToHighlight,
                                  final int maxLength) {
      throw new IllegalStateException("RenderStringValue shouldn't be called");
    }

    @Override
    public void renderComment(@NotNull final String comment) {
      throw new IllegalStateException("RenderComment shouldn't be called");
    }

    @Override
    public void renderSpecialSymbol(@NotNull final String symbol) {
      throw new IllegalStateException("RenderSpecialSymbol shouldn't be called");
    }

    @Override
    public void renderError(@NotNull final String error) {
      throw new IllegalStateException("RenderError shouldn't be called");
    }
  }

  private static class IllegalXFullValueEvaluationCallback implements XFullValueEvaluator.XFullValueEvaluationCallback {

    @Override
    public void evaluated(@NotNull final String fullValue) {
      throw new IllegalStateException("Evaluated shouldn't be called");
    }

    @Override
    public void evaluated(@NotNull final String fullValue, @Nullable final Font font) {
      throw new IllegalStateException("Evaluated shouldn't be called");
    }

    @Override
    public boolean isObsolete() {
      throw new IllegalStateException("IsObsolete shouldn't be called");
    }

    @Override
    public void errorOccurred(@NotNull final String errorMessage) {
      throw new IllegalStateException("ErrorOccurred shouldn't be called");
    }
  }

  private static class OneLineVarNode extends IllegalXValueNode {

    private int myPres = 0;

    @Override
    public void setPresentation(@Nullable final Icon icon,
                                @Nullable final String type,
                                @NotNull final String value,
                                final boolean hasChildren) {
      myPres++;

      assertEquals(AllIcons.Debugger.Value, icon);
      assertEquals("tp", type);
      assertEquals("vl", value);
      assertFalse(hasChildren);
    }

    public boolean check() {
      return myPres == 1;
    }
  }

  private static class MultilineVarNode extends IllegalXValueNode {

    private int myPres = 0;
    private int myEval = 0;

    @Override
    public void setPresentation(@Nullable final Icon icon,
                                @Nullable final String type,
                                @NotNull final String value,
                                final boolean hasChildren) {
      myPres++;

      assertEquals(AllIcons.Debugger.Value, icon);
      assertEquals("tp", type);
      assertEquals("m l t", value);
      assertFalse(hasChildren);
    }

    @Override
    public void setFullValueEvaluator(@NotNull final XFullValueEvaluator fullValueEvaluator) {
      myEval++;

      final MockXFullValueEvaluationCallback callback = new MockXFullValueEvaluationCallback(" m  l   t    \nvl");
      fullValueEvaluator.startEvaluation(callback);

      assertEquals(1, callback.myCounter);
    }

    public boolean check() {
      return myEval == 1 && myPres == 1;
    }
  }

  private static class OneLineNode extends IllegalXValueNode {

    private int myPres = 0;

    @Override
    public void setPresentation(@Nullable final Icon icon, @NotNull final XValuePresentation presentation, final boolean hasChildren) {
      myPres++;

      assertEquals(AllIcons.Debugger.Value, icon);
      assertFalse(hasChildren);

      final MockXValueTextRenderer renderer = new MockXValueTextRenderer("vl");
      presentation.renderValue(renderer);

      assertEquals(1, renderer.myCounter);
    }

    public boolean check() {
      return myPres == 1;
    }
  }

  private static class MultilineNode extends IllegalXValueNode {

    private int myPres = 0;
    private int myEval = 0;

    @Override
    public void setPresentation(@Nullable final Icon icon, @NotNull final XValuePresentation presentation, final boolean hasChildren) {
      myPres++;

      assertEquals(AllIcons.Debugger.Value, icon);
      assertFalse(hasChildren);

      final MockXValueTextRenderer renderer = new MockXValueTextRenderer("m l t");
      presentation.renderValue(renderer);

      assertEquals(1, renderer.myCounter);
    }

    @Override
    public void setFullValueEvaluator(@NotNull final XFullValueEvaluator fullValueEvaluator) {
      myEval++;

      final MockXFullValueEvaluationCallback callback = new MockXFullValueEvaluationCallback(" m  l   t    \nvl");
      fullValueEvaluator.startEvaluation(callback);

      assertEquals(1, callback.myCounter);
    }

    public boolean check() {
      return myEval == 1 && myPres == 1;
    }
  }

  private static class MockXFullValueEvaluationCallback extends IllegalXFullValueEvaluationCallback {

    @NotNull
    private final String myExpected;

    private int myCounter = 0;

    public MockXFullValueEvaluationCallback(@NotNull final String expected) {
      myExpected = expected;
    }

    @Override
    public void evaluated(@NotNull final String fullValue) {
      myCounter++;

      assertEquals(myExpected, fullValue);
    }
  }

  private static class MockXValueTextRenderer extends IllegalXValueTextRenderer {

    @NotNull
    private final String myExpected;

    private int myCounter = 0;

    public MockXValueTextRenderer(@NotNull final String expected) {
      myExpected = expected;
    }

    @Override
    public void renderValue(@NotNull final String value) {
      myCounter++;

      assertEquals(myExpected, value);
    }
  }
}