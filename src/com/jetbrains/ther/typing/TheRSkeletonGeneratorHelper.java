package com.jetbrains.ther.typing;

import com.jetbrains.ther.TheRHelp;
import com.jetbrains.ther.psi.api.TheRFunctionExpression;
import com.jetbrains.ther.psi.api.TheRParameter;
import com.jetbrains.ther.typing.types.*;

import java.util.*;

public class TheRSkeletonGeneratorHelper {
  public static final Map<String, TheRType> TYPES = new HashMap<String, TheRType>();
  static {
    TYPES.put("logical", TheRLogicalType.INSTANCE);
    TYPES.put("complex", TheRComplexType.INSTANCE);
    TYPES.put("numeric", TheRNumericType.INSTANCE);
    TYPES.put("integer", TheRIntegerType.INSTANCE);
    TYPES.put("number", TheRComplexType.INSTANCE);
    TYPES.put("raw", TheRRawType.INSTANCE);
    TYPES.put("string", TheRCharacterType.INSTANCE);
    TYPES.put("character", TheRCharacterType.INSTANCE);
    TYPES.put("name", TheRCharacterType.INSTANCE);
  }

  public static void parseArgumentsDescription(String description, List<TheRParameter> parameters, Map<TheRParameter, TheRType> parsedTypes) {
    Map<TheRParameter, String> argsDesc = new HashMap<TheRParameter, String>();
    String[] argTexts = description.split("\n\n");
    for (String argText : argTexts) {
      String[] split = argText.split(":", 2);
      if (split.length < 2) {
        continue;
      }
      String arguments = split[0];
      String[] argNames = arguments.split(",");
      for (String argName : argNames) {
        String name = argName.trim();
        TheRParameter parameter = findParameter(name, parameters);
        if (parameter != null) {
          argsDesc.put(parameter, split[1]);
        }
      }
    }
    for (Map.Entry<TheRParameter, String> entry : argsDesc.entrySet()) {
      TheRParameter parameter = entry.getKey();
      String text = entry.getValue();
      TheRType type = findType(text);
      if (!TheRUnknownType.class.isInstance(type)) {
        parsedTypes.put(parameter, type);
      }
    }
  }

  public static TheRType findType(String text) {
    Set<TheRType> foundTypes = new HashSet<TheRType>();
    String[] words = text.split("[^a-zA-Z/-]");
    for (String word : words) {
      if (word.isEmpty()) {
        continue;
      }
      TheRType type = TheRSkeletonGeneratorHelper.TYPES.get(word);
      if (type != null) {
        foundTypes.add(type);
      }
    }
    return TheRUnionType.create(foundTypes);
  }

  public static Map<TheRParameter, TheRType> guessArgsTypeFromHelp(TheRHelp help, TheRFunctionExpression function) {
    List<TheRParameter> parameters = function.getParameterList().getParameterList();
    Map<TheRParameter, TheRType> parsedTypes = new HashMap<TheRParameter, TheRType>();
    String argumentsDescription = help.myArguments;
    if (argumentsDescription != null && !parameters.isEmpty()) {
      TheRSkeletonGeneratorHelper.parseArgumentsDescription(argumentsDescription, parameters, parsedTypes);
    }
    return parsedTypes;
  }

  private static TheRParameter findParameter(String name, List<TheRParameter> parameters) {
    for (TheRParameter parameter : parameters) {
      if (name.equals(parameter.getName())) {
        return parameter;
      }
    }
    return null;
  }


  public static TheRType guessReturnValueTypeFromHelp(TheRHelp help) {
    if (help.myValue == null) {
      return TheRUnknownType.INSTANCE;
    }
    return findType(help.myValue);
  }
}
