package com.jetbrains.ther.xdebugger.stack;

import com.intellij.ui.SimpleTextAttributes;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XDebuggerTreeNodeHyperlink;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.data.TheRVar;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.frame.TheRStackFrame;
import com.jetbrains.ther.debugger.frame.TheRVarsLoader;
import com.jetbrains.ther.debugger.mock.IllegalTheRDebuggerEvaluator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
      null
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
      null
    );

    frame.computeChildren(node);

    //noinspection StatementWithEmptyBody
    while (loader.myCounter == 0 || node.myCounter == 0) {
    }

    assertEquals(1, loader.myCounter);
    assertEquals(1, node.myCounter);
  }

  private static class OrdinaryTheRVarsLoader implements TheRVarsLoader {

    private int myCounter = 0;

    @NotNull
    @Override
    public List<TheRVar> load() throws TheRDebuggerException {
      myCounter++;

      return Arrays.asList(
        new TheRVar("n1", "t1", "v1"),
        new TheRVar("n2", "t2", "v2")
      );
    }
  }

  private static class IllegalXCompositeNode implements XCompositeNode {

    @Override
    public void addChildren(@NotNull final XValueChildrenList children, final boolean last) {
      throw new IllegalStateException("AddChildren shouldn't be called");
    }

    @Override
    public void tooManyChildren(final int remaining) {
      throw new IllegalStateException("TooManyChildren shouldn't be called");
    }

    @Override
    public void setAlreadySorted(final boolean alreadySorted) {
      throw new IllegalStateException("SetAlreadySorted shouldn't be called");
    }

    @Override
    public void setErrorMessage(@NotNull final String errorMessage) {
      throw new IllegalStateException("SetErrorMessage shouldn't be called");
    }

    @Override
    public void setErrorMessage(@NotNull final String errorMessage, final XDebuggerTreeNodeHyperlink link) {
      throw new IllegalStateException("SetErrorMessage shouldn't be called");
    }

    @Override
    public void setMessage(@NotNull final String message,
                           @Nullable final Icon icon,
                           @NotNull final SimpleTextAttributes attributes,
                           @Nullable final XDebuggerTreeNodeHyperlink link) {
      throw new IllegalStateException("SetMessage shouldn't be called");
    }

    @Override
    public boolean isObsolete() {
      throw new IllegalStateException("IsObsolete shouldn't be called");
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
}