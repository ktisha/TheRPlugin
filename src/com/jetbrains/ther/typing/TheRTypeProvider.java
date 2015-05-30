package com.jetbrains.ther.typing;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import com.jetbrains.ther.TheRPsiUtils;
import com.jetbrains.ther.TheRStaticAnalyzerHelper;
import com.jetbrains.ther.psi.api.*;
import com.jetbrains.ther.typing.types.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TheRTypeProvider {


  public static final String LIST = "list";

  public static TheRType getType(TheRPsiElement element) {
    return TheRTypeContext.getTypeFromCache(element);
  }

  /**
   * evaluates type of expression
   */
  public static TheRType buildType(PsiElement element) {
    if (element == null) {
      return TheRUnknownType.INSTANCE;
    }
    if (element instanceof TheRStringLiteralExpression) {
      return TheRCharacterType.INSTANCE;
    }
    if (element instanceof TheRNumericLiteralExpression) {
      return getNumericType((TheRNumericLiteralExpression)element);
    }
    if (element instanceof TheRLogicalLiteralExpression) {
      return TheRLogicalType.INSTANCE;
    }
    if (element instanceof TheRNullLiteralExpression) {
      return TheRNullType.INSTANCE;
    }
    if (element instanceof TheRNaLiteralExpression) {
      return ((TheRNaLiteralExpression)element).getType();
    }

    if (element instanceof TheRReferenceExpression) {
      return getReferenceExpressionType((TheRReferenceExpression)element);
    }

    if (element instanceof TheRAssignmentStatement) {
      TheRPsiElement assignedValue = ((TheRAssignmentStatement)element).getAssignedValue();
      if (assignedValue != null) {
        return getType(assignedValue);
      }
    }

    if (element instanceof TheRCallExpression) {
      return getCallExpressionType((TheRCallExpression)element);
    }

    if (element instanceof TheRFunctionExpression) {
      return new TheRFunctionType((TheRFunctionExpression)element);
    }
    if (element instanceof TheRSubscriptionExpression) {
      TheRSubscriptionExpression subscriptionExpression = (TheRSubscriptionExpression)element;
      boolean isSingleBracket = subscriptionExpression.getLbracket() != null;
      List<TheRExpression> expressionList = subscriptionExpression.getExpressionList();
      TheRExpression base = expressionList.get(0);
      if (base != null) {
        TheRType type = getType(base);
        return type.getSubscriptionType(expressionList.subList(1, expressionList.size()), isSingleBracket);
      }
    }
    if (element instanceof TheRBlockExpression) {
      TheRBlockExpression blockExpression = (TheRBlockExpression)element;
      List<TheRExpression> expressionList = blockExpression.getExpressionList();
      if (!expressionList.isEmpty()) {
        TheRExpression lastExpression = expressionList.get(expressionList.size() - 1);
        return TheRTypeProvider.getType(lastExpression);
      }
    }

    if (element instanceof  TheRIfStatement) {
      TheRIfStatement ifStatement = (TheRIfStatement)element;
      List<TheRExpression> expressions = ifStatement.getExpressionList();
      if (expressions.size() > 1) {
        Set<TheRType> types = new HashSet<TheRType>();
        for (int i = 1; i < expressions.size(); i++) {
          TheRType type = getType(expressions.get(i));
          if (!TheRUnknownType.class.isInstance(type)) {
            types.add(type);
          }
        }
        return TheRUnionType.create(types);
      }
    }

    if (element instanceof TheRMemberExpression) {
      TheRMemberExpression memberExpression = (TheRMemberExpression)element;
      TheRType elementType = getType(memberExpression.getExpression());
      return elementType.getMemberType(memberExpression.getTag());
    }
    if (element instanceof TheRSliceExpression) {
      TheRSliceExpression sliceExpression = ((TheRSliceExpression)element);
      for (TheRExpression expression : sliceExpression.getExpressionList()) {
        TheRType expressionType = getType(expression);
        if (!TheRUnknownType.class.isInstance(expressionType) && !TheRNumericType.class.isInstance(expressionType)) {
          return new TheRErrorType("Wrong arguments in slicing");
        }
      }
      return TheRNumericType.INSTANCE; // TODO: think!
    }
    if (element instanceof TheROperatorExpression) {
      return getBinaryExpressionType((TheROperatorExpression)element);
    }
    if (element instanceof TheRAtExpression) {
      TheRAtExpression atExpression = (TheRAtExpression)element;
      TheRExpression base = atExpression.getExpression();
      String tag = atExpression.getTag();
      TheRType baseType = getType(base);
      return baseType.getSlotType(tag);
    }
    return TheRUnknownType.INSTANCE;
  }

  private static TheRType getNumericType(TheRNumericLiteralExpression expression) {
    if (expression.getInteger() != null) {
      return TheRIntegerType.INSTANCE;
    }
    if (expression.getComplex() != null) {
      return TheRComplexType.INSTANCE;
    }
    return expression.getNumeric() != null ? TheRNumericType.INSTANCE : TheRUnknownType.INSTANCE;
  }

  private static TheRType getBinaryExpressionType(TheROperatorExpression expression) {
    TheRFunctionExpression function = TheRPsiUtils.getFunction(expression);
    if (function == null) {
      return TheRUnknownType.INSTANCE;
    }
    TheRType functionType = getType(function);
    if (!TheRFunctionType.class.isInstance(functionType)) {
      return TheRUnknownType.INSTANCE; // TODO: Error?
    }
    List<TheRExpression> arguments = PsiTreeUtil.getChildrenOfTypeAsList(expression, TheRExpression.class);
    return getFunctionCallReturnType((TheRFunctionType)functionType, arguments);
  }

  private static TheRType getCallExpressionType(TheRCallExpression element) {
    String functionName = element.getExpression().getName();
    if (LIST.equals(functionName)) {
      return getNewListType(element.getArgumentList().getExpressionList());
    }
    if ("setClass".equals(functionName)) {
      return getClassConstructor(element);
    }
    if ("new".equals(functionName)) {
      return getTypeFromNew(element);
    }

    TheRFunctionExpression function = TheRPsiUtils.getFunction(element);
    final TheRFunctionType functionType;
    if (function != null) {
      functionType = new TheRFunctionType(function);
    } else {
      TheRType type = TheRTypeProvider.getType(element.getExpression());
      if (!TheRFunctionType.class.isInstance(type)) {
        return TheRUnknownType.INSTANCE;
      }
      functionType = (TheRFunctionType)type;
    }

    return getFunctionCallReturnType(functionType, element.getArgumentList().getExpressionList());
  }

  private static TheRType getClassConstructor(TheRCallExpression element) {
    TheRS4ClassType s4Class = TheRS4ClassType.createFromSetClass(element);
    if (s4Class != null) {
      // TODO : check S4 params
      return new TheRFunctionType(s4Class);
    }
    return TheRUnknownType.INSTANCE;
  }

  private static TheRType getTypeFromNew(TheRCallExpression callExpression) {
    String name = null;
    TheRExpression classExpression = TheRPsiUtils.findParameterValue("Class", callExpression);
    if (classExpression instanceof TheRStringLiteralExpression) {
        name = classExpression.getText().substring(1, classExpression.getText().length() - 1);
    }
    if (name == null) {
      return TheRUnknownType.INSTANCE;
    }
    TheRType atomicType = findTypeByName(name);
    if (atomicType != null) {
      return atomicType;
    }
    if ("list".equals(name)) {
      List<TheRExpression> arguments = callExpression.getArgumentList().getExpressionList();
      arguments.remove(classExpression);
      return getNewListType(arguments);
    }

    TheRS4ClassType s4Class = TheRS4ClassType.byName(callExpression.getProject(), name);
    if (s4Class == null) {
      return TheRUnknownType.INSTANCE;
    }
    for (TheRExpression argument : callExpression.getArgumentList().getExpressionList()) {
      if (argument instanceof TheRAssignmentStatement) {
        PsiElement assignee = ((TheRAssignmentStatement)argument).getAssignee();
        TheRPsiElement value = ((TheRAssignmentStatement)argument).getAssignedValue();
        if (assignee instanceof TheRReferenceExpression) {
          String slotName = assignee.getText();
          if (!s4Class.hasSlot(slotName)) {
            return new TheRErrorType("Invalid slot " + slotName);
          }
          if (!TheRTypeChecker.matchTypes(s4Class.getSlotType(slotName), getType(value))) {
            return new TheRErrorType("Wrong type of slot " + slotName);
          }
        }
      }
    }
    return s4Class;
  }

  private static TheRType getFunctionCallReturnType(TheRFunctionType functionType, List<TheRExpression> arguments) {
    // step 1: check @return
    if (functionType.getReturnType() != null) {
      return functionType.getReturnType();
    }

    Map<TheRExpression, TheRParameter> matchedParams = new HashMap<TheRExpression, TheRParameter>();
    List<TheRExpression> matchedByTripleDot = new ArrayList<TheRExpression>();

    try {
      TheRTypeChecker.matchArgs(arguments, matchedParams, matchedByTripleDot, functionType);
    }
    catch (MatchingException e) {
      return TheRUnknownType.INSTANCE;
    }

    Map<String, TheRParameterConfiguration> paramToSuppliedConfiguration =
      new HashMap<String, TheRParameterConfiguration>();

    // step 2: check @type annotation
    if (!isMatchedTypes(functionType, matchedParams, matchedByTripleDot, paramToSuppliedConfiguration)) {
      return TheRUnknownType.INSTANCE;
    }

    //step 3: check @rule
    return tryApplyRule(functionType, paramToSuppliedConfiguration);
  }

  private static TheRType getNewListType(List<TheRExpression> arguments) {
    TheRListType list = new TheRListType();
    for (TheRExpression arg : arguments) {
      if (arg instanceof TheRAssignmentStatement) {
        TheRAssignmentStatement assignment = (TheRAssignmentStatement)arg;
        list.addField(assignment.getAssignee().getText(), getType(assignment.getAssignedValue()));
      } else {
        list.addField(getType(arg));
      }
    }
    return list;
  }

  private static boolean isMatchedTypes(TheRFunctionType functionType,
                                        Map<TheRExpression, TheRParameter> matchedParams,
                                        List<TheRExpression> matchedByTripleDot,
                                        Map<String, TheRParameterConfiguration> paramToSuppliedConfiguration) {
    for (Map.Entry<TheRExpression, TheRParameter> entry : matchedParams.entrySet()) {
      TheRExpression expr = entry.getKey();
      TheRParameter parameter = entry.getValue();

      if (expr instanceof TheRAssignmentStatement) {
        expr = (TheRExpression)((TheRAssignmentStatement)expr).getAssignedValue();
      }

      TheRType exprType = getType(expr);
      String paramName = parameter.getName();
      paramToSuppliedConfiguration.put(paramName, new TheRParameterConfiguration(exprType, expr));
      TheRType paramType = functionType.getParameterType(paramName);

      if (paramType != null && !paramType.equals(exprType)) {
        // can't match
        return false;
      }
    }

    if (!matchedByTripleDot.isEmpty()) {
      List<TheRType> types = new ArrayList<TheRType>();
      for (TheRExpression expr : matchedByTripleDot) {
        types.add(getType(expr));
      }
      paramToSuppliedConfiguration.put("...", new TheRParameterConfiguration(new TheRTypeSequence(types), null));
    }

    return true;
  }

  public static TheRType getParamType(TheRParameter parameter, TheRFunctionType functionType) {
    TheRType type = functionType.getParameterType(parameter.getName());
    if (type != null) {
      return type;
    }
    return TheRUnknownType.INSTANCE;
  }

  private static TheRType tryApplyRule(TheRFunctionType functionType,
                                       Map<String, TheRParameterConfiguration> paramToSuppliedConfiguration) {
    rulefor:
    for (TheRFunctionRule rule : functionType.getRules()) {
      TheRTypeEnvironment env = new TheRTypeEnvironment();
      for (Map.Entry<String, TheRParameterConfiguration> entry : rule.getParameters().entrySet()) {
        String paramName = entry.getKey();
        TheRParameterConfiguration conf = entry.getValue();
        TheRParameterConfiguration exprConf = paramToSuppliedConfiguration.get(paramName);
        if (exprConf == null) {
          continue rulefor;
        }
        TheRType ruleType = conf.getType();
        if (ruleType != null) {
          if (ruleType instanceof TheRTypeVariable) {
            TheRTypeVariable typeVariable = (TheRTypeVariable)ruleType;
            String variableName = typeVariable.getName();
            if (!env.contains(variableName)) {
              env.addType(variableName, exprConf.getType());
            }
            ruleType = env.getType(variableName);
          }
          if (!TheRTypeChecker.matchTypes(ruleType, exprConf.getType())) {
            continue rulefor;
          }
        }
        TheRExpression ruleValue = conf.getValue();
        if (ruleValue != null) {
          TheRExpression exprValue = exprConf.getValue();

          // TODO : evaluate expressions?
          if (exprValue == null || !matchExpressions(exprValue, ruleValue)) {
            continue rulefor;
          }
        }
      }
      return rule.getReturnType().resolveType(env);
    }
    return TheRUnknownType.INSTANCE;
  }

  private static boolean matchExpressions(TheRExpression substitution, TheRExpression base) {
    if (base instanceof TheRReferenceExpression) {
      String name = base.getName();
      if ("string".equals(name) && substitution instanceof TheRStringLiteralExpression) {
        return true;
      }
      if ("number".equals(name) && substitution instanceof TheRNumericLiteralExpression) {
        return true;
      }
    }
    return substitution.getText().equals(base.getText());
  }


  @Nullable
  private static TheRType guessTypeFromFunctionBody(final TheRParameter parameter) {
    final TheRType[] type = new TheRType[1];
    type[0] = null;
    TheRFunctionExpression function = TheRPsiUtils.getFunction(parameter);
    if (function == null) {
      return null;
    }
    final TheRBlockExpression blockExpression = PsiTreeUtil.getChildOfType(function, TheRBlockExpression.class);
    if (blockExpression == null) {
      return null;
    }
    Query<PsiReference> references = ReferencesSearch.search(parameter);
    references.forEach(new Processor<PsiReference>() {
      @Override
      public boolean process(PsiReference reference) {
        PsiElement element = reference.getElement();
        PsiElement parent = element.getParent();
        //TODO: check operations more strict
        //TODO: check control flow analysis
        if (parent instanceof TheROperatorExpression) {
          if (PsiTreeUtil.isAncestor(blockExpression, element, false)) {
            type[0] = TheRNumericType.INSTANCE;
            return false;
          }
        }
        return true;
      }
    });
    return type[0];
  }

  @Nullable
  public static TheRType findTypeByName(String typeName) {
    if (typeName.equals("numeric")) {
      return TheRNumericType.INSTANCE;
    }
    if (typeName.equals("character")) {
      return TheRCharacterType.INSTANCE;
    }
    if (typeName.equals("logical")) {
      return TheRLogicalType.INSTANCE;
    }
    if (typeName.equals("complex")) {
      return TheRComplexType.INSTANCE;
    }
    if (typeName.equals("integer")) {
      return TheRIntegerType.INSTANCE;
    }
    if (typeName.equals("null")) {
      return TheRNullType.INSTANCE;
    }
    if (typeName.equals("raw")) {
      return TheRRawType.INSTANCE;
    }
    //we need to return null in this case to create type variable
    return null;
  }

  private static TheRType getReferenceExpressionType(TheRReferenceExpression expression) {
    PsiReference reference = expression.getReference();
    if (reference == null) {
      return TheRUnknownType.INSTANCE;
    }
    return TheRStaticAnalyzerHelper.getReferenceType(expression);
  }

  public static TheRType guessReturnValueTypeFromBody(TheRFunctionExpression functionExpression) {
    TheRExpression expression = functionExpression.getExpression();
    if (expression == null) {
      return TheRUnknownType.INSTANCE;
    }
    Set<TheRType> types = new HashSet<TheRType>();
    TheRType type = getType(expression);
    if (!TheRUnknownType.class.isInstance(type)) {
      types.add(type);
    }
    collectReturnTypes(functionExpression, types);
    return TheRUnionType.create(types);
  }

  private static void collectReturnTypes(TheRFunctionExpression functionExpression, Set<TheRType> types) {
    TheRCallExpression[] calls = TheRPsiUtils.getAllChildrenOfType(functionExpression, TheRCallExpression.class);
    for (TheRCallExpression callExpression : calls) {
      if (TheRPsiUtils.isReturn(callExpression)) {
        List<TheRExpression> args = callExpression.getArgumentList().getExpressionList();
        if (args.size() == 1) {
          TheRType rType = TheRTypeProvider.getType(args.iterator().next());
          if (rType != null && !TheRUnknownType.class.isInstance(rType)) {
            types.add(rType);
          }
        }
      }
    }
  }

  public static boolean isSubtype(TheRType subType, TheRType type) {
    return type.getClass().isAssignableFrom(subType.getClass());
  }
}
