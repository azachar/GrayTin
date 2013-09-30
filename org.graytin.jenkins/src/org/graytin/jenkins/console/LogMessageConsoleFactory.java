package org.graytin.jenkins.console;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.MessageConsole;

/**
 * Factory to provide new instances of {@link LogMessageConsole}
 * 
 * @since 4.1.4
 * 
 */
public class LogMessageConsoleFactory implements IConsoleFactory {
	private final IConsoleManager consoleManager;

	private MessageConsole console = null;

	public LogMessageConsoleFactory() {
		consoleManager = ConsolePlugin.getDefault().getConsoleManager();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.console.IConsoleFactory#openConsole()
	 */
	public void openConsole() {
		IOConsole console = getConsole();

		IConsole[] existing = consoleManager.getConsoles();
		boolean exists = false;
		for (int i = 0; i < existing.length; i++) {
			if (console == existing[i]) {
				exists = true;
			}
		}
		
		if (!exists) {
			consoleManager.addConsoles(new IConsole[] { console });
		}
		
//		console.clearConsole();
		consoleManager.showConsoleView(console);
	}

	private synchronized MessageConsole getConsole() {
		console = new LogMessageConsole();
		return console;
	}

	void closeConsole(LogMessageConsole console) {
		synchronized (this) {
			if (console != this.console)
				throw new IllegalArgumentException("Wrong console instance!"); //$NON-NLS-1$
			this.console = null;
		}
		consoleManager.removeConsoles(new IConsole[] { console });
	}
}