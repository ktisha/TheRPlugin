package com.jetbrains.ther.inspections;

import com.jetbrains.ther.TheRTestCase;
import org.jetbrains.annotations.NotNull;

public abstract class TheRInspectionTest extends TheRTestCase{

  protected void doTest(@NotNull String filename) {
    myFixture.configureByFile(filename);
    myFixture.enableInspections(getInspection());
    myFixture.testHighlighting(true, false, false, filename);
  }

  @NotNull
  abstract Class<? extends  TheRLocalInspection> getInspection();
}
