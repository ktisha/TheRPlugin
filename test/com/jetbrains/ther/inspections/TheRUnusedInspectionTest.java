package com.jetbrains.ther.inspections;

import com.jetbrains.ther.TheRTestCase;
import org.jetbrains.annotations.NotNull;

public class TheRUnusedInspectionTest extends TheRTestCase {

  @Override
  protected String getTestDataPath() {
    return super.getTestDataPath() + "/inspections/unusedInspection";
  }

  public void test() {
    doTest("main.R");
  }

  private void doTest(@NotNull String filename) {
    myFixture.configureByFile(filename);
    myFixture.enableInspections(TheRUnusedInspection.class);
    myFixture.testHighlighting(true, false, false, filename);
  }
}
