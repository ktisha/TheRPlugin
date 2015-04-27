package com.jetbrains.ther;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.ther.psi.api.*;

import java.util.*;

public class TheRStaticAnalyzerHelper {

  private interface StaticAnalysisResult {
    StaticAnalysisResult applyRead(TheRReferenceExpression ref);

    StaticAnalysisResult applyAssign(TheRAssignmentStatement assignmentStatement);

    StaticAnalysisResult merge(StaticAnalysisResult other);

    void applyReturn();

    boolean isEnd();
  }

  private static class ReachDefinitions implements StaticAnalysisResult {

    private TheRReferenceExpression myWhat;
    private Set<PsiElement> myDefinitions = new HashSet<PsiElement>();
    private boolean myEnd;

    private ReachDefinitions(TheRReferenceExpression what) {
      myWhat = what;
    }

    @Override
    public StaticAnalysisResult applyRead(TheRReferenceExpression ref) {
      if (ref == myWhat) {
        myEnd = true;
      }
      return this;
    }

    @Override
    public StaticAnalysisResult applyAssign(TheRAssignmentStatement assignmentStatement) {
      PsiElement assignee = assignmentStatement.getAssignee();
      if (assignee != null && assignee.getText().equals(myWhat.getName())) {
        ReachDefinitions result = new ReachDefinitions(myWhat);
        result.myDefinitions.add(assignmentStatement);
        return result;
      }
      return this;
    }

    @Override
    public StaticAnalysisResult merge(StaticAnalysisResult other) {
      if (!(other instanceof ReachDefinitions)) {
        throw new IllegalArgumentException();
      }
      myDefinitions.addAll(((ReachDefinitions)other).myDefinitions);
      return this;
    }

    @Override
    public void applyReturn() { }

    @Override
    public boolean isEnd() {
      return myEnd;
    }

    public void addDefinition(PsiElement definition) {
      myDefinitions.add(definition);
    }

    public Set<PsiElement> getDefinitions() {
      return myDefinitions;
    }
  }

  private static class OptionalParameters implements StaticAnalysisResult {

    private Set<String> myPossibleOptionals = new HashSet<String>();
    private Set<String> myOptionals = new HashSet<String>();
    private Set<String> myReaded = new HashSet<String>();

    private OptionalParameters(List<TheRParameter> parameters) {
      for (TheRParameter parameter : parameters) {
        if (parameter.getName() != null && parameter.getExpression() == null) {
          myPossibleOptionals.add(parameter.getName());
        }
      }
    }

    public OptionalParameters(OptionalParameters parameters) {
      myPossibleOptionals = parameters.myPossibleOptionals;
      myOptionals.addAll(parameters.myOptionals);
      myReaded.addAll(parameters.myReaded);
    }

    @Override
    public StaticAnalysisResult applyRead(TheRReferenceExpression ref) {
      PsiElement parent = ref.getParent();
      if (parent instanceof TheRArgumentList) {
        parent = parent.getParent();
        if (parent instanceof TheRCallExpression) {
          TheRCallExpression callExpression = (TheRCallExpression)parent;
          String functionName = callExpression.getExpression().getText();
          if ("missing".equals(functionName) || "is.null".equals(functionName)) {
            return this;
          }
        }
      }
      String name = ref.getName();
      if (myPossibleOptionals.contains(name) && !myOptionals.contains(name)) {
        OptionalParameters newParams = new OptionalParameters(this);
        newParams.myReaded.add(name);
        return newParams;
      }
      return this;
    }

    @Override
    public StaticAnalysisResult applyAssign(TheRAssignmentStatement assignmentStatement) {
      PsiElement assignee = assignmentStatement.getAssignee();
      if (assignee == null) {
        return this;
      }
      String name = assignee.getText();
      if (myPossibleOptionals.contains(name) && !myReaded.contains(name)) {
        OptionalParameters newParams = new OptionalParameters(this);
        newParams.myOptionals.add(name);
        return newParams;
      }
      return this;
    }

    @Override
    public StaticAnalysisResult merge(StaticAnalysisResult other) {
      if (!(other instanceof OptionalParameters)) {
        throw new IllegalArgumentException();
      }
      OptionalParameters otherParams = (OptionalParameters)other;
      myOptionals.addAll(otherParams.myOptionals);
      Set<String> mergeReaded = new HashSet<String>();
      for (String name : myReaded) {
        if (otherParams.myReaded.contains(name)) {
          mergeReaded.add(name);
        }
      }
      myReaded = mergeReaded;
      return this;
    }

    @Override
    public void applyReturn() {
      for (String name : myPossibleOptionals) {
        if (!myReaded.contains(name)) {
          myOptionals.add(name);
        }
      }
    }

    @Override
    public boolean isEnd() {
      return false;
    }

    public Set<String> getOptionalParameters() {
      return myOptionals;
    }
  }


  public static Set<PsiElement> reachDefinitions(TheRReferenceExpression what) {
    ReachDefinitions resolveResult = new ReachDefinitions(what);
    TheRFunctionExpression functionExpression = PsiTreeUtil.getParentOfType(what, TheRFunctionExpression.class);
    if (functionExpression != null) {
      List<TheRParameter> parameterList = functionExpression.getParameterList().getParameterList();
      for (TheRParameter parameter : parameterList) {
        String name = parameter.getName();
        if (name != null && name.equals(what.getText())) {
          resolveResult.addDefinition(parameter);
          break;
        }
      }
      resolveResult = (ReachDefinitions)analyze(functionExpression.getExpression(), resolveResult);
    }
    else {
      PsiFile file = what.getContainingFile();
      TheRExpression[] expressions = PsiTreeUtil.getChildrenOfType(file, TheRExpression.class);
      assert expressions != null;
      resolveResult = (ReachDefinitions)analyzeSequence(Arrays.asList(expressions), resolveResult);
    }
    return resolveResult.getDefinitions();
  }

  public static Set<String> optionalParameters(TheRFunctionExpression function) {
    TheRExpression functionExpression = function.getExpression();
    if (!TheRBlockExpression.class.isInstance(functionExpression)) {
      return new HashSet<String>();
    }
    OptionalParameters parameters = new OptionalParameters(function.getParameterList().getParameterList());
    parameters = (OptionalParameters)analyze(functionExpression, parameters);
    parameters.applyReturn();
    return parameters.getOptionalParameters();
  }

  private static StaticAnalysisResult analyze(TheRPsiElement where, StaticAnalysisResult parentResult) {
    //TODO: find better way --- can we use just ==
    if (where instanceof TheRReferenceExpression) {
      return parentResult.applyRead((TheRReferenceExpression)where);
    }

    if (where instanceof TheRAssignmentStatement) {
      TheRAssignmentStatement assignment = (TheRAssignmentStatement)where;
      StaticAnalysisResult resolveResult = analyze(assignment.getAssignedValue(), parentResult);
      if (resolveResult.isEnd()) {
        return resolveResult;
      }
      resolveResult = resolveResult.applyAssign(assignment);
      return resolveResult;
    }

    if (where instanceof TheRIfStatement) {
      TheRIfStatement ifStatement = (TheRIfStatement)where;
      List<TheRExpression> list = ifStatement.getExpressionList();
      TheRExpression condition = list.get(0);
      StaticAnalysisResult conditionResult = analyze(condition, parentResult);
      if (conditionResult.isEnd()) {
        return conditionResult;
      }
      StaticAnalysisResult thenResult = analyze(list.get(1), conditionResult);
      if (thenResult.isEnd()) {
        return thenResult;
      }
      StaticAnalysisResult elseResult = conditionResult;
      if (list.size() == 3) {
        elseResult = analyze(list.get(2), conditionResult);
        if (elseResult.isEnd()) {
          return elseResult;
        }
      }
      return thenResult.merge(elseResult);
    }

    if (where instanceof TheRBinaryExpression || where instanceof TheRPrefixExpression) {
      TheRExpression[] expressions = PsiTreeUtil.getChildrenOfType(where, TheRExpression.class);
      if (expressions == null) {
        return parentResult;
      }
      return analyzeSequence(Arrays.asList(expressions), parentResult);
    }

    if (where instanceof TheRBlockExpression) {
      TheRBlockExpression block = (TheRBlockExpression)where;
      return analyzeSequence(block.getExpressionList(), parentResult);
    }

    if (where instanceof TheRParenthesizedExpression) {
      TheRParenthesizedExpression parenthesized = (TheRParenthesizedExpression)where;
      return analyze(parenthesized.getExpression(), parentResult);
    }

    if (where instanceof TheRWhileStatement) {
      TheRWhileStatement whileStatement = (TheRWhileStatement)where;
      List<TheRExpression> list = whileStatement.getExpressionList();
      StaticAnalysisResult conditionResult = analyze(list.get(0), parentResult);
      if (conditionResult.isEnd()) {
        return conditionResult;
      }
      StaticAnalysisResult bodyResult = analyze(list.get(1), conditionResult);
      if (bodyResult.isEnd()) {
        return bodyResult;
      }
      return bodyResult.merge(conditionResult);
    }

    if (where instanceof TheRRepeatStatement) {
      TheRRepeatStatement repeatStatement = (TheRRepeatStatement)where;
      StaticAnalysisResult resolveResult = analyze(repeatStatement.getExpression(), parentResult);
      if (resolveResult.isEnd()) {
        return resolveResult;
      }
      return resolveResult.merge(parentResult);
    }

    if (where instanceof TheRCallExpression) {
      TheRCallExpression callExpression = (TheRCallExpression)where;
      List<TheRExpression> expressions = new ArrayList<TheRExpression>();
      expressions.add(callExpression.getExpression());
      for (TheRExpression argument : callExpression.getArgumentList().getExpressionList()) {
        TheRExpression child = argument;
        if (argument instanceof TheRAssignmentStatement) {
          child = (TheRExpression)((TheRAssignmentStatement)argument).getAssignedValue();
        }
        expressions.add(child);
      }
      StaticAnalysisResult result = analyzeSequence(expressions, parentResult);
      if (result.isEnd()) {
        return result;
      }
      if ("return".equals(callExpression.getExpression().getText())) {
        result.applyReturn();
      }
      return result;
    }

    if (where instanceof TheRSubscriptionExpression) {
      TheRSubscriptionExpression subscriptionExpression = (TheRSubscriptionExpression)where;
      return analyzeSequence(subscriptionExpression.getExpressionList(), parentResult);
    }

    if (where instanceof TheRSliceExpression) {
      TheRSliceExpression sliceExpression = (TheRSliceExpression)where;
      return analyzeSequence(sliceExpression.getExpressionList(), parentResult);
    }

    //TODO: look at other expressions (for, call expression)
    return parentResult;
  }


  private static StaticAnalysisResult analyzeSequence(List<TheRExpression> expressions, StaticAnalysisResult parentResult) {
    StaticAnalysisResult resolveResult = parentResult;
    for (TheRExpression expression : expressions) {
      resolveResult = analyze(expression, resolveResult);
      if (resolveResult.isEnd()) {
        return resolveResult;
      }
    }
    return resolveResult;
  }
}
