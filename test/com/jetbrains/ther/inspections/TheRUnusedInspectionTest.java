package com.jetbrains.ther.inspections;

import org.jetbrains.annotations.NotNull;

public class TheRUnusedInspectionTest extends TheRInspectionTest {

  @Override
  protected String getTestDataPath() {
    return super.getTestDataPath() + "/inspections/unusedInspection";
  }

  public void test() {
    doTest("main.R");
  }

  @NotNull
  @Override
  Class<? extends TheRLocalInspection> getInspection() {
    return TheRUnusedInspection.class;
  }
}
