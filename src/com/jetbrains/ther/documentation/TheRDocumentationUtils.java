package com.jetbrains.ther.documentation;

import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.TheRHelp;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TheRDocumentationUtils {
  private static final Pattern myPattern = Pattern.compile("^.+:");

  @NotNull
  public static String getFormattedString(@NotNull TheRHelp help) {
    final StringBuilder builder = new StringBuilder();
    if (help.myDescription != null) {
      builder.append(help.myDescription);
      builder.append("<br>");
    }
    if (help.myArguments != null) {
      builder.append("<b>Args:</b>");
      builder.append("<br>");
      String[] args = help.myArguments.split("\n");
      for (String arg : args) {
        formatAndAppend(builder, arg);
      }
      builder.append("<br>");
    }
    if (help.myValue != null) {
      builder.append("<b>Returns:</b>");
      builder.append("<br>");
      builder.append(help.myValue);
    }
    return builder.toString();
  }

  @NotNull
  public static String getFormattedString(@NotNull String docString) {
    final StringBuilder builder = new StringBuilder();
    final String[] strings = StringUtil.splitByLines(docString);
    for (String string : strings) {
      final String trimmedString = string.trim();
      if (trimmedString.startsWith("Args:") || trimmedString.startsWith("Returns:")) {
        builder.append("<br>");
        builder.append("<b>").append(string).append("</b>");
      }
      else {
        formatAndAppend(builder, trimmedString);
      }
    }
    return builder.toString();
  }

  private static void formatAndAppend(StringBuilder builder, String docString) {
    final Matcher matcher = myPattern.matcher(docString);
    if (matcher.find()) {
      builder.append("<br>");
      builder.append(matcher.replaceFirst("<b>$0</b>"));
    }
    else {
      builder.append(docString);
    }
    builder.append(" ");
  }
}
