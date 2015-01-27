package com.jetbrains.ther.lexer;

import com.intellij.lexer.Lexer;
import junit.framework.TestCase;

public class TheRHighlightingLexerTest extends TestCase {

  public void testLogicTrue() {
    doTest("TRUE", "TheR:TRUE");
  }

  public void testLogicFalse() {
    doTest("FALSE", "TheR:FALSE");
  }

  public void testNumeric1() {
    doTest("1", "TheR:numeric");
  }

  public void testNumeric10() {
    doTest("10", "TheR:numeric");
  }

  public void testNumericFloat() {
    doTest("0.1", "TheR:numeric");
  }

  public void testNumericFloat2() {
    doTest(".2", "TheR:numeric");
  }

  public void testNumericExponent() {
    doTest("1e-7", "TheR:numeric");
  }

  public void testNumericFloatExponent() {
    doTest("1.2e+7", "TheR:numeric");
  }

  public void testNumericHexExponent() {
    doTest("0x1.1p-2", "TheR:numeric");
  }

  public void testNumericBinaryExponent() {
    doTest("0x123p456", "TheR:numeric");
  }

  public void testNumericHex() {
    doTest("0x1", "TheR:numeric");
  }

  public void testInteger1() {
    doTest("1L", "TheR:integer");
  }

  public void testIntegerHex() {
    doTest("0x10L", "TheR:integer");
  }

  public void testIntegerLong() {
    doTest("1000000L", "TheR:integer");
  }

  public void testIntegerExponent() {
    doTest("1e6L", "TheR:integer");
  }

  public void testNumericWithWarn() {         // TODO: inspection. Actually, it's numeric one
    doTest("1.1L", "TheR:integer");
  }

  public void testNumericWithWarnExp() {      // TODO: inspection. Actually, it's numeric one
    doTest("1e-3L", "TheR:integer");
  }

  public void testSyntaxError() {
    doTest("12iL", "TheR:complex", "TheR:identifier");
  }

  public void testUnnecessaryDecimalPoint() {  // TODO: inspection. Unnecessary Decimal Point warning runtime
    doTest("1.L", "TheR:integer");
  }

  public void testComplex() {
    doTest("1i", "TheR:complex");
  }

  public void testFloatComplex() {
    doTest("4.1i", "TheR:complex");
  }

  public void testExponentComplex() {
    doTest("1e-2i", "TheR:complex");
  }

  public void testHexLong() {
    doTest("0xFL", "TheR:integer");
  }

  public void testSingleQuotedString() {
    doTest("'qwerty'", "TheR:string");
  }

  public void testDoubleQuotedString() {
    doTest("\"qwerty\"", "TheR:string");
  }

  public void testEscapeStringDouble() {
    doTest("\"\\\"\"", "TheR:string");
  }

  public void testEscapeStringSingle() {
    doTest("'\\\''", "TheR:string");
  }

  public void testEscapeString() {
    doTest("'\\r\\n\\t\\b\\a\\f\\v'", "TheR:string");
  }

  public void testEscapeOctString() {
    doTest("'\\123'", "TheR:string");
  }

  public void testEscapeHexString() {
    doTest("'\\x1'", "TheR:string");
  }

  public void testEscapeUnicodeString() {
    doTest("'\\u1234'", "TheR:string");
  }

  public void testEscapeBigUnicodeString() {
    doTest("'\\u12345678'", "TheR:string");
  }

  public void testErrorInString() {             //TODO: inspection. string errors
    doTest("'\\0'", "TheR:string");
  }

  public void testIdentifier() {
    doTest("a1", "TheR:identifier");
  }

  public void testIdentifierDot() {
    doTest("a.1", "TheR:identifier");
  }

  public void testIdentifierUnderscore() {
    doTest("a_1", "TheR:identifier");
  }

  public void testIdentifierDotDot() {
    doTest("..", "TheR:identifier");
  }

  public void testIdentifierDotUnderscore() {
    doTest("._", "TheR:identifier");
  }

  public void testIdentifierDotLetter() {
    doTest(".x", "TheR:identifier");
  }

  public void testIdentifierDotDigit() {
    doTest(".1", "TheR:numeric");
  }

  public void testAssignment() {
    doTest("a <- 42\n", "TheR:identifier", "TheR:SPACE", "TheR:<-", "TheR:SPACE", "TheR:numeric", "TheR:nl");
  }

  public void testAssignmentComment() {
    doTest("A <- a * 2  # R is case sensitive\n", "TheR:identifier", "TheR:SPACE", "TheR:<-", "TheR:SPACE", "TheR:identifier", "TheR:SPACE", "TheR:*", "TheR:SPACE", "TheR:numeric", "TheR:SPACE", "TheR:END_OF_LINE_COMMENT", "TheR:nl");
  }

  public void testPrintFunction() {
    doTest("print(a)\n", "TheR:identifier", "TheR:(", "TheR:identifier", "TheR:)", "TheR:nl");
  }

  public void testCat() {
    doTest("cat(A, \"\\n\") # \"84\" is concatenated with \"\\n\"\n", "TheR:identifier", "TheR:(", "TheR:identifier", "TheR:,", "TheR:SPACE", "TheR:string", "TheR:)", "TheR:SPACE", "TheR:END_OF_LINE_COMMENT", "TheR:nl");
  }

  public void testDoubleBrackets() {
    doTest("profile[[pnames[pm]]]", "TheR:identifier", "TheR:[[", "TheR:identifier", "TheR:[", "TheR:identifier", "TheR:]", "TheR:]]");
  }

  public void testDoubleBracketsSeparated() {
    doTest("return(invisible(dll_list[[ seq_along(dll_list)[ind] ]]))", "TheR:identifier", "TheR:(", "TheR:identifier", "TheR:(", "TheR:identifier", "TheR:[[", "TheR:SPACE", "TheR:identifier", "TheR:(", "TheR:identifier", "TheR:)", "TheR:[", "TheR:identifier", "TheR:]", "TheR:SPACE", "TheR:]]", "TheR:)", "TheR:)");
  }

  public void testIf() {
    doTest("if(A>a) # true, 84 > 42\n" +
           "{\n" +
           "  cat(A, \">\", a, \"\\n\")\n" +
           "} ", "TheR:if", "TheR:(", "TheR:identifier", "TheR:>", "TheR:identifier", "TheR:)", "TheR:SPACE", "TheR:END_OF_LINE_COMMENT", "TheR:nl", "TheR:{", "TheR:nl", "TheR:SPACE", "TheR:identifier", "TheR:(", "TheR:identifier", "TheR:,", "TheR:SPACE", "TheR:string", "TheR:,", "TheR:SPACE", "TheR:identifier", "TheR:,", "TheR:SPACE", "TheR:string", "TheR:)", "TheR:nl", "TheR:}", "TheR:SPACE");
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
