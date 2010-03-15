package net.frontlinesms.build.jet.compile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

class InputStreamPrinterThread extends Thread {
	private String outPrefix;
	private InputStream is;
	private InputStreamReader isr;
	private BufferedReader reader;
	
	private PrintStream out;
	private boolean shouldStop;

	public InputStreamPrinterThread(InputStream inputStream, PrintStream out) {
		this(null, inputStream, out);
	}
	
	public InputStreamPrinterThread(String outPrefix, InputStream inputStream, PrintStream out) {
		this.outPrefix = outPrefix;
		this.is = inputStream;
		this.isr = new InputStreamReader(this.is);
		this.reader = new BufferedReader(this.isr);
		this.out = out;
	}
	
	/**
	 * Request the thread to terminate.
	 */
	public void pleaseStop() {
		this.shouldStop = true;
	}

	/**
	 * Keep reading from {@link #reader} and writing the result to {@link #out} until
	 * {@link #reader} throws an {@link IOException} or {@link #shouldStop} is set to
	 * <code>true</code>.
	 */
	public void run() {
		while(!shouldStop) {
			String line;
			try {
				line = reader.readLine();
				if(line != null) {
					if(outPrefix != null) {
						line = outPrefix + line;
					}
					out.println(line);
					
					Thread.yield();
				}
			} catch (IOException ex) {
				out.println("EXCEPTION: " + ex.getMessage());
				out.println("HALTING READ.");
				shouldStop = true;
			}
		}
	}
}