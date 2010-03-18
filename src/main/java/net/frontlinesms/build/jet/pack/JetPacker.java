/**
 * 
 */
package net.frontlinesms.build.jet.pack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import net.frontlinesms.build.jet.FileUtils;
import net.frontlinesms.build.jet.ProcessStreamPrinter;
import net.frontlinesms.build.jet.PropertyUtils;
/**
 * Packs a jet package from java.
 * @author Alex Anderson <alex@frontlinesms.com>
 */
public class JetPacker {
	private static final String CONF_PROP_PACK_EXECUTABLE = "packer.path";
	private static final String CONF_PROP_WORKING_DIRECTORY = "workingDirectory";
	/** File encoding used for reading and writing .jpn files. */
	private static final String JPN_FILE_ENCODING = "UTF-16LE";
	
//> INSTANCE PROPERTIES
	/** Indicates whether this instance has been configured yet. */
	private boolean configured;
	/** The working directory for the packager */
	private File workingDirectory;
	/** The path to the pack executable */
	private String packExecutable;
	
	public void doPack(JetPackProfile packProfile) throws IOException {
		assert(this.configured) : "You cannot pack until the packer has been configured.";
		generateJpnFile(packProfile);
		executeXPack();
	}

	/** Calls {@link #configure(Map)} with the contents of the supplied config file. */
	private void configure(String confPath) throws IOException {
		Map<String, String> props = PropertyUtils.loadProperties(new File(confPath));
		configure(props);
	}

	/** Configures the {@link JetPacker} itself.  This is basically environment
	 * variables, working directory etc. 
	 * @throws FileNotFoundException */
	public void configure(Map<String, String> props) throws FileNotFoundException {
		assert(!configured) : "Can only configure once.";
		
		String workingDirPropValue = props.get(CONF_PROP_WORKING_DIRECTORY);
		assert(workingDirPropValue != null) : "No working directory specified in config file.  Should be specified with key: " + CONF_PROP_WORKING_DIRECTORY;
		
		// Get the working directory as an absolute location
		this.workingDirectory = new File(workingDirPropValue).getAbsoluteFile();
		if(!workingDirectory.exists()) {
			if(!workingDirectory.mkdirs()) throw new FileNotFoundException("Working directory could not be created at " + workingDirectory.getAbsolutePath()); 
		}
		
		this.packExecutable = props.get(CONF_PROP_PACK_EXECUTABLE);
		assert(this.packExecutable!=null) : "No package executable was specified.  Should be set with key: " + CONF_PROP_PACK_EXECUTABLE;
		
		this.configured = true;
	}
	
//> INSTANCE HELPER METHODS
	/** Generate the .jpn file which dictates the content of the package. 
	 * @param packProfile 
	 * @throws IOException */
	private void generateJpnFile(JetPackProfile packProfile) throws IOException {
		// For now, we just copy an old jpn file from the temp directory.
		String[] dotJpnLines = FileUtils.readFileFromClasspath(this.getClass(), "template.jpn", JPN_FILE_ENCODING);
		
		PropertyUtils.subProperties(dotJpnLines, packProfile.getSubstitutionProperties());
		
		FileUtils.writeFile(getJpnFile(), JPN_FILE_ENCODING, dotJpnLines);
		
		// TODO we should actually generate a JPN which is relevant to the project!
	}
	
	private int executeXPack() throws IOException {
		Process packageProcess = Runtime.getRuntime().exec(getPackageCommand(), null, this.workingDirectory);
		ProcessStreamPrinter psp = ProcessStreamPrinter.createStandardPrinter("package", packageProcess);
		return psp.startBlocking();
	}

	private String getPackageCommand() {
		String executable;
		if(this.packExecutable != null && this.packExecutable.length() > 0) {
			executable = this.packExecutable + "/jc";
		} else {
			executable = "xpack";
		}
		return executable 
			+ " " + getJpnFile();
	}

	private File getJpnFile() {
		return new File(this.workingDirectory, "output.jpn");
	}
	
//> STATIC HELPER METHODS
	public static void main(String[] args) throws IOException {
		assert(args.length > 2) : "Not enough args.";
		String packagerConfigFilePath = args[0];
		File profileRootDirectory = new File(args[1]); 
		String profileName = args[2];
		
		System.out.println("Starting...");
		
		JetPackProfile packProfile = JetPackProfile.loadFromDirectory(new File(profileRootDirectory, profileName));
		JetPacker packer = new JetPacker();
		packer.configure(packagerConfigFilePath);
		packer.doPack(packProfile);

		System.out.println("...completed.");
	}
}
