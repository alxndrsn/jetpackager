/**
 * 
 */
package net.frontlinesms.build.jet.pack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
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
	/** File encoding used for reading and writing .jpn files. */
	private static final String JPN_FILE_ENCODING = "UTF-16LE";
	
//> INSTANCE PROPERTIES
	/** Indicates whether this instance has been configured yet. */
	private boolean configured;
	/** The working directory for the packager */
	private final File workingDirectory;
	/** The path to the pack executable */
	private String packExecutable;
	
//> CONSTRUCTORS
	public JetPacker(File workingDirectory) {
		this.workingDirectory = workingDirectory;
	}
	
//> INSTANCE METHODS
	public int doPack(JetPackProfile packProfile) throws IOException {
		assert(this.configured) : "You cannot pack until the packer has been configured.";
		generateJpnFile(packProfile);
		return executeXPack();
	}

	/** Calls {@link #configure(Map)} with the contents of the supplied config file. */
	private void configure(String confPath) throws IOException {
		Map<String, String> props = PropertyUtils.loadProperties(new File(confPath));
		configure(props);
	}

	/** Configures the {@link JetPacker} itself.  This is basically environment
	 * variables, working directory etc. 
	 * @throws FileNotFoundException */
	public void configure(Map<String, String> props) {
		assert(!configured) : "Can only configure once.";
		
		this.packExecutable = props.get(CONF_PROP_PACK_EXECUTABLE);
		assert(this.packExecutable!=null) : "No package executable was specified.  Should be set with key: " + CONF_PROP_PACK_EXECUTABLE;
		
		this.configured = true;
	}

	/** Call {@link #configure(Map)} with default settings. */
	public void configureDefaults() {
		Map<String, String> defaultConfiguration = new HashMap<String, String>();
		defaultConfiguration.put(CONF_PROP_PACK_EXECUTABLE, "");
		configure(defaultConfiguration);
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
		return new File(this.workingDirectory, "output.jpn").getAbsoluteFile();
	}
	
//> STATIC HELPER METHODS
	public static void main(String[] args) throws IOException {
		assert(args.length > 2) : "Not enough args.";
		String workingDirectoryRoot = args[0];
		// Get the working directory as an absolute location
		File workingDirectory = new File(workingDirectoryRoot).getAbsoluteFile();
		if(!workingDirectory.exists()) {
			if(!workingDirectory.mkdirs()) throw new FileNotFoundException("Working directory could not be created at " + workingDirectory.getAbsolutePath()); 
		}
		
		String packagerConfigFilePath = args[1];
		File profileDirectory = new File(args[2]); 
		
		System.out.println("Starting...");
		
		JetPackProfile packProfile = JetPackProfile.loadFromDirectory(new File(profileDirectory, "pack.profile.properties"), workingDirectory);
		JetPacker packer = new JetPacker(workingDirectory);
		packer.configure(packagerConfigFilePath);
		packer.doPack(packProfile);

		System.out.println("...completed.");
	}
}
