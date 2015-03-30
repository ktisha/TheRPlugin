package com.jetbrains.ther.debugger;

import java.io.IOException;

public class TheRDebuggerTest02 extends TheRDebuggerTestCase {

  public TheRDebuggerTest02() throws IOException, InterruptedException {
    super("02");
  }

  public void test02() throws IOException, InterruptedException {
    myDebugger.executeInstruction();

    checkSize(1);
    checkOddcount();

    myDebugger.executeInstruction();

    checkSize(2);
    checkOddcount();
    checkA();
  }

  private void checkOddcount() {
    checkVariable(
      "oddcount",
      "function (x) \n" +
      "{\n" +
      "    k <- 0\n" +
      "    for (n in x) {\n" +
      "        if (n%%2 == 1) \n" +
      "            k <- k + 1\n" +
      "    }\n" +
      "    return(k)\n" +
      "}",
      "[1] \"closure\""
    );
  }

  private void checkA() {
    checkVariable("a", "[1] 3", "[1] \"double\"");
  }
}
