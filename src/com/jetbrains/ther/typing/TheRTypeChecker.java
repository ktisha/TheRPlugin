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
    TheRFunctionType functionType = (TheRFunctionType)TheRTypeProvider.getType(functionExpression);
    assert functionType != null;
    matchArgs(arguments, functionExpression, matchedParams, matchedByTripleDot, functionType);
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
    if (isOptional && replacementType instanceof TheRNullType) {
      return true;
    }
    if (type instanceof TheRUnionType) {
      return ((TheRUnionType) type).contains(replacementType);
    }
    return type.equals(replacementType) || TheRTypeProvider.isSubtype(replacementType, type);
  }

  public static void matchArgs(List<TheRExpression> arguments,
                               TheRFunctionExpression function,
                               Map<TheRExpression, TheRParameter> matchedParams,
                               List<TheRExpression> matchedByTripleDot,
                               TheRFunctionType functionType) throws MatchingException {
    ArrayList<TheRParameter> formalArguments = new ArrayList<TheRParameter>(function.getParameterList().getParameterList());
    ArrayList<TheRExpression> suppliedArguments = new ArrayList<TheRExpression>(arguments);
    exactMatching(formalArguments, suppliedArguments, matchedParams);
    partialMatching(formalArguments, suppliedArguments, matchedParams);
    positionalMatching(formalArguments, suppliedArguments, matchedParams, matchedByTripleDot, functionType);
  }

  static void partialMatching(ArrayList<TheRParameter> formalArguments,
                              ArrayList<TheRExpression> suppliedArguments,
                              Map<TheRExpression, TheRParameter> matchedParams) throws MatchingException {
    matchParams(formalArguments, suppliedArguments, true, matchedParams);
  }

  static void exactMatching(ArrayList<TheRParameter> formalArguments,
                            ArrayList<TheRExpression> suppliedArguments,
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
    int tripleDotPosition = -1;
    for (int i = 0; i < formalArguments.size(); i++) {
      TheRParameter param = formalArguments.get(i);
      if (param.getText().equals("...")) {
        tripleDotPosition = i;
        break;
      }
      if (i >= suppliedSize) {
        break;
      }
      TheRExpression arg = suppliedArguments.get(i);
      matchedArguments.add(arg);
      matchedParameter.add(param);
      matchedParams.put(arg, param);
    }

    formalArguments.removeAll(matchedParameter);
    suppliedArguments.removeAll(matchedArguments);

    if (tripleDotPosition != -1) {
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
