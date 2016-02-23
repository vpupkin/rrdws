package ws.rrd.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

public final class ExceptionUtils {

	private static final Logger log = Logger.getLogger(ExceptionUtils.class
			.getName());

	// Can not instantiation. Factory pattern.
	private ExceptionUtils() {
		return;
	}

	public static String getStackTrace(Exception e) {

		ByteArrayOutputStream osBytes = new ByteArrayOutputStream();

		// e.  printStackTrace(new PrintStream(osBytes, true));

		return osBytes.toString();
	}

	public static void swapFailedException(String targetUrl,
			HttpServletResponse resp, Exception e, int errorCode)
			throws IOException {

		log.warning(String.format("Swap '%s' failed. Exception message: '%s'.",
				targetUrl, getStackTrace(e)));

		resp.sendError(errorCode);
	}

	public static void swapFailedException(HttpServletResponse resp,
			Exception e, int errorCode) throws IOException {

		log.warning(String.format("Swap failed. Exception message: '%s'.",
				getStackTrace(e)));

		resp.sendError(errorCode);
	}
}
