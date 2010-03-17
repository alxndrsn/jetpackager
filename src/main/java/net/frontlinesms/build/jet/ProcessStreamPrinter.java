/**
 * 
 */
package net.frontlinesms.build.jet;

import java.io.PrintStream;

/**
 * Takes the output streams of a process, and forwards them to a pair of {@link PrintStream}s.
 * @author Alex <alex@frontlinesms.com>
 */
public class ProcessStreamPrinter {
	private final String name;
	private final Process process;
	private final InputStreamPrinterThread errReader;
	private final InputStreamPrinterThread outReader;
	
	private ProcessStreamPrinter(String name, Process process, PrintStream err, PrintStream out) {
		this.name = name;
		this.process = process;
		String prefix = "[" + name + "] ";
		this.errReader = new InputStreamPrinterThread(prefix, process.getErrorStream(), err);
		this.outReader = new InputStreamPrinterThread(prefix, process.getInputStream(), out);
	}
	
	public int startBlocking() {
		this.start();
		int exitValue;
		try { exitValue = process.waitFor(); } catch (InterruptedException ex) {
			throw new RuntimeException("Interrupted while waiting for process '" + this.name + "' to finish.", ex);
		}
		this.pleaseStop();
		
		return exitValue;
	}

	private void start() {
		this.errReader.start();
		this.outReader.start();
	}
	
	private void pleaseStop() {
		errReader.pleaseStop();
		outReader.pleaseStop();
	}
	
//> STATIC FACTORIES
	public static ProcessStreamPrinter createStandardPrinter(String name, Process process) {
		return new ProcessStreamPrinter(name, process, System.err, System.out);
	}
}
