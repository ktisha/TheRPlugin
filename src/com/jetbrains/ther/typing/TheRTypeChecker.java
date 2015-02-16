package com.jetbrains.ther.typing;

import com.jetbrains.ther.psi.api.TheRExpression;
import com.jetbrains.ther.psi.api.TheRParameter;

import java.util.List;

public class TheRTypeChecker {

  //TODO: generate some good message
  //TODO: code normal algo from language definition
  public static boolean matchTypes(List<TheRParameter> parameters, List<TheRExpression> arguments) {
    int i = 0;
    for (TheRParameter parameter : parameters) {
      TheRType paramType = TheRTypeProvider.getParamType(parameter);
      if (paramType == null || paramType.equals(TheRType.UNKNOWN)) {
        continue;
      }
      if (i < arguments.size()) {
        TheRType argType = TheRTypeProvider.getType(arguments.get(i));
        if (argType !=null && !argType.equals(TheRType.UNKNOWN)) {
         if (!argType.getName().equals(paramType.getName())) {
            return false;
          }
        }
      }
      i++;
    }
    return true;
  }
}
