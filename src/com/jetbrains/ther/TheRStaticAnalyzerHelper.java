package com.jetbrains.ther;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.ther.psi.api.*;
import com.jetbrains.ther.typing.TheRTypeContext;
import com.jetbrains.ther.typing.TheRTypeProvider;
import com.jetbrains.ther.typing.types.*;

import java.util.*;

public class TheRStaticAnalyzerHelper {

  private interface StaticAnalysisResult {
    StaticAnalysisResult applyRead(TheRReferenceExpression ref);

    StaticAnalysisResult applyAssign(TheRAssignmentStatement assignmentStatement);

    StaticAnalysisResult applyAssignInFor(TheRExpression assignee, TheRExpression expression);

    StaticAnalysisResult merge(StaticAnalysisResult other);

    void applyReturn();

    boolean isEnd();
  }

  private static class OptionalParameters implements StaticAnalysisResult {

    private Set<String> myPossibleOptionals = new HashSet<String>();
    private Set<String> myOptionals = new HashSet<String>();
    private Set<String> myRead = new HashSet<String>();

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
      myRead.addAll(parameters.myRead);
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
        newParams.myRead.add(name);
        return newParams;
      }
      return this;
    }

    private StaticAnalysisResult applyWrite(String name) {
      if (myPossibleOptionals.contains(name) && !myRead.contains(name)) {
        OptionalParameters newParams = new OptionalParameters(this);
        newParams.myOptionals.add(name);
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
      return applyWrite(assignee.getText());
    }

    @Override
    public StaticAnalysisResult applyAssignInFor(TheRExpression assignee, TheRExpression expression) {
      return applyWrite(assignee.getText());
    }

    @Override
    public StaticAnalysisResult merge(StaticAnalysisResult other) {
      if (!(other instanceof OptionalParameters)) {
        throw new IllegalArgumentException();
      }
      OptionalParameters otherParams = (OptionalParameters)other;
      myOptionals.addAll(otherParams.myOptionals);
      Set<String> mergeRead = new HashSet<String>();
      for (String name : myRead) {
        if (otherParams.myRead.contains(name)) {
          mergeRead.add(name);
        }
      }
      myRead = mergeRead;
      return this;
    }

    @Override
    public void applyReturn() {
      for (String name : myPossibleOptionals) {
        if (!myRead.contains(name)) {
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

  private static class ReachTypes implements StaticAnalysisResult {
    final Map<String, TheRType> myReachTypes = new HashMap<String, TheRType>();
    final TheRReferenceExpression myWhat;
    TheRType myResult = null;

    public ReachTypes(TheRReferenceExpression what) {
      myWhat = what;
    }

    private ReachTypes(ReachTypes other) {
      myReachTypes.putAll(other.myReachTypes);
      myWhat = other.myWhat;
      myResult = other.myResult;
    }

    public TheRType getResultType() {
      return myResult;
    }

    @Override
    public StaticAnalysisResult applyRead(TheRReferenceExpression ref) {
      String name = ref.getName();
      TheRType refType = TheRType.UNKNOWN;
      if (myReachTypes.containsKey(name)) {
        refType = myReachTypes.get(name);
      }
      TheRTypeContext.putTypeInCache(ref, refType);
      if (ref.equals(myWhat)) {
        myResult = refType;
      }
      return this;
    }

    @Override
    public StaticAnalysisResult applyAssign(TheRAssignmentStatement assignmentStatement) {
      PsiElement assignee = assignmentStatement.getAssignee();
      TheRPsiElement assignedValue = assignmentStatement.getAssignedValue();
      if (assignedValue == null) {
        return this;
      }
      TheRType assignedValueType = TheRTypeProvider.getType(assignedValue);
      ReachTypes result = this;
      if (assignee instanceof TheRReferenceExpression) {
        TheRReferenceExpression ref = (TheRReferenceExpression)assignee;
        String name = ref.getName();
        result = new ReachTypes(this);
        result.myReachTypes.put(name, assignedValueType);
      }
      if (assignee instanceof TheRMemberExpression) {
        TheRMemberExpression member = (TheRMemberExpression)assignee;
        TheRExpression base = member.getExpression();
        String tag = member.getTag();
        if (base instanceof TheRReferenceExpression) { // check only x$y not f()$y
          TheRReferenceExpression ref = (TheRReferenceExpression)base;
          String name = ref.getName();
          TheRType baseType = myReachTypes.get(name);
          if (baseType == null) {
            baseType = TheRType.UNKNOWN;
          }
          final Set<TheRType> beforeTypes;
          if (baseType instanceof TheRUnionType) {
            beforeTypes = ((TheRUnionType)baseType).getTypes();
          } else {
            beforeTypes = new HashSet<TheRType>();
            beforeTypes.add(baseType);
          }
          Set<TheRType> afterTypes = new HashSet<TheRType>();
          for (TheRType type : beforeTypes) {
            if (!TheRListType.class.isInstance(type)) {
              type = new TheRListType(false);
            }
            TheRListType list = (TheRListType) type;
            if ("NULL".equals(assignedValue.getText())) {
              list.removeField(tag);
            } else {
              list.addField(tag, assignedValueType);
            }
            afterTypes.add(list);
          }
          TheRType resultType = TheRUnionType.create(afterTypes);
          result = new ReachTypes(this);
          result.myReachTypes.put(name, resultType);
        }
      }
      if (assignee instanceof TheRSubscriptionExpression) {
        TheRSubscriptionExpression subscriptionExpression = (TheRSubscriptionExpression)assignee;
        List<TheRExpression> expressionList = subscriptionExpression.getExpressionList();
        TheRExpression base = expressionList.get(0);
        List<TheRExpression> arguments = expressionList.subList(1, expressionList.size());
        if (base instanceof TheRReferenceExpression) {
          String name = base.getName();
          TheRType baseType = myReachTypes.get(name);
          if (baseType == null) {
            baseType = TheRType.UNKNOWN;
          }
          TheRType resultType = baseType.afterSubscriptionType(arguments, assignedValueType);
          result = new ReachTypes(this);
          result.myReachTypes.put(name, resultType);
        }
      }

      //TODO: check for class(x) <- "clazz" here
      return result;
    }

    @Override
    public StaticAnalysisResult applyAssignInFor(TheRExpression assignee, TheRExpression expression) {
      if (!TheRReferenceExpression.class.isInstance(assignee)) {
        return this;
      }
      String name = assignee.getName();
      ReachTypes result = new ReachTypes(this);
      result.myReachTypes.put(name, TheRTypeProvider.getType(expression).getElementTypes());
      return result;
    }

    @Override
    public StaticAnalysisResult merge(StaticAnalysisResult other) {
      if (!ReachTypes.class.isInstance(other)) {
        throw new IllegalArgumentException();
      }
      ReachTypes result = new ReachTypes((ReachTypes)other);
      for (Map.Entry<String, TheRType> entry : myReachTypes.entrySet()) {
        String name = entry.getKey();
        TheRType type = entry.getValue();
        if (result.myReachTypes.containsKey(name)) {
          Set<TheRType> typeSet = new HashSet<TheRType>();
          typeSet.add(type);
          typeSet.add(result.myReachTypes.get(name));
          type = TheRUnionType.create(typeSet);
        }
        result.myReachTypes.put(name, type);
      }
      if (result.myResult == null && myResult != null) {
        result.myResult = myResult;
      }
      return result;
    }

    @Override
    public void applyReturn() {

    }

    @Override
    public boolean isEnd() {
      return false;
    }
  }

  public static TheRType getReferenceType(TheRReferenceExpression what) {
    ReachTypes resolveResult = new ReachTypes(what);
    TheRFunctionExpression functionExpression = PsiTreeUtil.getParentOfType(what, TheRFunctionExpression.class);
    if (functionExpression != null) {
      resolveResult = (ReachTypes)analyze(functionExpression.getExpression(), resolveResult);
    }
    else {
      PsiFile file = what.getContainingFile();
      TheRExpression[] expressions = PsiTreeUtil.getChildrenOfType(file, TheRExpression.class);
      assert expressions != null;
      resolveResult = (ReachTypes)analyzeSequence(Arrays.asList(expressions), resolveResult);
    }
    return resolveResult.getResultType();
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

    if (where instanceof TheROperatorExpression || where instanceof TheRPrefixExpression) {
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

    if (where instanceof TheRMemberExpression) {
      TheRMemberExpression memberExpression = (TheRMemberExpression) where;
      return analyze(memberExpression.getExpression(), parentResult);
    }
    if (where instanceof TheRForStatement) {
      TheRForStatement forStatement = (TheRForStatement)where;
      StaticAnalysisResult afterRange = analyze(forStatement.getRange(), parentResult);
      if (afterRange.isEnd()) {
        return afterRange;
      }
      StaticAnalysisResult withoutBody = afterRange.applyAssignInFor(forStatement.getTarget(), forStatement.getRange());
      if (withoutBody.isEnd()) {
        return withoutBody;
      }
      StaticAnalysisResult withBody = analyze(forStatement.getBody(), withoutBody);
      if (withBody.isEnd()) {
        return withBody;
      }
      return withoutBody.merge(withBody);
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
