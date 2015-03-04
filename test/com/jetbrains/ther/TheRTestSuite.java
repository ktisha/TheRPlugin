package com.jetbrains.ther;

import com.jetbrains.ther.lexer.TheRHighlightingLexerTest;
import com.jetbrains.ther.parser.TheRParsingTest;
import com.jetbrains.ther.typing.SimpleTypingTest;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TheRTestSuite extends TestCase {
  public static Test suite() {
    TestSuite testSuite = new TestSuite("AllTest");
    testSuite.addTestSuite(SimpleTypingTest.class);
    testSuite.addTestSuite(TheRHighlightingLexerTest.class);
    testSuite.addTestSuite(TheRParsingTest.class);
    return testSuite;
  }

}
