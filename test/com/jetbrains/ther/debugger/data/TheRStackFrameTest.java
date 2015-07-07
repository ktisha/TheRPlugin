package com.jetbrains.ther.debugger.data;

import com.jetbrains.ther.debugger.TheRDebuggerEvaluator;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TheRStackFrameTest {

  @Test(expected = UnsupportedOperationException.class)
  public void modifyInner() {
    final List<TheRVar> vars = getMutableVars();

    final TheRLocation location = new TheRLocation(
      new TheRFunction(Collections.singletonList("abc")), 1
    );

    final TheRStackFrame stackFrame = new TheRStackFrame(
      location,
      vars,
      new MockTheRDebuggerEvaluator()
    );

    stackFrame.getVars().add(new TheRVar("name3", "type3", "value3"));
  }

  @Test
  public void modifyOuter() {
    final List<TheRVar> vars = getMutableVars();
    final List<TheRVar> varsCopy = new ArrayList<TheRVar>(vars);

    final TheRLocation location = new TheRLocation(
      new TheRFunction(Collections.singletonList("abc")), 1
    );

    final TheRStackFrame stackFrame = new TheRStackFrame(
      location,
      vars,
      new MockTheRDebuggerEvaluator()
    );

    vars.add(new TheRVar("name3", "type3", "value3"));

    assertEquals(varsCopy, stackFrame.getVars());
  }

  @Test
  public void ordinary() {
    final List<TheRVar> vars = getMutableVars();
    final List<TheRVar> varsCopy = new ArrayList<TheRVar>(vars);

    final TheRLocation location = new TheRLocation(
      new TheRFunction(Collections.singletonList("abc")), 1
    );

    final TheRLocation locationCopy = new TheRLocation(
      new TheRFunction(Collections.singletonList("abc")), 1
    );

    final TheRStackFrame stackFrame = new TheRStackFrame(
      location,
      vars,
      new MockTheRDebuggerEvaluator()
    );

    assertEquals(varsCopy, stackFrame.getVars());
    assertEquals(locationCopy, stackFrame.getLocation());
  }

  @NotNull
  private List<TheRVar> getMutableVars() {
    final List<TheRVar> result = new ArrayList<TheRVar>();

    result.add(new TheRVar("name1", "type1", "value1"));
    result.add(new TheRVar("name2", "type2", "value2"));

    return result;
  }

  private static class MockTheRDebuggerEvaluator implements TheRDebuggerEvaluator {

    @Override
    public void evalCondition(@NotNull final String condition, @NotNull final ConditionReceiver receiver) {
      throw new IllegalStateException("EvalCondition shouldn't be called");
    }

    @Override
    public void evalExpression(@NotNull final String expression, @NotNull final ExpressionReceiver receiver) {
      throw new IllegalStateException("EvalExpression shouldn't be called");
    }
  }
}