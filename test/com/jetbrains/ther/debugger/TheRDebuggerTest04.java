package com.jetbrains.ther.debugger;

import java.io.IOException;

public class TheRDebuggerTest04 extends TheRDebuggerTestCase {

  public TheRDebuggerTest04() throws IOException, InterruptedException {
    super("04");
  }

  public void test04() throws IOException, InterruptedException {
    myDebugger.executeInstruction();

    checkSize(1);
    checkPureY();

    myDebugger.executeInstruction();
    checkSize(1);
    checkColnamesY();

    myDebugger.executeInstruction();
    checkSize(1);
    checkRownamesY();
  }

  private void checkPureY() {
    checkVariable(
      "y",
      "     [,1] [,2]\n" +
      "[1,]    1    3\n" +
      "[2,]    2    4",
      "[1] \"double\""
    );
  }

  private void checkColnamesY() {
    checkVariable(
      "y",
      "     a b\n" +
      "[1,] 1 3\n" +
      "[2,] 2 4",
      "[1] \"double\""
    );
  }

  private void checkRownamesY() {
    checkVariable(
      "y",
      "  a b\n" +
      "c 1 3\n" +
      "d 2 4",
      "[1] \"double\""
    );
  }
}
