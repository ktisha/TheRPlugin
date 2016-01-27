package com.jetbrains.ther.run.debug.stack;

import com.jetbrains.ther.debugger.frame.TheRVar;
import com.jetbrains.ther.debugger.mock.IllegalTheRValueModifier;
import com.jetbrains.ther.run.debug.mock.MockXValueNode;
import com.jetbrains.ther.run.debug.mock.MockXVarNode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TheRXPresentationUtilsTest {

  @Test
  public void oneLineVar() {
    final MockXVarNode node = new MockXVarNode("tp", "vl", null);

    TheRXPresentationUtils.computePresentation(
      new TheRVar("nm", "tp", "vl", new IllegalTheRValueModifier()),
      node
    );

    assertEquals(1, node.getPres());
    assertEquals(0, node.getEval());
  }

  @Test
  public void multilineVar() {
    final MockXVarNode node = new MockXVarNode("tp", "m l t", " m  l   t    \nvl");

    TheRXPresentationUtils.computePresentation(
      new TheRVar("nm", "tp", " m  l   t    \nvl", new IllegalTheRValueModifier()),
      node
    );

    assertEquals(1, node.getPres());
    assertEquals(1, node.getEval());
  }

  @Test
  public void oneLineValue() {
    final MockXValueNode node = new MockXValueNode("vl", null);

    TheRXPresentationUtils.computePresentation(
      "vl",
      node
    );

    assertEquals(1, node.getPres());
    assertEquals(0, node.getEval());
  }

  @Test
  public void multilineValue() {
    final MockXValueNode node = new MockXValueNode("m l t", " m  l   t    \nvl");

    TheRXPresentationUtils.computePresentation(
      " m  l   t    \nvl",
      node
    );

    assertEquals(1, node.getPres());
    assertEquals(1, node.getEval());
  }
}