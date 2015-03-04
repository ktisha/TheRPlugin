package com.jetbrains.ther.typing;

import com.jetbrains.ther.TheRTestCase;
import com.jetbrains.ther.inspections.TheRTypeCheckerInspection;
import org.jetbrains.annotations.NotNull;

public class SimpleTypingTest extends TheRTestCase {
  public void testSimple() {
    doTest("test.R");
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
