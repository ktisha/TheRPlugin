package com.jetbrains.ther.typing;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import com.jetbrains.ther.TheRPsiUtils;
import com.jetbrains.ther.psi.api.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TheRTypeProvider {

  //TODO: get type from context

  /**
   * evaluates type of expression
   */
  public static TheRType getType(PsiElement element) {
    if (element == null) {
      return TheRType.UNKNOWN;
    }
    if (element instanceof TheRStringLiteralExpression) {
      return TheRCharacterType.INSTANCE;
    }
    if (element instanceof TheRNumericLiteralExpression) {
      return TheRNumericType.INSTANCE;
    }
    if (element instanceof TheRLogicalLiteralExpression) {
      return TheRLogicalType.INSTANCE;
    }
    //TODO:complete this logic by all the rules
    if (element instanceof TheRReferenceExpression) {
      PsiReference reference = element.getReference();
      if (reference == null) {
        return TheRType.UNKNOWN;
      }
      PsiElement resolve = reference.resolve();
      if (resolve != null && resolve instanceof TheRAssignmentStatement) {
        TheRAssignmentStatement assignmentStatement = (TheRAssignmentStatement)resolve;
        TheRPsiElement assignedValue = assignmentStatement.getAssignedValue();
        if (assignedValue != null) {
          return getType(assignedValue);
        }
      }
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

    if (element instanceof  TheRFunctionExpression) {
      return new TheRFunctionType((TheRFunctionExpression)element);
    }
    return TheRType.UNKNOWN;
  }

  private static TheRType getCallExpressionType(TheRCallExpression element) {
    // TODO: Move matching to separate method
    TheRFunctionExpression function = TheRPsiUtils.getFunction(element);
    List<TheRExpression> arguments = element.getArgumentList().getExpressionList();
    List<TheRParameter> parameters = function.getParameterList().getParameterList();
    ArrayList<TheRParameter> formalArguments = new ArrayList<TheRParameter>(parameters);
    ArrayList<TheRExpression> suppliedArguments = new ArrayList<TheRExpression>(arguments);
    Map<TheRExpression, TheRParameter> matchedParams = new HashMap<TheRExpression, TheRParameter>();
    List<TheRExpression> matchedByTripleDot = new ArrayList<TheRExpression>();

    try {
      TheRTypeChecker.exactMatching(formalArguments, suppliedArguments, matchedParams);
      TheRTypeChecker.partialMatching(formalArguments, suppliedArguments, matchedParams);
      TheRTypeChecker.positionalMatching(formalArguments, suppliedArguments, matchedParams, matchedByTripleDot);

      TheRFunctionType functionType = new TheRFunctionType(function);

      // step 1: check @type
      Map<String, TheRParameterConfiguration> paramToType =
        new HashMap<String, TheRParameterConfiguration>();
      for (Map.Entry<TheRExpression, TheRParameter> entry : matchedParams.entrySet()) {
        TheRExpression expr = entry.getKey();
        if (expr instanceof TheRAssignmentStatement) {
          expr = (TheRExpression)((TheRAssignmentStatement)expr).getAssignedValue();
        }
        TheRType exprType = getType(expr);
        String param = entry.getValue().getName();
        paramToType.put(param, new TheRParameterConfiguration(exprType, expr));
        TheRType paramType = functionType.getParameterType(param);
        // TODO : equals
        if (paramType != null && !paramType.getName().equals(exprType.getName())) {
          // can't match
          return TheRType.UNKNOWN;
        }
      }
      if (!matchedByTripleDot.isEmpty()) {
        List<TheRType> types = new ArrayList<TheRType>();
        for (TheRExpression expr : matchedByTripleDot) {
          types.add(getType(expr));
        }
        paramToType.put("...", new TheRParameterConfiguration(new TheRTypeSequence(types), null));
      }

      // step 2: check @return
      if (functionType.getReturnType() != null) {
        return functionType.getReturnType();
      }

      //step 3: check @rule
      rulefor:
      for (TheRFunctionRule rule : functionType.getRules()) {
        TheRTypeEnvironment env = new TheRTypeEnvironment();
        for (Map.Entry<String, TheRParameterConfiguration> entry : rule.getParameters().entrySet()) {
          String param = entry.getKey();
          TheRParameterConfiguration conf = entry.getValue();
          TheRParameterConfiguration exprConf = paramToType.get(param);

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
            // equals
            if (!ruleType.getName().equals(exprConf.getType().getName())) {
              continue rulefor;
            }
          }
          TheRExpression ruleValue = conf.getValue();
          if (ruleValue != null) {
            TheRExpression exprValue = exprConf.getValue();
            // TODO : equals
            if (exprValue == null || !exprValue.getText().equals(ruleValue.getText())) {
              continue rulefor;
            }
          }
        }
        return rule.getReturnType().resolveType(env);
      }
    }
    catch (MatchingException e) {
      return TheRType.UNKNOWN;
    }
    return TheRType.UNKNOWN;
  }

  private static boolean isLogicalLiteral(PsiElement element) {
    String elementText = element.getText();
    return elementText.equals("TRUE") || elementText.equals("T") || elementText.equals("F") ||
           elementText.equals("FALSE");
  }

  //TODO: pass parameter list and parse each line only once not for each parameter
  public static TheRType getParamType(TheRParameter parameter, TheRFunctionType functionType) {
    TheRType type = functionType.getParameterType(parameter.getName());
    if (type != null) {
      return type;
    }
    type = guessTypeFromFunctionBody(parameter);
    if (type != null) {
      return type;
    }
    return TheRType.UNKNOWN;
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
        if (parent instanceof TheRBinaryExpression) {
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

  //TODO:rewrite this normally
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
    return null;
  }
}
