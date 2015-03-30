package com.jetbrains.ther.debugger;

import java.io.IOException;

public class TheRDebuggerTest03 extends TheRDebuggerTestCase {

  public TheRDebuggerTest03() throws IOException, InterruptedException {
    super("03");
  }

  public void test03() throws IOException, InterruptedException {
    myDebugger.executeInstruction();

    checkSize(1);
    checkX();

    myDebugger.executeInstruction();

    checkSize(1);
    checkX();

    myDebugger.executeInstruction();

    checkSize(1);
    checkX();

    myDebugger.executeInstruction();

    checkSize(1);
    checkX();
  }

  private void checkX() {
    checkVariable("x", "[1]  1  2  3  4  5  6  7  8  9 10", "[1] \"integer\"");
  }
}
