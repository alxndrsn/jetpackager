/**
 * 
 */
package net.frontlinesms.build.jet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Build a jet package from java.
 * @author Alex Anderson <alex@frontlinesms.com>
 */
public class JetPackager {
	private static void doPackaging(JetPackage jetPackage) throws IOException {
		final File dir = new File("./temp");
		
		// Create the temporary working directory
		dir.mkdirs();
		
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
			fos = new FileOutputStream(new File(dir, "output.prj"));
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
		
		// TODO Execute the build
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
					lines[i] = line.replace(subKey, propertyValue);
					if(!lines[i].equals(line)) break;
					System.out.println(line + " <- " + subKey + "->" + propertyValue + " --> " + lines[i]);
				}
				assert(!lines[i].equals(line)) : "Failed to change line: " + line;
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		System.out.println("-jpiiproject=${jpn.path}".replace("${jpn.path}", "temp/frontlinesms.jpn"));
		
		assert(args.length > 1) : "Not enough args.";
		File profileDirectory = new File(args[0]); 
		String profileName = args[1];
		
		System.out.println("Starting...");
		
		// configure the JetPackage
		JetPackage jetPackage = JetPackage.loadFromFile(new File(profileDirectory, profileName + ".properties"));
		doPackaging(jetPackage);
		
		System.out.println("...completed.");
	}
	
	private static final String[] readFileFromClasspath(String filename) throws IOException {
		InputStream is = JetPackager.class.getResourceAsStream(filename);
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

class JetPackage {
//> PROPERTY SUBSTITUTION KEYS
	private static final String PROP_JPN_PATH = "jpn.path";
	private static final String PROP_OUTPUT_NAME = "outputName";
	private static final String PROP_JAVA_MAIN_CLASS = "java.mainClass";
	private static final String PROP_SPLASH_IMAGE_PATH = "splashImage.path";
	private static final String PROP_VERSION_INFO_PRODUCT_NAME = "versionInfo.productName";
	private static final String PROP_VERSION_INFO_COPYRIGHT_OWNER = "versionInfo.copyright.owner";
	private static final String PROP_VERSION_INFO_COPYRIGHT_YEAR = "versionInfo.copyright.year";
	private static final String PROP_VERSION_INFO_FILE_DESCRIPTION = "versionInfo.fileDescription";
	private static final String PROP_VERSION_INFO_COMPANY_NAME = "versionInfo.companyName";
	private static final String PROP_ICON_PATH = "icon.path";
	
//> INSTANCE PROPERTIES
	private final String jpnPath;
	private final String javaMainClass;
	private final String outputName;
	private final String splashImagePath;
	private final String versionInfoCompanyName;
	private final String versionInfoFileDescription;
	private final String versionInfoCopyrightYear;
	private final String versionInfoCopyrightOwner;
	private final String versionInfoProductName;
	private List<String> classpathDirectories;
	private List<String> classpathJars;
	private String iconPath;
	
	public JetPackage(String jpnPath, String javaMainClass, String outputName,
			String splashImagePath, String versionInfoCompanyName,
			String versionInfoFileDescription, String versionInfoCopyrightYear,
			String versionInfoCopyrightOwner, String versionInfoProductName) {
		this.jpnPath = jpnPath;
		this.javaMainClass = javaMainClass;
		this.outputName = outputName;
		this.splashImagePath = splashImagePath;
		this.versionInfoCompanyName = versionInfoCompanyName;
		this.versionInfoFileDescription = versionInfoFileDescription;
		this.versionInfoCopyrightYear = versionInfoCopyrightYear;
		this.versionInfoCopyrightOwner = versionInfoCopyrightOwner;
		this.versionInfoProductName = versionInfoProductName;
	}

	/** Get the properties to substitute into template.prj */
	public Map<String, String> getSubstitutionProperties() {
		HashMap<String, String> props = new HashMap<String, String>();
		
		props.put(PROP_JPN_PATH, this.jpnPath); //					Path to the JPN file.  Is this the path to create at?  Or is the .jpn required at this point?
		props.put(PROP_JAVA_MAIN_CLASS, this.javaMainClass); //				The main java class to run
		props.put(PROP_OUTPUT_NAME, this.outputName); //					The file name of the built executable
		props.put(PROP_SPLASH_IMAGE_PATH, this.splashImagePath); // 			The path to the splash image
		props.put(PROP_VERSION_INFO_COMPANY_NAME, this.versionInfoCompanyName); //		Company name, as used in version info
		props.put(PROP_VERSION_INFO_FILE_DESCRIPTION, this.versionInfoFileDescription); //	The name of the project, as used in version info
		props.put(PROP_VERSION_INFO_COPYRIGHT_YEAR, this.versionInfoCopyrightYear); //	The year of the copyright, as used in version info
		props.put(PROP_VERSION_INFO_COPYRIGHT_OWNER, this.versionInfoCopyrightOwner); // The owner of the copyright, as used in version info
		props.put(PROP_VERSION_INFO_PRODUCT_NAME, this.versionInfoProductName); //		The product name, as used in version info
		props.put(PROP_ICON_PATH, this.iconPath); // The path to the icon
		
		return props;
	}
	
	public List<String> getModules() {
		ArrayList<String> modules = new ArrayList<String>();
		// add icon if it's specified
		if(this.iconPath != null) {
			modules.add(this.iconPath);
		}
		return modules;
	}
	
	public static JetPackage loadFromFile(File propertiesFile) throws IOException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(propertiesFile);
			
			Map<String, String> props = loadProperties(fis);
			
			JetPackage jetPackage = new JetPackage(
					props.remove(PROP_JPN_PATH),
					props.remove(PROP_JAVA_MAIN_CLASS),
					props.remove(PROP_OUTPUT_NAME),
					props.remove(PROP_SPLASH_IMAGE_PATH),
					props.remove(PROP_VERSION_INFO_COMPANY_NAME),
					props.remove(PROP_VERSION_INFO_FILE_DESCRIPTION),
					props.remove(PROP_VERSION_INFO_COPYRIGHT_YEAR),
					props.remove(PROP_VERSION_INFO_COPYRIGHT_OWNER),
					props.remove(PROP_VERSION_INFO_PRODUCT_NAME));
			
			String iconPath = props.remove(PROP_ICON_PATH);
			if(iconPath != null) jetPackage.iconPath = iconPath;
			
			// Check all properties used
			assert(props.size() == 0) : "There are " + props.size() + " unused properties.";
			
			return jetPackage;
		} finally {
			if(fis != null) try { fis.close(); } catch(IOException ex) {}
		}
	}
	
	private static final Map<String, String> loadProperties(InputStream in) throws IOException {
		InputStreamReader isr = null;
		BufferedReader reader = null;
		try {
			isr = new InputStreamReader(in, "UTF-8");
			reader = new BufferedReader(isr);
			
			HashMap<String, String> props = new HashMap<String, String>();
			String line;
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.length() > 0 && line.charAt(0) != '#') {
					assert(line.indexOf('=') != -1) : "Bad line in properties file: " + line;
				}
				String[] parts = line.split("=", 2);
				String key = parts[0].trim();
				String value = parts[1].trim();
				String replaced = props.put(key, value);
				assert(replaced == null) : "Property overwrites another in line: " + line;
			}
			
			return props;
		} finally {
			if(isr != null) try { isr.close(); } catch(IOException ex) {}
			if(reader != null) try { reader.close(); } catch(IOException ex) {}
		}
	}
}