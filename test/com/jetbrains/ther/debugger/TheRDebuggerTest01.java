package com.jetbrains.ther.debugger;

import java.io.IOException;

public class TheRDebuggerTest01 extends TheRDebuggerTestCase {

  public TheRDebuggerTest01() throws IOException, InterruptedException {
    super("01");
  }

  public void test01() throws IOException, InterruptedException {
    myDebugger.executeInstruction();

    checkSize(1);
    checkX();

    myDebugger.executeInstruction();

    checkSize(2);
    checkX();
    checkQ();

    myDebugger.executeInstruction();

    checkSize(3);
    checkX();
    checkQ();
    checkMean();

    myDebugger.executeInstruction();

    checkSize(4);
    checkX();
    checkQ();
    checkMean();
    checkSd();
  }

  private void checkX() {
    checkVariable("x", "[1] 1 2 4", "[1] \"double\"");
  }

  private void checkQ() {
    checkVariable("q", "[1] 1 2 4 1 2 4 8", "[1] \"double\"");
  }

  private void checkMean() {
    checkVariable("mean", "[1] 2.333333", "[1] \"double\"");
  }

  private void checkSd() {
    checkVariable("sd", "[1] 1.527525", "[1] \"double\"");
  }
}