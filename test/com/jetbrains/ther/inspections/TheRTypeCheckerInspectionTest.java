package com.jetbrains.ther.inspections;

import org.jetbrains.annotations.NotNull;

public class TheRTypeCheckerInspectionTest extends TheRInspectionTest {

  public void testNoWarnings() {
    doTest("test.R");
  }

  public void testWrongTypeParameter() {
    doTest("test1.R");
  }

  public void testArgumentsMatching() {
    doTest("test2.R");
  }

  public void testTripleDot() {
    doTest("test3.R");
  }

  public void testRule() {
    doTest("test4.R");
  }

  public void testGuessReturnFromBody() {
    doTest("test5.r");
  }

  @Override
  protected String getTestDataPath() {
    return super.getTestDataPath() + "/typing/";
  }

  @NotNull
  @Override
  Class<? extends TheRLocalInspection> getInspection() {
    return TheRTypeCheckerInspection.class;
  }
}
