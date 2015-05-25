package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRProcessResponseType;
import org.junit.Test;

import static com.jetbrains.ther.debugger.TheRProcessResponseTypeCalculator.calculate;
import static com.jetbrains.ther.debugger.TheRProcessResponseTypeCalculator.isComplete;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TheRProcessResponseTypeCalculatorTest {

  @Test
  public void completePlus() {
    assertTrue(isComplete("x <- function() {\n+ "));
  }

  @Test
  public void completeBrowser() {
    assertTrue(isComplete("ls()\n[1] \"x\"\nBrowse[1]> "));
  }

  @Test
  public void calculatePlus() {
    assertEquals(TheRProcessResponseType.PLUS, calculate("+ "));
  }

  @Test
  public void calculateJustBrowse() {
    assertEquals(TheRProcessResponseType.JUST_BROWSE, calculate("Browse[1]> "));
  }

  @Test
  public void calculateDebugging() {
    assertEquals(
      TheRProcessResponseType.DEBUGGING,
      calculate(
        "debugging in: x()\n" +
        "debug: {\n" +
        "    on.exit(.doTrace(intellij_ther_x_exit(), \"on exit\"))\n" +
        "    {\n" +
        "        .doTrace(intellij_ther_x_enter(), \"on entry\")\n" +
        "        {\n" +
        "            print(\"x\")\n" +
        "        }\n" +
        "    }\n" +
        "}\n" +
        "Browse[3]> "
      )
    );
  }

  @Test
  public void calculateStartTrace() {
    assertEquals(
      TheRProcessResponseType.START_TRACE,
      calculate(
        "Tracing x() on entry \n" +
        "[1] \"enter x\"\n" +
        "debug: {\n" +
        "    print(\"x\")\n" +
        "}\n" +
        "Browse[3]> "
      )
    );
  }

  @Test
  public void calculateContinueTrace() {
    assertEquals(
      TheRProcessResponseType.CONTINUE_TRACE,
      calculate(
        "Tracing FUN(c(-1, 0, 1)[[1L]], ...) on exit \n" +
        "[1] \"exit x\"\n" +
        "exiting from: FUN(c(-1, 0, 1)[[1L]], ...)\n" +
        "debugging in: FUN(c(-1, 0, 1)[[2L]], ...)\n" +
        "debug: {\n" +
        "    on.exit(.doTrace(intellij_ther_x_exit(), \"on exit\"))\n" +
        "    {\n" +
        "        .doTrace(intellij_ther_x_enter(), \"on entry\")\n" +
        "        {\n" +
        "            print(\"x\")\n" +
        "        }\n" +
        "    }\n" +
        "}\n" +
        "Browse[3]> "
      )
    );
  }

  @Test
  public void calculateEndTrace() {
    assertEquals(
      TheRProcessResponseType.END_TRACE,
      calculate(
        "Tracing FUN(c(-1, 0, 1)[[3L]], ...) on exit \n" +
        "[1] \"exit x\"\n" +
        "exiting from: FUN(c(-1, 0, 1)[[3L]], ...)\n" +
        "Browse[1]> "
      )
    );
  }

  @Test
  public void calculateResponseAndBrowse() {
    assertEquals(
      TheRProcessResponseType.RESPONSE_AND_BROWSE,
      calculate(
        "[1] \"x\"\n" +
        "Browse[1]> "
      )
    );
  }
}
