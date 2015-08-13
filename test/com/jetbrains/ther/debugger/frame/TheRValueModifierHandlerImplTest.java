package com.jetbrains.ther.debugger.frame;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TheRValueModifierHandlerImplTest {

  @Test
  public void ordinary() {
    final TheRValueModifierHandlerImpl handler = new TheRValueModifierHandlerImpl();

    handler.setMaxFrameNumber(2);

    assertTrue(handler.isModificationAvailable(2));
    assertFalse(handler.isModificationAvailable(1));
  }
}