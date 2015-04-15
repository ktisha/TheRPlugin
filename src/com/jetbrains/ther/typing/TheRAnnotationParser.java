package com.jetbrains.ther.typing;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.ther.TheRElementGenerator;
import com.jetbrains.ther.psi.api.TheRExpression;
import com.jetbrains.ther.typing.types.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TheRAnnotationParser {

  public static final String TAG_PREFIX = "@";
  public static final Pattern SPACE_PATTERN = Pattern.compile("\\s");
  public static final String TYPE_TAG = "type";
  public static final String RETURN_TAG = "return";
  public static final String RULE_TAG = "rule";
  public static final Pattern COLON_PATTERN = Pattern.compile(":");
  public static final Pattern ARROW_PATTERN = Pattern.compile("->");
  public static final Pattern COMMA_PATTERN = Pattern.compile(",");
  public static final Pattern EQUALS_PATTERN = Pattern.compile("=");
  public static final Pattern MAX_PATTERN = Pattern.compile("max\\((.*)\\)");
  private TheRFunctionType myType;

  public TheRAnnotationParser(TheRFunctionType type) {
    myType = type;
  }

  public void interpretLine(Substring line) {
    if (!line.startsWith(TAG_PREFIX)) {
      return;
    }
    line = line.substring(TAG_PREFIX.length());
    List<Substring> split = line.split(SPACE_PATTERN);
    Substring tagName = split.get(0);
    if (tagName != null) {
      parseTag(tagName, line.substring(tagName.length()).trim());
    }
  }

  @SuppressWarnings("EqualsBetweenInconvertibleTypes")
  private void parseTag(Substring tagName, Substring line) {
    if (tagName.equals(TYPE_TAG)) {
      parseType(line);
      return;
    }
    if (tagName.equals(RETURN_TAG)) {
      parseReturn(line);
      return;
    }
    if (tagName.equals(RULE_TAG)) {
      parseRule(line);
    }
  }

  private void parseRule(Substring line) {
    List<Substring> split = line.split(ARROW_PATTERN);
    if (split.size() != 2) {
      return;
    }
    Substring parameters = split.get(0).trim();
    Substring returnType = split.get(1).trim();
    TheRFunctionRule rule =  new TheRFunctionRule(findType(returnType));
    if (parameters.startsWith("(") && parameters.getValue().endsWith(")")) {
      parseParameters(parameters.substring(1, parameters.length() - 1), rule);
    }
    myType.addRule(rule);
  }

  private TheRType findType(Substring typeSubstring) {
    String typeName = typeSubstring.trim().getValue();
    Matcher maxMatcher = MAX_PATTERN.matcher(typeName);
    if (maxMatcher.matches()) {
      Substring types = new Substring(maxMatcher.group(1));
      List<TheRType> typesList = new ArrayList<TheRType>();
      for (Substring type : types.split(COMMA_PATTERN)) {
        typesList.add(findType(type));
      }
      return new TheRMaxType(typesList);
    }
    TheRType type = createType(typeName);
    return type != null ? type : new TheRTypeVariable(typeName);
  }

  private void parseParameters(Substring line, TheRFunctionRule rule) {
    List<Substring> split = line.split(COMMA_PATTERN);
    for (Substring parameterDescr : split) {
      parseParameterRule(parameterDescr, rule);
    }
  }

  private void parseParameterRule(Substring descr, TheRFunctionRule rule) {
    List<Substring> split = descr.split(COLON_PATTERN);
    if (split.size() == 1) {
      split = descr.split(EQUALS_PATTERN);
      if (split.size() == 2) {
        PsiFile file =
          TheRElementGenerator.createDummyFile(split.get(1).trim().getValue(), false, myType.getFunctionExpression().getProject());
        PsiElement child = file.getFirstChild();
        if (child instanceof TheRExpression) {
          rule.addParameter(split.get(0).trim().getValue(), null, (TheRExpression)child);
        }
      }
    } else if (split.size() == 2) {
      String name = split.get(0).trim().getValue();
      Substring typeAndMaybeValue = split.get(1);
      final Substring typeSubstring;
      TheRExpression value = null;
      split = typeAndMaybeValue.split(EQUALS_PATTERN);
      if (split.size() == 1) {
        typeSubstring = typeAndMaybeValue;
      } else {
        PsiFile file =
          TheRElementGenerator.createDummyFile(split.get(1).trim().getValue(), false, myType.getFunctionExpression().getProject());
        PsiElement child = file.getFirstChild();
        if (child instanceof TheRExpression) {
          value = (TheRExpression)child;
        }
        typeSubstring = split.get(0);
      }
      TheRType type = findType(typeSubstring);
      // TODO: check that 'value' type matches 'type'
      rule.addParameter(name, type, value);
    }
  }

  private void parseReturn(Substring line) {
    String typeName = line.trim().getValue();
    TheRType type = createType(typeName);
    if (type != null && type != TheRType.UNKNOWN) {
      myType.setReturnType(type);
    }
  }

  private void parseType(Substring line) {
    List<Substring> split = line.split(COLON_PATTERN);
    if (split.size() > 2) {
      return;
    }
    Substring parameterName = split.get(0).trim();
    Substring typeName = split.get(1).trim();
    TheRType type = createType(typeName.getValue());
    if (type != TheRType.UNKNOWN) {
      myType.addParameterType(parameterName.getValue(), type);
    }
  }

  private TheRType createType(String typeName) {
    String[] typeNames = typeName.split("\\|");
    Set<TheRType> types = new HashSet<TheRType>();
    for (String name : typeNames) {
      TheRType type = TheRTypeProvider.findTypeByName(name.trim());
      if (type != TheRType.UNKNOWN) {
        types.add(type);
      }
    }
    return TheRUnionType.create(types);
  }

}
