// This is a generated file. Not intended for manual editing.
package com.jetbrains.ther.psi.api;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;

public class TheRVisitor extends PsiElementVisitor {

  public void visitArgumentList(@NotNull TheRArgumentList o) {
    visitElement(o);
  }

  public void visitAssignmentStatement(@NotNull TheRAssignmentStatement o) {
    visitExpression(o);
    // visitNamedElement(o);
  }

  public void visitBinaryExpression(@NotNull TheRBinaryExpression o) {
    visitExpression(o);
  }

  public void visitBlockExpression(@NotNull TheRBlockExpression o) {
    visitExpression(o);
  }

  public void visitBreakStatement(@NotNull TheRBreakStatement o) {
    visitExpression(o);
  }

  public void visitCallExpression(@NotNull TheRCallExpression o) {
    visitExpression(o);
  }

  public void visitEmptyExpression(@NotNull TheREmptyExpression o) {
    visitExpression(o);
  }

  public void visitExpression(@NotNull TheRExpression o) {
    visitElement(o);
  }

  public void visitForStatement(@NotNull TheRForStatement o) {
    visitExpression(o);
  }

  public void visitFunctionExpression(@NotNull TheRFunctionExpression o) {
    visitExpression(o);
  }

  public void visitHelpExpression(@NotNull TheRHelpExpression o) {
    visitExpression(o);
  }

  public void visitIfStatement(@NotNull TheRIfStatement o) {
    visitExpression(o);
  }

  public void visitNextStatement(@NotNull TheRNextStatement o) {
    visitExpression(o);
  }

  public void visitNumericLiteralExpression(@NotNull TheRNumericLiteralExpression o) {
    visitExpression(o);
  }

  public void visitParameter(@NotNull TheRParameter o) {
    visitNamedElement(o);
  }

  public void visitParameterList(@NotNull TheRParameterList o) {
    visitElement(o);
  }

  public void visitParenthesizedExpression(@NotNull TheRParenthesizedExpression o) {
    visitExpression(o);
  }

  public void visitPrefixExpression(@NotNull TheRPrefixExpression o) {
    visitExpression(o);
  }

  public void visitReferenceExpression(@NotNull TheRReferenceExpression o) {
    visitExpression(o);
  }

  public void visitRepeatStatement(@NotNull TheRRepeatStatement o) {
    visitExpression(o);
  }

  public void visitSliceExpression(@NotNull TheRSliceExpression o) {
    visitExpression(o);
  }

  public void visitStringLiteralExpression(@NotNull TheRStringLiteralExpression o) {
    visitExpression(o);
  }

  public void visitSubscriptionExpression(@NotNull TheRSubscriptionExpression o) {
    visitExpression(o);
  }

  public void visitWhileStatement(@NotNull TheRWhileStatement o) {
    visitExpression(o);
  }

  public void visitNamedElement(@NotNull TheRNamedElement o) {
    visitElement(o);
  }

  public void visitElement(@NotNull TheRElement o) {
    visitElement(o);
  }

}
