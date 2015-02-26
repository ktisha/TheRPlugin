package com.jetbrains.ther.typing;

import com.jetbrains.ther.psi.api.TheRAssignmentStatement;
import com.jetbrains.ther.psi.api.TheRExpression;
import com.jetbrains.ther.psi.api.TheRParameter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TheRTypeChecker {

  /**
   * @param parameters formal arguments of function
   * @param arguments  supplied arguments of function
   * @return null if everything is OK and error message otherwise
   */
  @Nullable
  public static String matchTypes(List<TheRParameter> parameters, List<TheRExpression> arguments) {
    ArrayList<TheRParameter> formalArguments = new ArrayList<TheRParameter>(parameters);
    ArrayList<TheRExpression> suppliedArguments = new ArrayList<TheRExpression>(arguments);
    Map<TheRExpression, TheRParameter> matchedParams = new HashMap<TheRExpression, TheRParameter>();

    //exact matching
    String message = matchParams(formalArguments, suppliedArguments, false, matchedParams);
    if (message != null) {
      return message;
    }

    //partial matching
    message = matchParams(formalArguments, suppliedArguments, true, matchedParams);
    if (message != null) {
      return message;
    }

    message = checkUnmatchedArgs(getNamedArguments(suppliedArguments));
    if (message != null) {
      return message;
    }

    //TODO:check for triple dot
    int i = 0;
    List<TheRExpression> matchedArguments = new ArrayList<TheRExpression>();
    for (TheRParameter param : formalArguments) {
      if (i >= suppliedArguments.size()) {
        return generateMissingArgErrorMessage(formalArguments, i);
      }
      TheRExpression arg = suppliedArguments.get(i);
      matchedArguments.add(arg);
      matchedParams.put(arg, param);
      i++;
    }
    if (i != suppliedArguments.size()) {
      for (TheRExpression expression : matchedArguments) {
        suppliedArguments.remove(expression);
      }
      message = checkUnmatchedArgs(suppliedArguments);
      if (message != null) {
        return message;
      }
    }

    for (Map.Entry<TheRExpression, TheRParameter> entry : matchedParams.entrySet()) {
      TheRType paramType = TheRTypeProvider.getParamType(entry.getValue());
      if (paramType == null || paramType.equals(TheRType.UNKNOWN)) {
        continue;
      }
      TheRType argType = TheRTypeProvider.getType(entry.getKey());
      if (argType != null && !argType.equals(TheRType.UNKNOWN)) {
        if (!argType.getName().equals(paramType.getName())) {
          return "types mismatch";
        }
      }
    }
    return null;
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

  //TODO:check for "..." in partial matching
  private static List<TheRParameter> getMatches(String name, List<TheRParameter> parameters, boolean usePartial) {
    List<TheRParameter> matches = new ArrayList<TheRParameter>();
    for (TheRParameter param : parameters) {
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

  /**
   * @param parameters
   * @param arguments
   * @param usePartialMatching
   * @return error message or null if everything is OK
   */
  private static String matchParams(List<TheRParameter> parameters, List<TheRExpression> arguments,
                                    boolean usePartialMatching,
                                    Map<TheRExpression, TheRParameter> matchedParams) {
    List<TheRExpression> namedArguments = getNamedArguments(arguments);
    for (TheRExpression namedArg : namedArguments) {
      String name = namedArg.getName();
      List<TheRParameter> matches = getMatches(name, parameters, usePartialMatching);
      if (matches.size() > 1) {
        return "formal argument " + name + " matched by multiply actual arguments";
      }
      if (matches.size() == 1) {
        matchedParams.put(namedArg, matches.get(0));
      }
    }

    for (Map.Entry<TheRExpression, TheRParameter> entry : matchedParams.entrySet()) {
      arguments.remove(entry.getKey());
      parameters.remove(entry.getValue());
    }
    return null;
  }

  /**
   *
   * @param arguments
   * @return error message or null if everything is OK
   */
  private static String checkUnmatchedArgs(List<TheRExpression> arguments) {
    int size = arguments.size();
    if (size > 0) {
      if (size == 1) {
        return "unused argument " + arguments.get(0).getText();
      }
      StringBuilder errorMessage = new StringBuilder("unused arguments: ");
      for (TheRExpression expression : arguments) {
        errorMessage.append(expression.getText()).append(", ");
      }
      int lastComma = errorMessage.lastIndexOf(",");
      return errorMessage.delete(lastComma, lastComma + 1).toString();
    }
    return null;
  }
}
