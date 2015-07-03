package com.jetbrains.ther.debugger.utils;

import org.junit.Test;

import static com.jetbrains.ther.debugger.utils.TheRDebuggerUtils.isCommentOrSpaces;
import static org.junit.Assert.*;

public class TheRDebuggerUtilsTest {

  @Test
  public void varsLoading() {
    fail("Not implemented");
  }

  @Test
  public void commandExecuting() {
    fail("Not implemented");
  }

  @Test
  public void commentChecking() {
    assertTrue(isCommentOrSpaces(" # abc "));
  }

  @Test
  public void spacesChecking() {
    assertTrue(isCommentOrSpaces("  "));
  }

  @Test
  public void nullChecking() {
    assertFalse(isCommentOrSpaces(null));
  }

  @Test
  public void ordinaryChecking() {
    assertFalse(isCommentOrSpaces(" abc "));
  }
}