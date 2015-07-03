package com.jetbrains.ther.debugger.data;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TheRFunctionTest {

  @Test(expected = IllegalArgumentException.class)
  public void emptyList() {
    new TheRFunction(Collections.<String>emptyList());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void modifyInner() {
    final List<String> names = getMutableNames();

    final TheRFunction function = new TheRFunction(names);

    function.getDefinition().add("ghi");
  }

  @Test
  public void modifyOuter() {
    final List<String> names = getMutableNames();
    final List<String> namesCopy = new ArrayList<String>(names);

    final TheRFunction function = new TheRFunction(names);
    names.add("ghi");

    assertEquals(namesCopy, function.getDefinition());
  }

  @Test
  public void ordinary() {
    final List<String> names = getMutableNames();
    final List<String> namesCopy = new ArrayList<String>(names);

    final TheRFunction function = new TheRFunction(names);

    assertEquals("def", function.getName());
    assertEquals(namesCopy, function.getDefinition());
  }

  @NotNull
  private List<String> getMutableNames() {
    final List<String> result = new ArrayList<String>();

    result.add("abc");
    result.add("def");

    return result;
  }
}