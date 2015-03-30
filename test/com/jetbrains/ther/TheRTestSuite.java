package com.jetbrains.ther;

import com.jetbrains.ther.inspections.TheRTypeCheckerInspectionTest;
import com.jetbrains.ther.inspections.TheRUnresolvedReferenceInspectionTest;
import com.jetbrains.ther.inspections.TheRUnusedInspectionTest;
import com.jetbrains.ther.lexer.TheRHighlightingLexerTest;
import com.jetbrains.ther.parser.TheRParsingTest;
import com.jetbrains.ther.rename.TheRRenameTest;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TheRTestSuite extends TestCase {
  public static Test suite() {
    TestSuite testSuite = new TestSuite("AllTest");
    testSuite.addTestSuite(TheRTypeCheckerInspectionTest.class);
    testSuite.addTestSuite(TheRUnresolvedReferenceInspectionTest.class);
    testSuite.addTestSuite(TheRUnusedInspectionTest.class);
    testSuite.addTestSuite(TheRHighlightingLexerTest.class);
    testSuite.addTestSuite(TheRParsingTest.class);
    testSuite.addTestSuite(TheRRenameTest.class);
    return testSuite;
  }

}
