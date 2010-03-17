/**
 * 
 */
package net.frontlinesms.build.jet.compile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import net.frontlinesms.build.jet.FileUtils;
import net.frontlinesms.build.jet.ProcessStreamPrinter;
import net.frontlinesms.build.jet.PropertyLoader;

/**
 * Compiles a jet package from java.
 * @author Alex Anderson <alex@frontlinesms.com>
 */
public class JetCompiler {
	
//> CONFIGURATION PROPERTY KEYS
	private static final String CONF_PROP_WORKING_DIRECTORY = "workingDirectory";
	private static final String CONF_PROP_COMPILE_EXECUTABLE = "compiler.path";
	/** File encoding used for .prj files. */
	private static final String PRJ_FILE_ENCODING = "UTF-8";
	
//> INSTANCE VARIABLES
	/** Indicates whether this instance has been configured yet. */
	private boolean configured;
	/** The working directory for the packager */
	private File workingDirectory;
	/** The path to the package executable */
	private String compileExecutable;
	
	private void doCompile(JetCompileProfile compileProfile) throws IOException {
		assert(configured) : "This packager is not configured yet.";
		
		// Load the template.prj file from the classpath into memory
		String[] dotPrj = FileUtils.readFileFromClasspath("template.prj", PRJ_FILE_ENCODING);
		
		// TODO append !module command for jars to .prj  N.B. Recently I am not sure this is necessary as we are
		// using !batch *.jar etc. to define java classes
		
		// Filter the properties in template.prj
		subProperties(dotPrj, compileProfile.getSubstitutionProperties());
		
		// write .prj file to the temp working directory
		FileUtils.writeFile(getPrjFile(), PRJ_FILE_ENCODING, dotPrj);
		
		// TODO Copy code to the classpath directory.  Currently this is manually copied in
		
		// Execute the build
		Process buildProcess = Runtime.getRuntime().exec(getCompileCommand(), null, this.workingDirectory);
		ProcessStreamPrinter printer = ProcessStreamPrinter.createStandardPrinter("compile", buildProcess);
		int buildStatus = printer.startBlocking();
		
		System.exit(buildStatus);
	}
	
	private File getPrjFile() {
		return new File(this.workingDirectory, "output.prj");
	}
	
	private String getCompileCommand() {
		String executable;
		if(this.compileExecutable != null && this.compileExecutable.length() > 0) {
			executable = this.compileExecutable + "/jc";
		} else {
			executable = "jc";
		}
		return executable
				+ " =p " + getPrjFile();
	}

	/** Calls {@link #configure(Map)} with the contents of the supplied config file. */
	private void configure(String confPath) throws IOException {
		Map<String, String> props = PropertyLoader.loadProperties(new File(confPath));
		configure(props);
	}
	
	/** Configures the {@link JetCompiler} itself.  This is basically environment
	 * variables, working directory etc. */
	private void configure(Map<String, String> props) throws FileNotFoundException {
		assert(!this.configured) : "This should not be configured more than once.";
		
		String workingDirPropValue = props.get(CONF_PROP_WORKING_DIRECTORY);
		assert(workingDirPropValue != null) : "No working directory specified in config file.  Should be specified with key: " + CONF_PROP_WORKING_DIRECTORY;
		
		// Get the working directory as an absolute location
		this.workingDirectory = new File(workingDirPropValue).getAbsoluteFile();
		if(!workingDirectory.exists()) {
			if(!workingDirectory.mkdirs()) throw new FileNotFoundException("Working directory could not be created at " + workingDirectory.getAbsolutePath()); 
		}
		
		this.compileExecutable = props.get(CONF_PROP_COMPILE_EXECUTABLE);
		assert(this.compileExecutable!=null) : "No package executable was specified.  Should be set with key: " + CONF_PROP_COMPILE_EXECUTABLE;
		
		this.configured = true;
	}

	/**
	 * Substitutes properties in place.
	 * @param lines Lines of file to sub properties into
	 * @param props Property name/value map
	 */
	private static void subProperties(String[] lines, Map<String, String> props) {
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if(line.contains("${")) {
				for(Entry<String, String> prop : props.entrySet()) {
					String propertyKey = prop.getKey();
					String propertyValue = prop.getValue();
					if(propertyValue == null) throw new IllegalStateException("Property not set: " + propertyKey);
					String subKey = "${" + propertyKey + "}";
					line = line.replace(subKey, propertyValue);
					lines[i] = line;
				}
				assert(!lines[i].equals(line)) : "Failed to change line: " + line;
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		assert(args.length > 2) : "Not enough args.";
		String packagerConfigFilePath = args[0];
		File profileRootDirectory = new File(args[1]); 
		String profileName = args[2];
		
		System.out.println("Starting...");
		
		// configure the JetPackage
		JetCompileProfile compileProfile = JetCompileProfile.loadFromDirectory(new File(profileRootDirectory, profileName));
		JetCompiler packager = new JetCompiler();
		packager.configure(packagerConfigFilePath);
		packager.doCompile(compileProfile);
		
		System.out.println("...completed.");
	}
}
