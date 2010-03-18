/**
 * 
 */
package net.frontlinesms.build.jet.pack;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.frontlinesms.build.jet.PropertyUtils;

/**
 * @author aga
 *
 */
public class JetPackProfile {
	
//> PROPERTY KEY CONSTANTS
	/** Property key: TODO document from template.notes.txt */
	private static final String PROP_CLASSPATH_COMMANDLINE = "classpath.commandline";
	/** Property key: TODO document from template.notes.txt */
	private static final String PROP_START_MENU_PROGRAM_FOLDER_ROOT = "startMenu.programFolderRoot";
	/** Property key: TODO document from template.notes.txt */
	private static final String PROP_PROGRAM_FILES_HOME = "programFilesHome";
	/** Property key: TODO document from template.notes.txt */
	private static final String PROP_EXECUTABLE_NAME = "executableName";
	/** Property key: TODO document from template.notes.txt */
	private static final String PROP_RESOURCEPATH = "resourcepath";
	/** Property key: TODO document from template.notes.txt */
	private static final String PROP_PACK_CONTENT_LINE_FORMAT = "packContent.lineFormat";
	/** Property key: TODO document from template.notes.txt */
	private static final String PROP_PACK_CONTENT = "packContent";
	/** Property key: for {@link #productName} */
	private static final String PROP_PRODUCT_NAME = "productName";
	/** Property key: TODO document from template.notes.txt */
	private static final String PROP_PRODUCT_VERSION = "productVersion";
	/** Property key: TODO document from template.notes.txt */
	private static final String PROP_PRODUCT_VERSION_STANDARDISED = "productVersion.standardised";
	/** Property key: TODO document from template.notes.txt */
	private static final String PROP_PRODUCT_DESCRIPTION = "productDescription";
	/** Property key: TODO document from template.notes.txt */
	private static final String PROP_PRODUCT_VENDOR = "productVendor";
	/** Property key: TODO document from template.notes.txt */
	private static final String PROP_WORKING_DIRECTORY = "workingDirectory";

//> INSTANCE PROPERTIES
	/** This is the directory that all paths in the package configuration are relative to. */
	private final File workingDirectory;
	/** This is the directory that jars are located in. */
	private final File jarDirectory;
	/** This is the directory that package contents are located in. */
	private final File packContentDirectory;
	
	/** Property: TODO document from template.notes.txt */
	private final String productName;
	/** Property: TODO document from template.notes.txt */
	private final String productVersion;
	/** Property: TODO document from template.notes.txt */
	private final String productVersionStandardised;
	/** Property: TODO document from template.notes.txt */
	private final String productDescription;
	/** Property: TODO document from template.notes.txt */
	private final String productVendor;
	/** Property: TODO document from template.notes.txt */
	private final String executableName;
	/** Property: TODO document from template.notes.txt */
	private final String startMenuProgramFolderRoot;
	/** Property: TODO document from template.notes.txt */
	private final String programFilesHome;
	
//> CONSTRUCTORS
	private JetPackProfile(File workingDirectory,
				String productName,
				String productVersion, String productVersionStandardised,
				String productDescription, String productVendor,
				String executableName, String startMenuProgramFolderRoot,
				String programFilesHome) {
		this.workingDirectory = workingDirectory;
		assert(this.workingDirectory.exists()) : "The working directory does not exist: " + this.workingDirectory.getAbsolutePath();
		this.jarDirectory = new File(workingDirectory, "classpath");
		assert(this.jarDirectory.exists()) : "The classpath directory does not exist: " + this.jarDirectory.getAbsolutePath();
		this.packContentDirectory = new File(workingDirectory, "packageContent");
		assert(this.packContentDirectory.exists()) : "The pack content directory does not exist: " + this.packContentDirectory.getAbsolutePath();
		
		this.productName = productName;
		this.productVersion = productVersion;
		this.productVersionStandardised = productVersionStandardised;
		this.productDescription = productDescription;
		this.productVendor = productVendor;
		this.executableName = executableName;
		this.startMenuProgramFolderRoot = startMenuProgramFolderRoot;
		this.programFilesHome = programFilesHome;
	}
	
//> INSTANCE METHODS
	public Map<String, String> getSubstitutionProperties() {
		Map<String, String> props = new HashMap<String, String>();
		
		props.put(PROP_WORKING_DIRECTORY, workingDirectory.getAbsolutePath());
		
		// Add classpath details
		props.put(PROP_CLASSPATH_COMMANDLINE, getClasspathCommandline());
		props.put(PROP_RESOURCEPATH, getResourcepath());
		
		// Add package content details
		props.put(PROP_PACK_CONTENT, getPackContent());
		props.put(PROP_PACK_CONTENT_LINE_FORMAT, getPackContentLineFormat());
		
		// Add other variables
		props.put(PROP_PRODUCT_NAME, this.productName);
		props.put(PROP_PRODUCT_VERSION, this.productVersion);
		props.put(PROP_PRODUCT_VERSION_STANDARDISED, this.productVersionStandardised);
		props.put(PROP_PRODUCT_DESCRIPTION, this.productDescription);
		props.put(PROP_PRODUCT_VENDOR, this.productVendor);
		props.put(PROP_EXECUTABLE_NAME, this.executableName);
		props.put(PROP_START_MENU_PROGRAM_FOLDER_ROOT, this.startMenuProgramFolderRoot);
		props.put(PROP_PROGRAM_FILES_HOME, this.programFilesHome);

		return props;
	}

//> PROPERTY GENERATION
	/** @return entry for {@link #PROP_PACK_CONTENT} */
	private String getPackContent() {
		return new PackContentBuilder(this.packContentDirectory).toString();
	}
	
	/** @return entry for {@link #PROP_PACK_CONTENT_LINE_FORMAT} */
	private String getPackContentLineFormat() {
		StringBuilder bob = new StringBuilder();
		for(File f : this.packContentDirectory.listFiles()) {
			appendPackContentLineFormat(bob, f);
		}
		return bob.toString();
	}
	
	private void appendPackContentLineFormat(StringBuilder bob, File f) {
		if(f.isDirectory()) {
			bob.append("!directory \"" + getRelativePackPath(f) + "\"\n");
			for(File child : f.listFiles()) appendPackContentLineFormat(bob, child);
		} else if(f.isFile()) {
			bob.append("!file \"" + getRelativePackPath(f) + "\" \"" + f.getAbsolutePath() + "\"\n");
		} else throw new RuntimeException("Not sure what to do with file: " + f.getAbsolutePath());
	}
	
	private String getRelativePackPath(File f) {
		String packRoot = this.packContentDirectory.getAbsolutePath();
		String abs = f.getAbsolutePath();
		String rel = abs.substring(packRoot.length());
		while(rel.length()>0 && (rel.charAt(0)=='\\' || rel.charAt(0)=='/')) {
			rel = rel.substring(1);
		}
		return rel;
	}

	/** @return entry for {@link #PROP_CLASSPATH_COMMANDLINE} */
	private String getClasspathCommandline() {
		StringBuilder bob = new StringBuilder();
		File[] jarList = this.getJarlist();
		for (int i = 0; i < jarList.length; i++) {
			if(i > 0) bob.append(';');
			bob.append(jarList[i].getName());
		}
		return bob.toString();
	}
	
	/** @return entry for {@link #PROP_RESOURCEPATH} */
	private String getResourcepath() {
		StringBuilder bob = new StringBuilder();
		File[] jarList = this.getJarlist();
		for (int i = 0; i < jarList.length; i++) {
			bob.append("            !entry 3 \"");
			bob.append(jarList[i].getAbsolutePath());
			bob.append("\" null\n");
		}
		return bob.toString();
	}
	
	/** @return a list of jars in the {@link #jarDirectory} */
	private File[] getJarlist() {
		return this.jarDirectory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && file.getName().endsWith(".jar");
			}
		});
	}

//> STATIC FACTORIES
	/** @return a new {@link JetPackProfile} configured from the supplied directory */
	public static JetPackProfile loadFromDirectory(File profile, File workingDirectory) throws IOException {
		Map<String, String> props = PropertyUtils.loadProperties(profile);
		
		JetPackProfile packProfile = new JetPackProfile(workingDirectory,
				props.remove(PROP_PRODUCT_NAME),
				props.remove(PROP_PRODUCT_VERSION),
				props.remove(PROP_PRODUCT_VERSION_STANDARDISED),
				props.remove(PROP_PRODUCT_DESCRIPTION),
				props.remove(PROP_PRODUCT_VENDOR),
				props.remove(PROP_EXECUTABLE_NAME),
				props.remove(PROP_START_MENU_PROGRAM_FOLDER_ROOT),
				props.remove(PROP_PROGRAM_FILES_HOME)
				);

		// Check all properties used
		assert(props.size() == 0) : "There are " + props.size() + " unused properties.";
		
		return packProfile;
	}
}

class PackContentBuilder {
	private static final String LINEEND = "\n";
	private static final String TAB = "    ";
	
	private final long TIMESTAMP;
	private int currentIndent;
	private final StringBuilder bob = new StringBuilder(); 
	
	public PackContentBuilder(File root) {
		this.TIMESTAMP = System.currentTimeMillis();
		
		++currentIndent;
		for(File f : root.listFiles()) {
			appendFile(f);
		}
		--currentIndent;
		
		assert(currentIndent == 0) : "Seem to have traversed directories wierdly: indent=" + currentIndent;
	}

	private void appendFile(File f) {
		if(f.isDirectory()) {
			// add opening tag for folder
			String folderName = f.getName();
			String folderPermissions = "0 0 4";
			append("<folder \"" + folderName
					+ "\" " + folderPermissions);
			
			// add contents of the folder
			++currentIndent;
			for(File child : f.listFiles()) {
				appendFile(child);
			}
			--currentIndent;
			
			// add closing tag for folder
			append(">end //folder");
		} else if(f.isFile()) {
			append("<file");
			appendAttribute("basic_info", "\"" + f.getName() + "\" \"" + f.getAbsolutePath() + "\" 0 false \"" + f.getAbsolutePath() + "\"");
			appendAttribute("flags", "0 0 4");
			appendAttribute("cachedLastModified", TIMESTAMP + "L");
			appendAttribute("attributes", "0");
			append(">end //file");
		} else throw new RuntimeException("Not sure how to handle file: " + f.getAbsolutePath());
	}
	
	private void indent() {
		for(int i=0; i<currentIndent; ++i) bob.append(TAB);
	}
	
	private void append(String line) {
		indent();
		// append content
		bob.append(line);
		// append line end
		bob.append(LINEEND);
	}
	
	private void appendAttribute(String name, String value) {
		indent();
		bob.append(TAB);
		bob.append('!');
		bob.append(name);
		bob.append(' ');
		bob.append(value);
		bob.append(LINEEND);
	}
	
	public String toString() {
		return bob.toString();
	}
}