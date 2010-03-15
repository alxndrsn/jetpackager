/**
 * 
 */
package net.frontlinesms.build.jet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Build a jet package from java.
 * @author Alex Anderson <alex@frontlinesms.com>
 */
public class JetCompiler {
	
//> CONFIGURATION PROPERTY KEYS
	private static final String CONF_PROP_WORKING_DIRECTORY = "workingDirectory";
	private static final String CONF_PROP_PACKAGE_EXECUTABLE = "packager.path";
	
//> INSTANCE VARIABLES
	/** Indicates whether this packager has been configured yet. */
	private boolean configured;
	/** The working directory for the packager */
	private File workingDirectory;
	/** The path to the package executable */
	private String packageExecutable;
	
	private void doPackaging(JetCompileProfile jetPackage) throws IOException {
		assert(configured) : "This packager is not configured yet.";
		
		// Load the template.prj file from the classpath into memory
		String[] dotPrj = readFileFromClasspath("template.prj");
		
		// TODO append !module command for jars to .prj
		
		// Filter the properties in template.prj
		subProperties(dotPrj, jetPackage.getSubstitutionProperties());
		
		// write .prj file to the temp working directory
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		BufferedWriter writer = null;
		try {
			fos = new FileOutputStream(getPrjFile());
			osw = new OutputStreamWriter(fos, "UTF-8");
			writer = new BufferedWriter(osw);
			for(String line : dotPrj) {
				writer.write(line);
				writer.write('\n');
			}
		} finally {
			if(writer != null) try { writer.close(); } catch(IOException ex) {}
			if(osw != null) try { osw.close(); } catch(IOException ex) {}
			if(fos != null) try { fos.close(); } catch(IOException ex) {}
		}
		
		// TODO Copy code to the classpath directory
		
		// Execute the build
		Process buildProcess = Runtime.getRuntime().exec(getPackageCommand(), null, this.workingDirectory);
		
		InputStreamPrinterThread errReader = new InputStreamPrinterThread("[package|err] ", buildProcess.getErrorStream(), System.err);
		errReader.start();
		
		InputStreamPrinterThread stdoutReader = new InputStreamPrinterThread("[package|out] ", buildProcess.getInputStream(), System.out);
		stdoutReader.start();
		
		int buildStatus;
		try { buildStatus = buildProcess.waitFor(); } catch (InterruptedException ex) {
			throw new RuntimeException("Interrupted while waiting for build to finish.", ex);
		}

		errReader.pleaseStop();
		stdoutReader.pleaseStop();
		
		System.exit(buildStatus);
	}
	
	private File getPrjFile() {
		return new File(this.workingDirectory, "output.prj");
	}
	
	private String getPackageCommand() {
		String executable = "jc";
		if(this.packageExecutable != null && this.packageExecutable.length() > 0) {
			executable = this.packageExecutable + "/jc";
		}
		return executable
				+ " =p " + getPrjFile();
	}
	
	private void configure(String confPath) throws IOException {
		Map<String, String> props = PropertyLoader.loadProperties(new File(confPath));
		configure(props);
	}
	
	private void configure(Map<String, String> props) throws FileNotFoundException {
		String workingDirPropValue = props.get(CONF_PROP_WORKING_DIRECTORY);
		assert(workingDirPropValue != null) : "No working directory specified in config file.  Should be specified with key: " + CONF_PROP_WORKING_DIRECTORY;
		
		// Get the working directory as an absolute location
		this.workingDirectory = new File(workingDirPropValue).getAbsoluteFile();
		if(!workingDirectory.exists()) {
			if(!workingDirectory.mkdirs()) throw new FileNotFoundException("Working directory could not be created at " + workingDirectory.getAbsolutePath()); 
		}
		
		this.packageExecutable = props.get(CONF_PROP_PACKAGE_EXECUTABLE);
		assert(this.packageExecutable!=null) : "No package executable was specified.  Should be set with key: " + CONF_PROP_PACKAGE_EXECUTABLE;
		
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
		JetCompileProfile jetPackage = JetCompileProfile.loadFromDirectory(new File(profileRootDirectory, profileName));
		JetCompiler packager = new JetCompiler();
		packager.configure(packagerConfigFilePath);
		packager.doPackaging(jetPackage);
		
		System.out.println("...completed.");
	}
	
	private static final String[] readFileFromClasspath(String filename) throws IOException {
		InputStream is = JetCompiler.class.getResourceAsStream(filename);
		InputStreamReader isr = new InputStreamReader(is, "UTF-8");
		BufferedReader reader = new BufferedReader(isr);
		
		String line;
		List<String> lines = new ArrayList<String>();
		while((line = reader.readLine()) != null) {
			lines.add(line);
		}
		return lines.toArray(new String[lines.size()]);
	}
}
