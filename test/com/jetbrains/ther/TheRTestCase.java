package com.jetbrains.ther;

import com.intellij.testFramework.PlatformTestCase;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;

import java.io.File;

public class TheRTestCase extends UsefulTestCase {
  public static final String TEST_DATA_PATH = new File("testData").getAbsolutePath().replace(File.pathSeparatorChar, '/');
  protected CodeInsightTestFixture myFixture;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    initPlatformPrefix();
    final TestFixtureBuilder<IdeaProjectTestFixture> projectBuilder = IdeaTestFixtureFactory.getFixtureFactory().
      createFixtureBuilder(getName());
    myFixture = IdeaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(projectBuilder.getFixture());
    myFixture.setUp();
    myFixture.setTestDataPath(getTestDataPath());
  }

  protected String getTestDataPath() {
    return TEST_DATA_PATH;
  }

  private static void initPlatformPrefix() {
    PlatformTestCase.autodetectPlatformPrefix();
  }

  @Override
  public void tearDown() throws Exception {
    myFixture.tearDown();
    super.tearDown();
  }
}

