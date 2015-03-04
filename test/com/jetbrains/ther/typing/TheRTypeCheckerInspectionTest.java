package com.jetbrains.ther.typing;

import com.jetbrains.ther.TheRTestCase;
import com.jetbrains.ther.inspections.TheRTypeCheckerInspection;
import org.jetbrains.annotations.NotNull;

public class TheRTypeCheckerInspectionTest extends TheRTestCase {

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

  private void doTest(@NotNull String filename) {
    myFixture.configureByFile(filename);
    myFixture.enableInspections(TheRTypeCheckerInspection.class);
    myFixture.testHighlighting(true, false, false, filename);
  }

  @Override
  protected String getTestDataPath() {
    return super.getTestDataPath() + "/typing/";
  }
}
