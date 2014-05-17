package com.jetbrains.ther.parser;

import com.intellij.testFramework.ParsingTestCase;
import com.intellij.testFramework.TestDataPath;
import com.jetbrains.ther.parsing.TheRParserDefinition;

@TestDataPath("/testData/psi/")
public class TheRParsingTest extends ParsingTestCase {
  private static final String DATA_PATH = System.getProperty("user.dir") + "/testData/psi/";

  public TheRParsingTest() {
    super("", "r", new TheRParserDefinition());
  }

  @Override
  protected String getTestDataPath() {
    return DATA_PATH;
  }

  public void testSlice() {
    doTest();
  }

  public void testAssignment() {
    doTest();
  }

  public void testBinary() {
    doTest();
  }

  public void testBinarySlice() {
    doTest();
  }

  public void testFunctionCall() {
    doTest();
  }

  public void testFunctionDefinition() {
    doTest();
  }

  public void testIfStatement() {
    doTest();
  }

  public void testForStatement() {
    doTest();
  }

  public void testRepeatStatement() {
    doTest();
  }

  public void testRepeatBlockStatement() {
    doTest();
  }

  public void testWhileStatement() {
    doTest();
  }

  public void testHelpStatement() {
    doTest();
  }

  public void testSubscription() {
    doTest();
  }

  public void testStatementBreak() {
    doTest();
  }

  public void testStatementBreakAssignment() {
    doTest();
  }

  public void testHelpOnKeyword() {
    doTest();
  }

  public void testBreak() {
    doTest();
  }

  public void testOperator() {
    doTest();
  }

  public void testParenthesized() {
    doTest();
  }

  public void testSemicolon() {
    doTest();
  }

  public void testPrecedence() {
    doTest();
  }

  public void testStringKeywordArg() {
    doTest();
  }

  public void testKeywordArg() {
    doTest();
  }

  public void testBlockAsArgument() {
    doTest();
  }

  public void testFormulae() {
    doTest();
  }

  public void testFunctionBodyAsExpression() {
    doTest();
  }

  public void testFunctionAsCallArgument() {
    doTest();
  }

  public void testIfShortForm() {
    doTest();
  }

  public void testAssignmentInSubscription() {
    doTest();
  }

  public void testEmptyKeywordArgument() {
    doTest();
  }

  public void testIfInKeywordArgument() {
    doTest();
  }

  public void testIfStatementAsArgument() {
    doTest();
  }

  public void doTest() {
    doTest(true);
  }
}
