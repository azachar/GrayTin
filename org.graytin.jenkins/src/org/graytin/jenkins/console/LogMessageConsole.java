package org.graytin.jenkins.console;

import java.io.PrintStream;

import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;


/**
 * Message console implementation that displays the System.out and System.err messages.
 * 
 * @since 4.1.4
 * 
 */
public class LogMessageConsole extends MessageConsole {

	public final static String DISPLAY_NAME = "BI SDK log";
	
	public final static String CONSOLE_TYPE = "logConsole"; //$NON-NLS-1$

	private PrintStream originalOut;

	private PrintStream originalErr;

	public LogMessageConsole() {
//		super(NLS.bind(Messages.LogMessageConsole_name, Platform.getInstallLocation().getURL().getPath()), TYPE, ImageCache.getImageDescriptor( LogMessageConsole.class, "icons/logmessageconsole.png"), true);
		super(DISPLAY_NAME, CONSOLE_TYPE, null, true);
		saveCurrentPrintStreams();
		MessageConsoleStream outStream = newMessageStream();
		MessageConsoleStream errStream = newMessageStream();
		//errStream.setColor(ColorCache.getColor(LogMessageConsole.class, 255, 0, 0));
		System.setOut(new PrintStream(outStream));
		System.setErr(new PrintStream(errStream));
	}

	/**
	 * Save the original printstreams to be able to restore them again when this console gets disposed
	 */
	private void saveCurrentPrintStreams() {
		originalOut = System.out;
		originalErr = System.err;
	}

	/**
	 * Sets the print streams to their original values again.
	 */
	private void restoreOriginalPrintStreams() {
		System.setOut(originalOut);
		System.setErr(originalErr);
	}

	@Override
	protected void dispose() {
		restoreOriginalPrintStreams();
		super.dispose();
	}

}
