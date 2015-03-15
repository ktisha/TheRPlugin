package com.jetbrains.ther.inspections;

import org.jetbrains.annotations.NotNull;

public class TheRUnresolvedReferenceInspectionTest extends TheRInspectionTest {
  @Override
  protected String getTestDataPath() {
    return super.getTestDataPath() + "/inspections/unresolvedReferenceInspection";
  }

  public void test() {
    doTest("main.R");
  }

  @NotNull
  @Override
  Class<? extends TheRLocalInspection> getInspection() {
    return TheRUnresolvedReferenceInspection.class;
  }
}
