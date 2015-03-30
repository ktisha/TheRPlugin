// This is a generated file. Not intended for manual editing.
package com.jetbrains.ther.parsing;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.jetbrains.ther.psi.TheRElementType;
import com.jetbrains.ther.psi.stubs.TheRElementTypeFactory;
import com.jetbrains.ther.psi.*;

public interface TheRElementTypes {

  IElementType THE_R_ARGUMENT_LIST = new TheRElementType("THE_R_ARGUMENT_LIST");
  IElementType THE_R_ASSIGNMENT_STATEMENT = TheRElementTypeFactory.getElementTypeByName("THE_R_ASSIGNMENT_STATEMENT");
  IElementType THE_R_BINARY_EXPRESSION = new TheRElementType("THE_R_BINARY_EXPRESSION");
  IElementType THE_R_BLOCK_EXPRESSION = new TheRElementType("THE_R_BLOCK_EXPRESSION");
  IElementType THE_R_BREAK_STATEMENT = new TheRElementType("THE_R_BREAK_STATEMENT");
  IElementType THE_R_CALL_EXPRESSION = new TheRElementType("THE_R_CALL_EXPRESSION");
  IElementType THE_R_EMPTY_EXPRESSION = new TheRElementType("THE_R_EMPTY_EXPRESSION");
  IElementType THE_R_EXPRESSION = new TheRElementType("THE_R_EXPRESSION");
  IElementType THE_R_FOR_STATEMENT = new TheRElementType("THE_R_FOR_STATEMENT");
  IElementType THE_R_FUNCTION_EXPRESSION = new TheRElementType("THE_R_FUNCTION_EXPRESSION");
  IElementType THE_R_HELP_EXPRESSION = new TheRElementType("THE_R_HELP_EXPRESSION");
  IElementType THE_R_IF_STATEMENT = new TheRElementType("THE_R_IF_STATEMENT");
  IElementType THE_R_LOGICAL_LITERAL_EXPRESSION = new TheRElementType("THE_R_LOGICAL_LITERAL_EXPRESSION");
  IElementType THE_R_NEXT_STATEMENT = new TheRElementType("THE_R_NEXT_STATEMENT");
  IElementType THE_R_NUMERIC_LITERAL_EXPRESSION = new TheRElementType("THE_R_NUMERIC_LITERAL_EXPRESSION");
  IElementType THE_R_PARAMETER = new TheRElementType("THE_R_PARAMETER");
  IElementType THE_R_PARAMETER_LIST = new TheRElementType("THE_R_PARAMETER_LIST");
  IElementType THE_R_PARENTHESIZED_EXPRESSION = new TheRElementType("THE_R_PARENTHESIZED_EXPRESSION");
  IElementType THE_R_PREFIX_EXPRESSION = new TheRElementType("THE_R_PREFIX_EXPRESSION");
  IElementType THE_R_REFERENCE_EXPRESSION = new TheRElementType("THE_R_REFERENCE_EXPRESSION");
  IElementType THE_R_REPEAT_STATEMENT = new TheRElementType("THE_R_REPEAT_STATEMENT");
  IElementType THE_R_SLICE_EXPRESSION = new TheRElementType("THE_R_SLICE_EXPRESSION");
  IElementType THE_R_STRING_LITERAL_EXPRESSION = new TheRElementType("THE_R_STRING_LITERAL_EXPRESSION");
  IElementType THE_R_SUBSCRIPTION_EXPRESSION = new TheRElementType("THE_R_SUBSCRIPTION_EXPRESSION");
  IElementType THE_R_WHILE_STATEMENT = new TheRElementType("THE_R_WHILE_STATEMENT");

  IElementType THE_R_AND = new TheRElementType("&");
  IElementType THE_R_ANDAND = new TheRElementType("&&");
  IElementType THE_R_AT = new TheRElementType("@");
  IElementType THE_R_BREAK = new TheRElementType("break");
  IElementType THE_R_COLON = new TheRElementType(":");
  IElementType THE_R_COMMA = new TheRElementType(",");
  IElementType THE_R_COMPLEX = new TheRElementType("complex");
  IElementType THE_R_DIV = new TheRElementType("/");
  IElementType THE_R_DOUBLECOLON = new TheRElementType("::");
  IElementType THE_R_ELSE = new TheRElementType("else");
  IElementType THE_R_EQ = new TheRElementType("=");
  IElementType THE_R_EQEQ = new TheRElementType("==");
  IElementType THE_R_EXP = new TheRElementType("^");
  IElementType THE_R_FALSE = new TheRElementType("FALSE");
  IElementType THE_R_FOR = new TheRElementType("for");
  IElementType THE_R_FUNCTION = new TheRElementType("function");
  IElementType THE_R_GE = new TheRElementType(">=");
  IElementType THE_R_GT = new TheRElementType(">");
  IElementType THE_R_HELP = new TheRElementType("help");
  IElementType THE_R_IDENTIFIER = new TheRElementType("identifier");
  IElementType THE_R_IF = new TheRElementType("if");
  IElementType THE_R_IN = new TheRElementType("in");
  IElementType THE_R_INF = new TheRElementType("INF");
  IElementType THE_R_INFIX_OP = new TheRElementType("INFIX_OP");
  IElementType THE_R_INTEGER = new TheRElementType("integer");
  IElementType THE_R_INT_DIV = new TheRElementType("INT_DIV");
  IElementType THE_R_KRONECKER_PROD = new TheRElementType("KRONECKER_PROD");
  IElementType THE_R_LBRACE = new TheRElementType("{");
  IElementType THE_R_LBRACKET = new TheRElementType("[");
  IElementType THE_R_LDBRACKET = new TheRElementType("[[");
  IElementType THE_R_LE = new TheRElementType("<=");
  IElementType THE_R_LEFT_ASSIGN = new TheRElementType("<-");
  IElementType THE_R_LEFT_COMPLEX_ASSIGN = new TheRElementType("<<-");
  IElementType THE_R_LIST_SUBSET = new TheRElementType("$");
  IElementType THE_R_LPAR = new TheRElementType("(");
  IElementType THE_R_LT = new TheRElementType("<");
  IElementType THE_R_MATCHING = new TheRElementType("MATCHING");
  IElementType THE_R_MATRIX_PROD = new TheRElementType("MATRIX_PROD");
  IElementType THE_R_MINUS = new TheRElementType("-");
  IElementType THE_R_MODULUS = new TheRElementType("MODULUS");
  IElementType THE_R_MULT = new TheRElementType("*");
  IElementType THE_R_NA = new TheRElementType("NA");
  IElementType THE_R_NAN = new TheRElementType("NAN");
  IElementType THE_R_NA_CHARACTER = new TheRElementType("NA_CHARACTER");
  IElementType THE_R_NA_COMPLEX = new TheRElementType("NA_COMPLEX");
  IElementType THE_R_NA_INTEGER = new TheRElementType("NA_INTEGER");
  IElementType THE_R_NA_REAL = new TheRElementType("NA_REAL");
  IElementType THE_R_NEXT = new TheRElementType("next");
  IElementType THE_R_NL = new TheRElementType("nl");
  IElementType THE_R_NOT = new TheRElementType("!");
  IElementType THE_R_NOTEQ = new TheRElementType("!=");
  IElementType THE_R_NULL = new TheRElementType("NULL");
  IElementType THE_R_NUMERIC = new TheRElementType("numeric");
  IElementType THE_R_OR = new TheRElementType("|");
  IElementType THE_R_OROR = new TheRElementType("||");
  IElementType THE_R_OUTER_PROD = new TheRElementType("OUTER_PROD");
  IElementType THE_R_PLUS = new TheRElementType("+");
  IElementType THE_R_RBRACE = new TheRElementType("}");
  IElementType THE_R_RBRACKET = new TheRElementType("]");
  IElementType THE_R_RDBRACKET = new TheRElementType("]]");
  IElementType THE_R_REPEAT = new TheRElementType("repeat");
  IElementType THE_R_RIGHT_ASSIGN = new TheRElementType("->");
  IElementType THE_R_RIGHT_COMPLEX_ASSIGN = new TheRElementType("->>");
  IElementType THE_R_RPAR = new TheRElementType(")");
  IElementType THE_R_SEMI = new TheRElementType(";");
  IElementType THE_R_STRING = new TheRElementType("string");
  IElementType THE_R_TILDE = new TheRElementType("~");
  IElementType THE_R_TRIPLECOLON = new TheRElementType(":::");
  IElementType THE_R_TRIPLE_DOTS = new TheRElementType("TRIPLE_DOTS");
  IElementType THE_R_TRUE = new TheRElementType("TRUE");
  IElementType THE_R_WHILE = new TheRElementType("while");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
       if (type == THE_R_ARGUMENT_LIST) {
        return new TheRArgumentListImpl(node);
      }
      else if (type == THE_R_ASSIGNMENT_STATEMENT) {
        return new TheRAssignmentStatementImpl(node);
      }
      else if (type == THE_R_BINARY_EXPRESSION) {
        return new TheRBinaryExpressionImpl(node);
      }
      else if (type == THE_R_BLOCK_EXPRESSION) {
        return new TheRBlockExpressionImpl(node);
      }
      else if (type == THE_R_BREAK_STATEMENT) {
        return new TheRBreakStatementImpl(node);
      }
      else if (type == THE_R_CALL_EXPRESSION) {
        return new TheRCallExpressionImpl(node);
      }
      else if (type == THE_R_EMPTY_EXPRESSION) {
        return new TheREmptyExpressionImpl(node);
      }
      else if (type == THE_R_EXPRESSION) {
        return new TheRExpressionImpl(node);
      }
      else if (type == THE_R_FOR_STATEMENT) {
        return new TheRForStatementImpl(node);
      }
      else if (type == THE_R_FUNCTION_EXPRESSION) {
        return new TheRFunctionExpressionImpl(node);
      }
      else if (type == THE_R_HELP_EXPRESSION) {
        return new TheRHelpExpressionImpl(node);
      }
      else if (type == THE_R_IF_STATEMENT) {
        return new TheRIfStatementImpl(node);
      }
      else if (type == THE_R_LOGICAL_LITERAL_EXPRESSION) {
        return new TheRLogicalLiteralExpressionImpl(node);
      }
      else if (type == THE_R_NEXT_STATEMENT) {
        return new TheRNextStatementImpl(node);
      }
      else if (type == THE_R_NUMERIC_LITERAL_EXPRESSION) {
        return new TheRNumericLiteralExpressionImpl(node);
      }
      else if (type == THE_R_PARAMETER) {
        return new TheRParameterImpl(node);
      }
      else if (type == THE_R_PARAMETER_LIST) {
        return new TheRParameterListImpl(node);
      }
      else if (type == THE_R_PARENTHESIZED_EXPRESSION) {
        return new TheRParenthesizedExpressionImpl(node);
      }
      else if (type == THE_R_PREFIX_EXPRESSION) {
        return new TheRPrefixExpressionImpl(node);
      }
      else if (type == THE_R_REFERENCE_EXPRESSION) {
        return new TheRReferenceExpressionImpl(node);
      }
      else if (type == THE_R_REPEAT_STATEMENT) {
        return new TheRRepeatStatementImpl(node);
      }
      else if (type == THE_R_SLICE_EXPRESSION) {
        return new TheRSliceExpressionImpl(node);
      }
      else if (type == THE_R_STRING_LITERAL_EXPRESSION) {
        return new TheRStringLiteralExpressionImpl(node);
      }
      else if (type == THE_R_SUBSCRIPTION_EXPRESSION) {
        return new TheRSubscriptionExpressionImpl(node);
      }
      else if (type == THE_R_WHILE_STATEMENT) {
        return new TheRWhileStatementImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
