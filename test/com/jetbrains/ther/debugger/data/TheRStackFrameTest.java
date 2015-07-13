package com.jetbrains.ther.debugger.data;

import com.jetbrains.ther.debugger.mock.IllegalTheRDebuggerEvaluator;
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
      new IllegalTheRDebuggerEvaluator()
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
      new IllegalTheRDebuggerEvaluator()
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
      new IllegalTheRDebuggerEvaluator()
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
}