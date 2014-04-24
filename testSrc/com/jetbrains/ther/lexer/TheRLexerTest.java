package com.jetbrains.ther.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.testFramework.PlatformLiteFixture;

public class TheRLexerTest extends PlatformLiteFixture {

  public void testLogicTrue() {
    doTest("TRUE", "TheR:TRUE_KEYWORD");
  }

  public void testLogicFalse() {
    doTest("FALSE", "TheR:FALSE_KEYWORD");
  }

  public void testNumeric1() {
    doTest("1", "TheR:NUMERIC_LITERAL");
  }

  public void testNumeric10() {
    doTest("10", "TheR:NUMERIC_LITERAL");
  }

  public void testNumericFloat() {
    doTest("0.1", "TheR:NUMERIC_LITERAL");
  }

  public void testNumericFloat2() {
    doTest(".2", "TheR:NUMERIC_LITERAL");
  }

  public void testNumericExponent() {
    doTest("1e-7", "TheR:NUMERIC_LITERAL");
  }

  public void testNumericFloatExponent() {
    doTest("1.2e+7", "TheR:NUMERIC_LITERAL");
  }

  public void testNumericHexExponent() {
    doTest("0x1.1p-2", "TheR:NUMERIC_LITERAL");
  }

  public void testNumericBinaryExponent() {
    doTest("0x123p456", "TheR:NUMERIC_LITERAL");
  }

  public void testNumericHex() {
    doTest("0x1", "TheR:NUMERIC_LITERAL");
  }

  public void testInteger1() {
    doTest("1L", "TheR:INTEGER_LITERAL");
  }

  public void testIntegerHex() {
    doTest("0x10L", "TheR:INTEGER_LITERAL");
  }

  public void testIntegerLong() {
    doTest("1000000L", "TheR:INTEGER_LITERAL");
  }

  public void testIntegerExponent() {
    doTest("1e6L", "TheR:INTEGER_LITERAL");
  }

  public void testNumericWithWarn() {         // TODO: inspection. Actually, it's numeric one
    doTest("1.1L", "TheR:INTEGER_LITERAL");
  }

  public void testNumericWithWarnExp() {      // TODO: inspection. Actually, it's numeric one
    doTest("1e-3L", "TheR:INTEGER_LITERAL");
  }

  public void testSyntaxError() {
    doTest("12iL", "TheR:COMPLEX_LITERAL", "TheR:IDENTIFIER");
  }

  public void testUnnecessaryDecimalPoint() {  // TODO: inspection. Unnecessary Decimal Point warning runtime
    doTest("1.L", "TheR:INTEGER_LITERAL");
  }

  public void testComplex() {
    doTest("1i", "TheR:COMPLEX_LITERAL");
  }

  public void testHexLong() {
    doTest("0xFL", "TheR:INTEGER_LITERAL");
  }



  private static void doTest(String text, String... expectedTokens) {
    doLexerTest(text, new TheRLexer(), expectedTokens);
  }

  public static void doLexerTest(String text, Lexer lexer, String... expectedTokens) {
    doLexerTest(text, lexer, false, expectedTokens);
  }

  public static void doLexerTest(String text,
                                 Lexer lexer,
                                 boolean checkTokenText,
                                 String... expectedTokens) {
    lexer.start(text);
    int idx = 0;
    int tokenPos = 0;
    while (lexer.getTokenType() != null) {
      if (idx >= expectedTokens.length) {
        StringBuilder remainingTokens = new StringBuilder("\"" + lexer.getTokenType().toString() + "\"");
        lexer.advance();
        while (lexer.getTokenType() != null) {
          remainingTokens.append(",");
          remainingTokens.append(" \"").append(checkTokenText ? lexer.getTokenText() : lexer.getTokenType().toString()).append("\"");
          lexer.advance();
        }
        fail("Too many tokens. Following tokens: " + remainingTokens.toString());
      }
      assertEquals("Token offset mismatch at position " + idx, tokenPos, lexer.getTokenStart());
      String tokenName = checkTokenText ? lexer.getTokenText() : lexer.getTokenType().toString();
      assertEquals("Token mismatch at position " + idx, expectedTokens[idx], tokenName);
      idx++;
      tokenPos = lexer.getTokenEnd();
      lexer.advance();
    }

    if (idx < expectedTokens.length) fail("Not enough tokens");
  }

}
