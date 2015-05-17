package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import com.jetbrains.ther.debugger.data.TheRProcessResponseType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

public class TheRProcessReceiver {

  @NotNull
  private static final String BROWSE_REGEX = "Browse\\[\\d+\\]>";

  @NotNull
  private static final Pattern JUST_BROWSE_PATTERN = Pattern.compile("^" + BROWSE_REGEX + " $");

  @NotNull
  private static final Pattern DEBUGGING_PATTERN = Pattern.compile("^debugging in.*" + BROWSE_REGEX + " $", Pattern.DOTALL);

  @NotNull
  private static final Pattern START_TRACE_PATTERN = Pattern.compile("^Tracing .* on entry.*" + BROWSE_REGEX + " $", Pattern.DOTALL);

  @NotNull
  private static final Pattern CONTINUE_TRACE_PATTERN =
    Pattern.compile("^Tracing .* on exit.*debugging in.*" + BROWSE_REGEX + " $", Pattern.DOTALL);

  @NotNull
  private static final Pattern END_TRACE_PATTERN = Pattern.compile("^Tracing .* on exit.*" + BROWSE_REGEX + " $", Pattern.DOTALL);

  @NotNull
  private static final Pattern RESPONSE_AND_BROWSE_PATTERN = Pattern.compile("^.*" + BROWSE_REGEX + " $", Pattern.DOTALL);

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
  public TheRProcessResponse receive() throws IOException, InterruptedException {
    final StringBuilder sb = new StringBuilder();
    int pings = 0;

    while (true) {
      waitForResponse();
      appendResponse(sb);

      if (isComplete(sb)) {
        return calculateResponse(sb, pings);
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

  private boolean isComplete(@NotNull final CharSequence response) {
    return endsWithPlusAndSpace(response) || RESPONSE_AND_BROWSE_PATTERN.matcher(response).matches();
  }

  @NotNull
  private TheRProcessResponse calculateResponse(final StringBuilder response, final int pings) {
    for (int i = 0; i < pings; i++) {
      response.setLength(response.lastIndexOf(TheRDebugConstants.LINE_SEPARATOR) - 1); // remove ping
    }

    final TheRProcessResponseType type = calculateResponseType(
      response.substring(response.indexOf(TheRDebugConstants.LINE_SEPARATOR) + 1)
    );

    response.setLength(response.lastIndexOf(TheRDebugConstants.LINE_SEPARATOR)); // remove last line

    final int index = response.indexOf(TheRDebugConstants.LINE_SEPARATOR);

    if (index == -1) {
      return new TheRProcessResponse("", type);
    }
    else {
      return new TheRProcessResponse(
        response.substring(index + 1), // remove first line
        type
      );
    }
  }

  @NotNull
  private TheRProcessResponseType calculateResponseType(@NotNull final CharSequence response) {
    if (endsWithPlusAndSpace(response)) {
      return TheRProcessResponseType.PLUS;
    }

    if (JUST_BROWSE_PATTERN.matcher(response).matches()) {
      return TheRProcessResponseType.JUST_BROWSE;
    }

    if (DEBUGGING_PATTERN.matcher(response).matches()) {
      return TheRProcessResponseType.DEBUGGING;
    }

    if (START_TRACE_PATTERN.matcher(response).matches()) {
      return TheRProcessResponseType.START_TRACE;
    }

    if (CONTINUE_TRACE_PATTERN.matcher(response).matches()) {
      return TheRProcessResponseType.CONTINUE_TRACE;
    }

    if (END_TRACE_PATTERN.matcher(response).matches()) {
      return TheRProcessResponseType.END_TRACE;
    }

    if (RESPONSE_AND_BROWSE_PATTERN.matcher(response).matches()) {
      return TheRProcessResponseType.RESPONSE_AND_BROWSE;
    }

    return null; // TODO exception
  }

  private void ping() throws IOException {
    mySender.send(TheRDebugConstants.PING_COMMAND);
  }

  private boolean endsWithPlusAndSpace(@NotNull final CharSequence sequence) {
    // TODO check line separator

    final int length = sequence.length();

    return length >= 2 && sequence.charAt(length - 1) == ' ' && sequence.charAt(length - 2) == '+';
  }
}
