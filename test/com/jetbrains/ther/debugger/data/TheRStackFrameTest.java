package com.jetbrains.ther.debugger.data;

import com.jetbrains.ther.debugger.mock.IllegalTheRDebuggerEvaluator;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TheRStackFrameTest {

  @Test(expected = UnsupportedOperationException.class)
  public void modifyInner() {
    final TheRStackFrame stackFrame = new TheRStackFrame(
      getLocation(),
      getMutableVars(),
      new IllegalTheRDebuggerEvaluator()
    );

    stackFrame.getVars().add(getNewVar());
  }

  @Test
  public void modifyOuter() {
    final List<TheRVar> vars = getMutableVars();
    final List<TheRVar> varsCopy = new ArrayList<TheRVar>(vars);

    final TheRStackFrame stackFrame = new TheRStackFrame(
      getLocation(),
      vars,
      new IllegalTheRDebuggerEvaluator()
    );

    vars.add(getNewVar());

    assertEquals(varsCopy, stackFrame.getVars());
  }

  @Test
  public void ordinary() {
    final List<TheRVar> vars = getMutableVars();
    final List<TheRVar> varsCopy = new ArrayList<TheRVar>(vars);

    final TheRLocation location = getLocation();
    final TheRLocation locationCopy = new TheRLocation(location.getFunctionName(), location.getLine());

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

  @NotNull
  private TheRLocation getLocation() {
    return new TheRLocation("abc", 10);
  }

  @NotNull
  private TheRVar getNewVar() {
    return new TheRVar("name3", "type3", "value3");
  }
}