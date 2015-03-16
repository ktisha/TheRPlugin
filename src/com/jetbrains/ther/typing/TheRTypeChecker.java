package com.jetbrains.ther.typing;

import com.jetbrains.ther.psi.api.TheRAssignmentStatement;
import com.jetbrains.ther.psi.api.TheRExpression;
import com.jetbrains.ther.psi.api.TheRParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TheRTypeChecker {

  public static void matchTypes(List<TheRParameter> parameters, List<TheRExpression> arguments) throws MatchingException {
    ArrayList<TheRParameter> formalArguments = new ArrayList<TheRParameter>(parameters);
    ArrayList<TheRExpression> suppliedArguments = new ArrayList<TheRExpression>(arguments);
    Map<TheRExpression, TheRParameter> matchedParams = new HashMap<TheRExpression, TheRParameter>();

    exactMatching(formalArguments, suppliedArguments, matchedParams);

    partialMatching(formalArguments, suppliedArguments, matchedParams);

    //TODO: extract method
    int i = 0;
    //TODO: rename to positionalMatched
    List<TheRExpression> matchedArguments = new ArrayList<TheRExpression>();
    for (TheRParameter param : formalArguments) {
      if (i >= suppliedArguments.size()) {
        throw new MatchingException(generateMissingArgErrorMessage(formalArguments, i));
      }
      TheRExpression arg = suppliedArguments.get(i);
      matchedArguments.add(arg);
      matchedParams.put(arg, param);
      i++;
    }
    if (i != suppliedArguments.size() && !containsTripleDot(formalArguments)) {
      for (TheRExpression expression : matchedArguments) {
        suppliedArguments.remove(expression);
      }
    }

    checkUnmatchedArgs(suppliedArguments);

    for (Map.Entry<TheRExpression, TheRParameter> entry : matchedParams.entrySet()) {
      TheRParameter parameter = entry.getValue();
      TheRType paramType = TheRTypeProvider.getParamType(parameter);
      if (paramType == null || paramType.equals(TheRType.UNKNOWN)) {
        continue;
      }
      TheRType argType = TheRTypeProvider.getType(entry.getKey());
      if (argType != null && !argType.equals(TheRType.UNKNOWN)) {
        if (!argType.getName().equals(paramType.getName())) {
          throw new MatchingException(parameter.getText() + " expected to be of type " + paramType.getName() +
                                      ", found type " + argType.getName());
        }
      }
    }
  }

  private static boolean containsTripleDot(ArrayList<TheRParameter> formalArguments) {
    for (TheRParameter parameter: formalArguments) {
      if (parameter.getText().equals("...")) {
        return true;
      }
    }
    return false;
  }

  private static void partialMatching(ArrayList<TheRParameter> formalArguments,
                                      ArrayList<TheRExpression> suppliedArguments,
                                      Map<TheRExpression, TheRParameter> matchedParams) throws MatchingException {
    matchParams(formalArguments, suppliedArguments, true, matchedParams);
  }

  private static void exactMatching(ArrayList<TheRParameter> formalArguments,
                                    ArrayList<TheRExpression> suppliedArguments,
                                    Map<TheRExpression, TheRParameter> matchedParams) throws MatchingException {
    matchParams(formalArguments, suppliedArguments, false, matchedParams);
  }

  private static String generateMissingArgErrorMessage(ArrayList<TheRParameter> parameters, int i) {
    String noDefaultMessage = " missing, with no default";
    if (i == parameters.size() - 1) {
      return "argument \'" + parameters.get(i).getText() + "\" is" + noDefaultMessage;
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
