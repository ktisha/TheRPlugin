package com.jetbrains.ther.psi;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.ther.TheRElementGenerator;
import com.jetbrains.ther.parsing.TheRElementTypes;
import com.jetbrains.ther.psi.api.*;
import com.jetbrains.ther.psi.references.TheRReferenceImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Alefas
 * @since 27/01/15.
 */
public class TheRPsiImplUtil {
  public static final TokenSet LEFT_ASSIGNMENTS = TokenSet.create(
    TheRElementTypes.THE_R_LEFT_ASSIGN, TheRElementTypes.THE_R_LEFT_COMPLEX_ASSIGN);
  public static final TokenSet RIGHT_ASSIGNMENTS = TokenSet.create(
    TheRElementTypes.THE_R_RIGHT_ASSIGN, TheRElementTypes.THE_R_RIGHT_COMPLEX_ASSIGN);
  public static final TokenSet RESERVED_WORDS = TokenSet.create(
    TheRElementTypes.THE_R_IF, TheRElementTypes.THE_R_ELSE, TheRElementTypes.THE_R_REPEAT,
    TheRElementTypes.THE_R_WHILE, TheRElementTypes.THE_R_FUNCTION, TheRElementTypes.THE_R_FOR,
    TheRElementTypes.THE_R_IN, TheRElementTypes.THE_R_NEXT, TheRElementTypes.THE_R_BREAK);
  public static final TokenSet OPERATORS = TokenSet.create(
    TheRElementTypes.THE_R_MINUS, TheRElementTypes.THE_R_PLUS, TheRElementTypes.THE_R_NOT, TheRElementTypes.THE_R_TILDE, TheRElementTypes.THE_R_HELP,
    TheRElementTypes.THE_R_COLON, TheRElementTypes.THE_R_MULT, TheRElementTypes.THE_R_DIV, TheRElementTypes.THE_R_EXP, TheRElementTypes.THE_R_MODULUS,
    TheRElementTypes.THE_R_INT_DIV, TheRElementTypes.THE_R_MATRIX_PROD, TheRElementTypes.THE_R_OUTER_PROD, TheRElementTypes.THE_R_MATCHING, TheRElementTypes.THE_R_KRONECKER_PROD,
    TheRElementTypes.THE_R_INFIX_OP, TheRElementTypes.THE_R_LT, TheRElementTypes.THE_R_GT, TheRElementTypes.THE_R_EQEQ, TheRElementTypes.THE_R_GE,
    TheRElementTypes.THE_R_LE, TheRElementTypes.THE_R_AND, TheRElementTypes.THE_R_ANDAND, TheRElementTypes.THE_R_OR, TheRElementTypes.THE_R_OROR,
    TheRElementTypes.THE_R_LEFT_ASSIGN, TheRElementTypes.THE_R_RIGHT_ASSIGN, TheRElementTypes.THE_R_LIST_SUBSET, TheRElementTypes.THE_R_AT);


  public static boolean isLeft(TheRAssignmentStatement assignment) {
    final ASTNode operator = assignment.getNode().findChildByType(LEFT_ASSIGNMENTS);
    return operator != null;
  }

  public static boolean isRight(TheRAssignmentStatement assignment) {
    final ASTNode operator = assignment.getNode().findChildByType(RIGHT_ASSIGNMENTS);
    return operator != null;
  }

  public static TheRPsiElement getAssignedValue(TheRAssignmentStatement assignment) {
    PsiElement child;
    if (!assignment.isRight()) {
      child = assignment.getLastChild();
      while (child != null && !(child instanceof TheRExpression)) {
        if (child instanceof PsiErrorElement) return null; // incomplete assignment operator can't be analyzed properly, bail out.
        child = child.getPrevSibling();
      }
    } else {
      child = assignment.getFirstChild();
      while (child != null && !(child instanceof TheRExpression)) {
        if (child instanceof PsiErrorElement) return null; // incomplete assignment operator can't be analyzed properly, bail out.
        child = child.getNextSibling();
      }
    }
    return (TheRPsiElement)child;
  }

  public static PsiElement getAssignee(TheRAssignmentStatement assignment) {
    final ASTNode node = assignment.getNode();
    if (!assignment.isRight()) {
      ASTNode childNode = node.findChildByType(TheRElementTypes.THE_R_REFERENCE_EXPRESSION);
      return childNode == null ? null : childNode.getPsi();
    }
    for (ASTNode element = node.getLastChildNode(); element != null; element = element.getTreePrev()) {
      if (element.getElementType() == TheRElementTypes.THE_R_REFERENCE_EXPRESSION) {
        return element.getPsi();
      }
    }
    return null;
  }

  public static PsiElement setName(TheRAssignmentStatement assignment, String name) {
    ASTNode nameNode = assignment.getNameNode();
    if (nameNode == null) {
      return assignment;
    }
    final ASTNode oldNameIdentifier = nameNode.findChildByType(TheRElementTypes.THE_R_IDENTIFIER);
    if (oldNameIdentifier != null) {
      final PsiFile dummyFile = TheRElementGenerator.createDummyFile(name, false, assignment.getProject());
      ASTNode identifier = dummyFile.getNode().getFirstChildNode().findChildByType(TheRElementTypes.THE_R_IDENTIFIER);
      if (identifier != null) {
        nameNode.replaceChild(oldNameIdentifier, identifier);
      }
    }
    return assignment;
  }

  public static String getName(TheRAssignmentStatement assignment) {
    final ASTNode node = assignment.getNameNode();
    return node != null ? node.getText() : null;
  }

  public static ASTNode getNameNode(TheRAssignmentStatement assignment) {
    PsiElement assignee = assignment.getAssignee();
    return assignee == null ? null : assignee.getNode();
  }

  public static ASTNode getNameNode(TheRParameter parameter) {
    PsiElement identifier = parameter.getIdentifier();
    return identifier == null ? null : identifier.getNode();
  }

  public static String getName(TheRParameter parameter) {
    ASTNode node = parameter.getNameNode();
    return node == null ? null: node.getText();
  }

  public static PsiElement setName(TheRParameter parameter, String name) {
    final ASTNode oldNameIdentifier = parameter.getNameNode();
    if (oldNameIdentifier != null) {
      final PsiFile dummyFile = TheRElementGenerator.createDummyFile(name, false, parameter.getProject());
      ASTNode identifier = dummyFile.getNode().getFirstChildNode().findChildByType(TheRElementTypes.THE_R_IDENTIFIER);
      if (identifier != null) {
        parameter.getNode().replaceChild(oldNameIdentifier, identifier);
      }
    }
    return parameter;
  }

  public static TheRReferenceImpl getReference(TheRReferenceExpression referenceExpression) {
    final PsiElement nextElement = PsiTreeUtil.skipSiblingsForward(referenceExpression, PsiWhiteSpace.class);
    if (nextElement != null && LEFT_ASSIGNMENTS.contains(nextElement.getNode().getElementType())) return null;
    final PsiElement prevElement = PsiTreeUtil.skipSiblingsBackward(referenceExpression, PsiWhiteSpace.class);
    if (prevElement != null && RIGHT_ASSIGNMENTS.contains(prevElement.getNode().getElementType())) return null;
    return new TheRReferenceImpl(referenceExpression);
  }

  public static String getDocStringValue(@NotNull final TheRFunctionExpression functionExpression) {  //TODO: make stub-aware
    final TheRAssignmentStatement statement = PsiTreeUtil.getParentOfType(functionExpression, TheRAssignmentStatement.class);
    if (statement == null) return null;

    List<PsiComment> comments = new ArrayList<PsiComment>();
    for (PsiElement sibling = statement.getPrevSibling(); sibling != null && !(sibling instanceof TheRExpression);
         sibling = sibling.getPrevSibling()) {
      if (sibling instanceof PsiComment) {
        comments.add((PsiComment)sibling);
      }
    }

    if (comments.isEmpty()) return null;
    Collections.reverse(comments);
    return getCommentText(comments);
  }

  private static String getCommentText(@NotNull final List<PsiComment> comments) {
    final StringBuilder stringBuilder = new StringBuilder();
    for (PsiComment comment : comments) {
      String string = comment.getText();
      stringBuilder.append(StringUtil.trimStart(string, "#")).append("\n");
    }
    return stringBuilder.toString();
  }

  @Nullable
  public static String getNamespace(TheRReferenceExpression referenceExpression) {
    final String text = referenceExpression.getText();
    final int namespaceIndex = text.indexOf("::");
    if (namespaceIndex > 0) {
      return text.substring(0, namespaceIndex);
    }
    return null;
  }

  public static String getName(TheRReferenceExpression referenceExpression) {
    final String text = referenceExpression.getText();
    final int namespaceIndex = text.indexOf("::");
    if (namespaceIndex > 0) {
      return text.substring(namespaceIndex + 2);
    }
    return text;
  }
}
