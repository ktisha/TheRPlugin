package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRProcessResponseAndType;
import com.jetbrains.ther.debugger.data.TheRProcessResponseType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

public class TheRProcessReceiver {

  @NotNull
  private static final Pattern JUST_BROWSE_PATTERN = Pattern.compile("^Browse\\[\\d+\\]> $");

  @NotNull
  private static final Pattern ENDS_WITH_BROWSE_PATTERN = Pattern.compile("^.*Browse\\[\\d+\\]> $", Pattern.DOTALL);

  @NotNull
  private static final Pattern DEBUGGING_PATTERN = Pattern.compile("^debugging in.*$", Pattern.DOTALL);

  @NotNull
  private final InputStream myStream;

  @NotNull
  private final InputStreamReader myReader;

  @NotNull
  private final char[] myBuffer;

  @NotNull
  private final TheRProcessSender mySender;

  public TheRProcessReceiver(@NotNull final InputStream stream, @NotNull final TheRProcessSender sender) {
    myStream = stream;
    myReader = new InputStreamReader(stream);
    myBuffer = new char[TheRDebugConstants.DEFAULT_BUFFER];

    mySender = sender;
  }

  @NotNull
  public TheRProcessResponseAndType receive() throws IOException, InterruptedException {
    final StringBuilder sb = new StringBuilder();
    int pings = 0;

    while (true) {
      waitForResponse();
      appendResponse(sb);

      final TheRProcessResponseType responseType = calculateResponseType(sb);

      if (responseType != null) {
        return new TheRProcessResponseAndType(removePingsAndCommand(sb, pings), responseType);
      }

      ping(); // pings interpreter to get tail of response
      pings++;
    }
  }

  private void waitForResponse() throws IOException, InterruptedException {
    long millis = TheRDebugConstants.INITIAL_SLEEP;

    while (myStream.available() == 0) {
      Thread.sleep(millis);
      millis *= 2;
    }
  }

  private void appendResponse(@NotNull final StringBuilder sb) throws IOException {
    while (myStream.available() != 0) {
      sb.append(myBuffer, 0, myReader.read(myBuffer));
    }
  }

  @Nullable
  public static TheRProcessResponseType calculateResponseType(@NotNull final CharSequence response) {
    if (endsWithPlusAndSpace(response)) {
      return TheRProcessResponseType.PLUS;
    }

    if (JUST_BROWSE_PATTERN.matcher(response).matches()) {
      return TheRProcessResponseType.JUST_BROWSE;
    }

    if (DEBUGGING_PATTERN.matcher(response).matches()) {
      return TheRProcessResponseType.DEBUG;
    }

    if (ENDS_WITH_BROWSE_PATTERN.matcher(response).matches()) {
      return TheRProcessResponseType.RESPONSE_AND_BROWSE;
    }

    return null;
  }

  @NotNull
  private String removePingsAndCommand(final StringBuilder sb, final int pings) {
    for (int i = 0; i < pings; i++) {
      sb.setLength(sb.lastIndexOf(TheRDebugConstants.LINE_SEPARATOR) - 1);
    }

    return sb.substring(sb.indexOf(TheRDebugConstants.LINE_SEPARATOR) + 1);
  }

  private void ping() throws IOException {
    mySender.send(TheRDebugConstants.PING_COMMAND);
  }

  private static boolean endsWithPlusAndSpace(@NotNull final CharSequence sequence) {
    final int length = sequence.length();

    return length >= 2 && sequence.charAt(length - 1) == ' ' && sequence.charAt(length - 2) == '+';
  }
}
