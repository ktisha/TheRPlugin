package com.jetbrains.ther.xdebugger.stack;

import com.intellij.icons.AllIcons;
import com.intellij.ui.ColoredTextContainer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.frame.TheRStackFrame;
import com.jetbrains.ther.debugger.frame.TheRVar;
import com.jetbrains.ther.debugger.frame.TheRVarsLoader;
import com.jetbrains.ther.debugger.mock.IllegalTheRDebuggerEvaluator;
import com.jetbrains.ther.debugger.mock.IllegalTheRValueModifier;
import com.jetbrains.ther.debugger.mock.IllegalTheRVarsLoader;
import com.jetbrains.ther.xdebugger.mock.ExecutorServices;
import com.jetbrains.ther.xdebugger.mock.IllegalXCompositeNode;
import com.jetbrains.ther.xdebugger.mock.MockXSourcePosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class TheRXStackFrameTest {

  @Test
  public void ordinaryComputing() {
    final OrdinaryTheRVarsLoader loader = new OrdinaryTheRVarsLoader();
    final OrdinaryXCompositeNode node = new OrdinaryXCompositeNode();

    final TheRXStackFrame frame = new TheRXStackFrame(
      new TheRStackFrame(
        new TheRLocation("abc", 2),
        loader,
        new IllegalTheRDebuggerEvaluator()
      ),
      null,
      ExecutorServices.SINGLE_EXECUTOR
    );

    frame.computeChildren(node);

    //noinspection StatementWithEmptyBody
    while (loader.myCounter == 0 || node.myCounter == 0) {
    }

    assertEquals(1, loader.myCounter);
    assertEquals(1, node.myCounter);
  }

  @Test
  public void errorComputing() {
    final ErrorTheRVarsLoader loader = new ErrorTheRVarsLoader();
    final ErrorXCompositeNode node = new ErrorXCompositeNode();

    final TheRXStackFrame frame = new TheRXStackFrame(
      new TheRStackFrame(
        new TheRLocation("def", 4),
        loader,
        new IllegalTheRDebuggerEvaluator()
      ),
      null,
      ExecutorServices.SINGLE_EXECUTOR
    );

    frame.computeChildren(node);

    //noinspection StatementWithEmptyBody
    while (loader.myCounter == 0 || node.myCounter == 0) {
    }

    assertEquals(1, loader.myCounter);
    assertEquals(1, node.myCounter);
  }

  @Test
  public void nullPresentation() {
    final TheRXStackFrame frame = new TheRXStackFrame(
      new TheRStackFrame(
        new TheRLocation("abc", 2),
        new IllegalTheRVarsLoader(),
        new IllegalTheRDebuggerEvaluator()
      ),
      null,
      ExecutorServices.ILLEGAL_EXECUTOR
    );

    final MockColoredTextContainer container = new MockColoredTextContainer();

    frame.customizePresentation(container);

    assertEquals(Collections.singletonList("<invalid frame>"), container.myFragments);
    assertEquals(Collections.singletonList(SimpleTextAttributes.ERROR_ATTRIBUTES), container.myAttributes);
    assertEquals(null, container.myIcon);
  }

  @Test
  public void mainPresentation() {
    final TheRXStackFrame frame = new TheRXStackFrame(
      new TheRStackFrame(
        new TheRLocation(TheRDebugConstants.MAIN_FUNCTION_NAME, 2),
        new IllegalTheRVarsLoader(),
        new IllegalTheRDebuggerEvaluator()
      ),
      new MockXSourcePosition("script.r", 2),
      ExecutorServices.ILLEGAL_EXECUTOR
    );

    final MockColoredTextContainer container = new MockColoredTextContainer();

    frame.customizePresentation(container);

    assertEquals(Arrays.asList("script.r", ":3"), container.myFragments);
    assertEquals(Arrays.asList(SimpleTextAttributes.REGULAR_ATTRIBUTES, SimpleTextAttributes.REGULAR_ATTRIBUTES), container.myAttributes);
    assertEquals(AllIcons.Debugger.StackFrame, container.myIcon);
  }

  @Test
  public void ordinaryPresentation() {
    final TheRXStackFrame frame = new TheRXStackFrame(
      new TheRStackFrame(
        new TheRLocation("abc", 2),
        new IllegalTheRVarsLoader(),
        new IllegalTheRDebuggerEvaluator()
      ),
      new MockXSourcePosition("script.r", 9),
      ExecutorServices.ILLEGAL_EXECUTOR
    );

    final MockColoredTextContainer container = new MockColoredTextContainer();

    frame.customizePresentation(container);

    assertEquals(Collections.singletonList("abc, script.r:10"), container.myFragments);
    assertEquals(Collections.singletonList(SimpleTextAttributes.REGULAR_ATTRIBUTES), container.myAttributes);
    assertEquals(AllIcons.Debugger.StackFrame, container.myIcon);
  }

  private static class OrdinaryTheRVarsLoader implements TheRVarsLoader {

    private int myCounter = 0;

    @NotNull
    @Override
    public List<TheRVar> load() throws TheRDebuggerException {
      myCounter++;

      return Arrays.asList(
        new TheRVar("n1", "t1", "v1", new MockTheRValueModifier(true)),
        new TheRVar("n2", "t2", "v2", new MockTheRValueModifier(false))
      );
    }
  }

  private static class OrdinaryXCompositeNode extends IllegalXCompositeNode {

    private int myCounter = 0;

    @Override
    public void addChildren(@NotNull final XValueChildrenList children, final boolean last) {
      myCounter++;

      assertEquals(2, children.size());
      assertTrue(last);

      assertEquals("n1", children.getName(0));
      assertEquals("n2", children.getName(1));

      assertNotNull(children.getValue(0).getModifier());
      assertNull(children.getValue(1).getModifier());
    }
  }

  private static class ErrorTheRVarsLoader implements TheRVarsLoader {

    private int myCounter = 0;

    @NotNull
    @Override
    public List<TheRVar> load() throws TheRDebuggerException {
      myCounter++;

      throw new TheRDebuggerException("");
    }
  }

  private static class ErrorXCompositeNode extends IllegalXCompositeNode {

    private int myCounter = 0;

    @Override
    public void setErrorMessage(@NotNull final String errorMessage) {
      myCounter++;
    }
  }

  private static class MockColoredTextContainer implements ColoredTextContainer {

    @NotNull
    private final List<String> myFragments = new ArrayList<String>();

    @NotNull
    private final List<SimpleTextAttributes> myAttributes = new ArrayList<SimpleTextAttributes>();

    @Nullable
    private Icon myIcon = null;

    @Override
    public void append(@NotNull final String fragment, @NotNull final SimpleTextAttributes attributes) {
      myFragments.add(fragment);
      myAttributes.add(attributes);
    }

    @Override
    public void append(@NotNull final String fragment, @NotNull final SimpleTextAttributes attributes, final Object tag) {
      throw new IllegalStateException("Append shouldn't be called");
    }

    @Override
    public void setIcon(@Nullable final Icon icon) {
      myIcon = icon;
    }

    @Override
    public void setToolTipText(@Nullable final String text) {
      throw new IllegalStateException("SetToolTipText shouldn't be called");
    }
  }

  private static class MockTheRValueModifier extends IllegalTheRValueModifier {

    private final boolean myIsEnabled;

    public MockTheRValueModifier(final boolean isEnabled) {
      myIsEnabled = isEnabled;
    }

    @Override
    public boolean isEnabled() {
      return myIsEnabled;
    }
  }
}