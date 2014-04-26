package com.jetbrains.ther.parsing;

import com.intellij.psi.tree.IFileElementType;
import com.jetbrains.ther.psi.*;

public interface TheRElementTypes {
  IFileElementType FILE = new TheRFileElementType();

  // statements
  TheRElementType EXPRESSION_STATEMENT = new TheRElementType("EXPRESSION_STATEMENT", TheRExpressionStatementImpl.class);
  TheRElementType ASSIGNMENT_STATEMENT = new TheRElementType("ASSIGNMENT_STATEMENT", TheRAssignmentStatementImpl.class);

  // expressions
  TheRElementType INTEGER_LITERAL_EXPRESSION = new TheRElementType("INTEGER_LITERAL_EXPRESSION", TheRNumericLiteralExpressionImpl.class);
  TheRElementType FLOAT_LITERAL_EXPRESSION = new TheRElementType("FLOAT_LITERAL_EXPRESSION", TheRNumericLiteralExpressionImpl.class);
  TheRElementType IMAGINARY_LITERAL_EXPRESSION = new TheRElementType("IMAGINARY_LITERAL_EXPRESSION", TheRNumericLiteralExpressionImpl.class);
  TheRElementType STRING_LITERAL_EXPRESSION = new TheRElementType("STRING_LITERAL_EXPRESSION", TheRStringLiteralExpressionImpl.class);
  TheRElementType REFERENCE_EXPRESSION = new TheRElementType("REFERENCE_EXPRESSION", TheRReferenceExpressionImpl.class);

}
