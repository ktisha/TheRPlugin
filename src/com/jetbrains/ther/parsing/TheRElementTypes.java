package com.jetbrains.ther.parsing;

import com.intellij.psi.tree.IFileElementType;
import com.jetbrains.ther.psi.*;

public interface TheRElementTypes {
  IFileElementType FILE = new TheRFileElementType();

  // statements
  TheRElementType EXPRESSION_STATEMENT = new TheRElementType("EXPRESSION_STATEMENT", TheRExpressionStatementImpl.class);
  TheRElementType ASSIGNMENT_STATEMENT = new TheRElementType("ASSIGNMENT_STATEMENT", TheRAssignmentStatementImpl.class);
  TheRElementType IF_STATEMENT = new TheRElementType("IF_STATEMENT", TheRIfStatementImpl.class);
  TheRElementType WHILE_STATEMENT = new TheRElementType("WHILE_STATEMENT", TheRWhileStatementImpl.class);
  TheRElementType FOR_STATEMENT = new TheRElementType("FOR_STATEMENT", TheRForStatementImpl.class);
  TheRElementType REPEAT_STATEMENT = new TheRElementType("REPEAT_STATEMENT", TheRRepeatStatementImpl.class);
  TheRElementType BREAK_STATEMENT = new TheRElementType("BREAK_STATEMENT", TheRBreakStatementImpl.class);
  TheRElementType NEXT_STATEMENT = new TheRElementType("NEXT_STATEMENT", TheRNextStatementImpl.class);

  // expressions
  TheRElementType INTEGER_LITERAL_EXPRESSION = new TheRElementType("INTEGER_LITERAL_EXPRESSION", TheRNumericLiteralExpressionImpl.class);
  TheRElementType IMAGINARY_LITERAL_EXPRESSION = new TheRElementType("IMAGINARY_LITERAL_EXPRESSION", TheRNumericLiteralExpressionImpl.class);
  TheRElementType STRING_LITERAL_EXPRESSION = new TheRElementType("STRING_LITERAL_EXPRESSION", TheRStringLiteralExpressionImpl.class);
  TheRElementType REFERENCE_EXPRESSION = new TheRElementType("REFERENCE_EXPRESSION", TheRReferenceExpressionImpl.class);
  TheRElementType SLICE_EXPRESSION = new TheRElementType("SLICE_EXPRESSION", TheRSliceExpressionImpl.class);
  TheRElementType BINARY_EXPRESSION = new TheRElementType("BINARY_EXPRESSION", TheRBinaryExpressionImpl.class);
  TheRElementType PREFIX_EXPRESSION = new TheRElementType("PREFIX_EXPRESSION", TheRPrefixExpressionImpl.class);
  TheRElementType CALL_EXPRESSION = new TheRElementType("CALL_EXPRESSION", TheRCallExpressionImpl.class);
  TheRElementType KEYWORD_ARGUMENT_EXPRESSION = new TheRElementType("KEYWORD_ARGUMENT_EXPRESSION", TheRKeywordExpressionImpl.class);
  TheRElementType SUBSCRIPTION_EXPRESSION = new TheRElementType("SUBSCRIPTION_EXPRESSION", TheRSubscriptionExpressionImpl.class);
  TheRElementType EMPTY_EXPRESSION = new TheRElementType("EMPTY_EXPRESSION", TheREmptyExpressionImpl.class);
  TheRElementType HELP_EXPRESSION = new TheRElementType("HELP_EXPRESSION", TheRHelpExpressionImpl.class);
  TheRElementType REPR_EXPRESSION = new TheRElementType("REPR_EXPRESSION", TheRReprExpressionImpl.class);
  TheRElementType OPERATOR_EXPRESSION = new TheRElementType("OPERATOR_EXPRESSION", TheROperatorExpressionImpl.class);
  TheRElementType PARENTHESIZED_EXPRESSION = new TheRElementType("PARENTHESIZED_EXPRESSION", TheRParenthesizedExpressionImpl.class);


  TheRElementType ARGUMENT_LIST = new TheRElementType("ARGUMENT_LIST", TheRArgumentListImpl.class);
  TheRElementType PARAMETER_LIST = new TheRElementType("PARAMETER_LIST", TheRParameterListImpl.class);
  TheRElementType PARAMETER = new TheRElementType("PARAMETER", TheRParameterImpl.class);
  TheRElementType FUNCTION_DECLARATION = new TheRElementType("FUNCTION_DECLARATION", TheRFunctionImpl.class);
  TheRElementType BLOCK = new TheRElementType("BLOCK", TheRBlockImpl.class);

}
