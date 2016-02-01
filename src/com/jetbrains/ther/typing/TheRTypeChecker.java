package com.jetbrains.ther.typing;

import com.jetbrains.ther.psi.api.TheRAssignmentStatement;
import com.jetbrains.ther.psi.api.TheRExpression;
import com.jetbrains.ther.psi.api.TheRFunctionExpression;
import com.jetbrains.ther.psi.api.TheRParameter;
import com.jetbrains.ther.typing.types.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TheRTypeChecker {

  public static void checkTypes(List<TheRExpression> arguments, TheRFunctionExpression functionExpression) throws MatchingException {
    Map<TheRExpression, TheRParameter> matchedParams = new HashMap<TheRExpression, TheRParameter>();
    List<TheRExpression> matchedByTripleDot = new ArrayList<TheRExpression>();
    TheRType type = TheRTypeProvider.getType(functionExpression);
    if (!TheRFunctionType.class.isInstance(type)) {
      return; // TODO: fix me properly
    }
    TheRFunctionType functionType = (TheRFunctionType)type;
    matchArgs(arguments, matchedParams, matchedByTripleDot, functionType);
    for (Map.Entry<TheRExpression, TheRParameter> entry : matchedParams.entrySet()) {
      TheRParameter parameter = entry.getValue();
      TheRType paramType = TheRTypeProvider.getParamType(parameter, functionType);
      if (paramType == null || paramType instanceof TheRUnknownType) {
        continue;
      }
      boolean isOptional = functionType.isOptional(parameter.getName());
      TheRType argType = TheRTypeProvider.getType(entry.getKey());
      if (argType != null && !TheRUnknownType.class.isInstance(argType)) {
        if (!matchTypes(paramType, argType, isOptional)) {
          throw new MatchingException(parameter.getText() + " expected to be of type " + paramType +
                                      ", found type " + argType);
        }
      }
    }
  }

  public static boolean matchTypes(TheRType type, TheRType replacementType) {
    return matchTypes(type, replacementType, false);
  }

  public static boolean matchTypes(TheRType type, TheRType replacementType, boolean isOptional) {
    if (replacementType instanceof TheRUnknownType) {
      return true;
    }
    if (type instanceof TheRUnknownType) {
      return true;
    }
    if (isOptional && replacementType instanceof TheRNullType) {
      return true;
    }
    if (type instanceof TheRUnionType) {
      for (TheRType t : ((TheRUnionType)type).getTypes()) {
        if (matchTypes(t, replacementType)) {
          return true;
        }
      }
      return false;
    }
    if (replacementType instanceof TheRUnionType) {
      for (TheRType t : ((TheRUnionType)replacementType).getTypes()) {
        if (!matchTypes(type, t)) {
          return false;
        }
      }
      return true;
    }
    if (replacementType instanceof TheRS4ClassType) {
      TheRType superClass = ((TheRS4ClassType)replacementType).getSuperClass();
      if (superClass != null && matchTypes(type, superClass)) {
        return true;
      }
    }
    if (type instanceof TheRListType) {
      if (!TheRListType.class.isInstance(replacementType)) {
        return false;
      }
      TheRListType listType = (TheRListType)type;
      TheRListType replacementList = (TheRListType)replacementType;
      for (String field : listType.getFields()) {
        if (!replacementList.hasField(field)) {
          return false;
        }
        if (!matchTypes(listType.getFieldType(field), replacementList.getFieldType(field))) {
          return false;
        }
      }
      return true;
    }
    if (replacementType instanceof TheRNumericType && type instanceof TheRIntegerType) {
      return true; // yeah yeah
    }
    return type.equals(replacementType) || TheRTypeProvider.isSubtype(replacementType, type);
  }

  public static void matchArgs(List<TheRExpression> arguments,
                               Map<TheRExpression, TheRParameter> matchedParams,
                               List<TheRExpression> matchedByTripleDot,
                               TheRFunctionType functionType) throws MatchingException {
    List<TheRParameter> formalArguments = functionType.getFormalArguments();
    List<TheRExpression> suppliedArguments = new ArrayList<TheRExpression>(arguments);
    exactMatching(formalArguments, suppliedArguments, matchedParams);
    partialMatching(formalArguments, suppliedArguments, matchedParams);
    positionalMatching(formalArguments, suppliedArguments, matchedParams, matchedByTripleDot, functionType);
  }

  static void partialMatching(List<TheRParameter> formalArguments,
                              List<TheRExpression> suppliedArguments,
                              Map<TheRExpression, TheRParameter> matchedParams) throws MatchingException {
    matchParams(formalArguments, suppliedArguments, true, matchedParams);
  }

  static void exactMatching(List<TheRParameter> formalArguments,
                            List<TheRExpression> suppliedArguments,
                            Map<TheRExpression, TheRParameter> matchedParams) throws MatchingException {
    matchParams(formalArguments, suppliedArguments, false, matchedParams);
  }

  static void positionalMatching(List<TheRParameter> formalArguments,
                                 List<TheRExpression> suppliedArguments,
                                 Map<TheRExpression, TheRParameter> matchedParams,
                                 List<TheRExpression> matchedByTripleDot,
                                 TheRFunctionType functionType) throws MatchingException {
    List<TheRExpression> matchedArguments = new ArrayList<TheRExpression>();
    List<TheRParameter> matchedParameter = new ArrayList<TheRParameter>();
    int suppliedSize = suppliedArguments.size();
    boolean wasTripleDot = false;
    for (int i = 0; i < formalArguments.size(); i++) {
      TheRParameter param = formalArguments.get(i);
      if (param.getText().equals("...")) {
        wasTripleDot = true;
        break;
      }
      if (i >= suppliedSize) {
        break;
      }
      TheRExpression arg = suppliedArguments.get(i);
      if (arg instanceof TheRAssignmentStatement && ((TheRAssignmentStatement)arg).isEqual()) {
        String argName = ((TheRAssignmentStatement)arg).getAssignee().getText();
        if (!argName.equals(param.getName())) {
          wasTripleDot = true;
          break;
        }
      }
      matchedArguments.add(arg);
      matchedParameter.add(param);
      matchedParams.put(arg, param);
    }

    formalArguments.removeAll(matchedParameter);
    suppliedArguments.removeAll(matchedArguments);

    if (wasTripleDot) {
      matchedByTripleDot.addAll(suppliedArguments);
      suppliedArguments.clear();
    }

    List<TheRParameter> unmatched = new ArrayList<TheRParameter>();
    for (TheRParameter parameter : formalArguments) {
      if (parameter.getText().equals("...")) {
        continue;
      }
      TheRExpression defaultValue = parameter.getExpression();
      if (defaultValue != null) {
        matchedParams.put(defaultValue, parameter);
      } else {
        unmatched.add(parameter);
      }
    }
    if (!unmatched.isEmpty()) {
      unmatched.removeAll(functionType.getOptionalParams());
      if (!unmatched.isEmpty()) {
        throw new MatchingException(generateMissingArgErrorMessage(unmatched, 0));
      }
    }

    if (!suppliedArguments.isEmpty()) {
      checkUnmatchedArgs(suppliedArguments);
    }
  }

  private static String generateMissingArgErrorMessage(List<TheRParameter> parameters, int i) {
    String noDefaultMessage = " missing, with no default";
    if (i == parameters.size() - 1) {
      return "argument \'" + parameters.get(i).getText() + "\' is" + noDefaultMessage;
    }
    StringBuilder stringBuilder = new StringBuilder("arguments ");
    while (i < parameters.size()) {
      stringBuilder.append("\"").append(parameters.get(i).getText()).append("\"").append(", ");
      i++;
    }
    int length = stringBuilder.length();
    return stringBuilder.delete(length - 2, length - 1).append("are").append(noDefaultMessage).toString();
  }

  private static List<TheRParameter> getMatches(String name, List<TheRParameter> parameters, boolean usePartial) {
    List<TheRParameter> matches = new ArrayList<TheRParameter>();
    for (TheRParameter param : parameters) {
      if (usePartial && param.getText().equals("...")) {
        return matches;
      }
      String paramName = param.getName();
      if (paramName != null) {
        if (usePartial) {
          if (paramName.startsWith(name)) {
            matches.add(param);
          }
        }
        else {
          if (paramName.equals(name)) {
            matches.add(param);
          }
        }
      }
    }
    return matches;
  }

  private static List<TheRExpression> getNamedArguments(List<TheRExpression> arguments) {
    List<TheRExpression> namedArgs = new ArrayList<TheRExpression>();
    for (TheRExpression arg : arguments) {
      if (arg instanceof TheRAssignmentStatement && arg.getName() != null) {
        namedArgs.add(arg);
      }
    }
    return namedArgs;
  }

  private static void matchParams(List<TheRParameter> parameters, List<TheRExpression> arguments,
                                  boolean usePartialMatching,
                                  Map<TheRExpression, TheRParameter> matchedParams) throws MatchingException {
    List<TheRExpression> namedArguments = getNamedArguments(arguments);
    for (TheRExpression namedArg : namedArguments) {
      String name = namedArg.getName();
      List<TheRParameter> matches = getMatches(name, parameters, usePartialMatching);
      if (matches.size() > 1) {
        throw new MatchingException("formal argument " + name + " matched by multiply actual arguments");
      }
      if (matches.size() == 1) {
        matchedParams.put(namedArg, matches.get(0));
      }
    }

    for (Map.Entry<TheRExpression, TheRParameter> entry : matchedParams.entrySet()) {
      arguments.remove(entry.getKey());
      parameters.remove(entry.getValue());
    }
  }

  private static void checkUnmatchedArgs(List<TheRExpression> arguments) throws MatchingException {
    int size = arguments.size();
    if (size == 1) {
      throw new MatchingException("unused argument " + arguments.get(0).getText());
    }
    if (size > 0) {
      StringBuilder errorMessage = new StringBuilder("unused arguments: ");
      for (TheRExpression expression : arguments) {
        errorMessage.append(expression.getText()).append(", ");
      }
      int lastComma = errorMessage.lastIndexOf(",");
      throw new MatchingException(errorMessage.delete(lastComma, lastComma + 1).toString());
    }
  }
}
