package com.jetbrains.ther.typing;

import com.jetbrains.ther.psi.api.TheRParameter;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocStringUtil {
  public static final Pattern pattern = Pattern.compile("(.*)@type (.*) : (.*)");

  @Nullable
  public static TheRType parse(TheRParameter parameter, String comment) {
    Matcher matcher = pattern.matcher(comment);
    if (matcher.matches()) {
      String name = matcher.group(2);
      if (name.equals(parameter.getName())) {
        String type = matcher.group(3);
        return TheRTypeProvider.findTypeByName(type);
      }
    }
    return null;
  }
}
