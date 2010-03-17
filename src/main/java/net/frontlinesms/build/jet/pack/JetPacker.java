/**
 * 
 */
package net.frontlinesms.build.jet.pack;

import java.io.File;
import java.io.IOException;

import net.frontlinesms.build.jet.FileUtils;
import net.frontlinesms.build.jet.ProcessStreamPrinter;
import net.frontlinesms.build.jet.compile.JetCompileProfile;

/**
 * @author aga
 */
public class JetPacker {
	/** File encoding used for reading and writing .jpn files. */
	private static final String JPN_FILE_ENCODING = "UTF-16LE";
	
//> INSTANCE PROPERTIES
	/** Indicates whether this instance has been configured yet. */
	private boolean configured;
	/** The working directory for the packager */
	private File workingDirectory;
	
	public void doPack() throws IOException {
		assert(this.configured) : "You cannot pack until the packer has been configured.";
		generateJpnFile();
		executeXPack();
	}
	
	public void configure(JetCompileProfile compileProfile) {
		assert(!configured) : "Can only configure once.";
		configured = true;
	}
	
//> INSTANCE HELPER METHODS
	/** Generate the .jpn file which dictates the content of the package. 
	 * @throws IOException */
	private void generateJpnFile() throws IOException {
		// For now, we just copy an old jpn file from the temp directory.
		String[] dotJpnLines = FileUtils.readFileFromClasspath("/temp/FrontlineSMS.jpn", JPN_FILE_ENCODING);
		FileUtils.writeFile(getJpnFile(), JPN_FILE_ENCODING, dotJpnLines);
		
		// TODO we should actually generate a JPN which is relevant to the project!
	}
	
	private int executeXPack() throws IOException {
		Process packageProcess = Runtime.getRuntime().exec(getPackageCommand(), null, this.workingDirectory);
		ProcessStreamPrinter psp = ProcessStreamPrinter.createStandardPrinter("package", packageProcess);
		return psp.startBlocking();
	}

	private String getPackageCommand() {
		return "xpack " + getJpnFile();
	}

	private File getJpnFile() {
		return new File(this.workingDirectory, "output.jpn");
	}
	
//> STATIC HELPER METHODS
	public static void main(String[] args) throws IOException {
		JetPacker packer = new JetPacker();
		packer.configure(null);
		packer.doPack();
	}
}
