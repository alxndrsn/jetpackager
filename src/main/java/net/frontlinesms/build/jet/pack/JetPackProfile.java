/**
 * 
 */
package net.frontlinesms.build.jet.pack;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
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
	private static final String PACK_CONTENT_LINE_FORMAT = "packContent.lineFormat";
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
		this.jarDirectory = new File(workingDirectory, "packClasspath");
		assert(this.jarDirectory.exists()) : "The classpath directory does not exist: " + this.jarDirectory.getAbsolutePath();
		
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
		
		props.put(PROP_CLASSPATH_COMMANDLINE, getClasspathCommandline());
		props.put(PROP_RESOURCEPATH, getResourcepath());
		
		props.put(PROP_PRODUCT_NAME, this.productName);
		props.put(PROP_PRODUCT_VERSION, this.productVersion);
		props.put(PROP_PRODUCT_VERSION_STANDARDISED, this.productVersionStandardised);
		props.put(PROP_PRODUCT_DESCRIPTION, this.productDescription);
		props.put(PROP_PRODUCT_VENDOR, this.productVendor);
		props.put(PROP_EXECUTABLE_NAME, this.executableName);
		props.put(PROP_START_MENU_PROGRAM_FOLDER_ROOT, this.startMenuProgramFolderRoot);
		props.put(PROP_PROGRAM_FILES_HOME, this.programFilesHome);

		addPlaceholders(props, PROP_PACK_CONTENT, PACK_CONTENT_LINE_FORMAT);
		
		return props;
	}

//> PROPERTY GENERATION
	private String getClasspathCommandline() {
		StringBuilder bob = new StringBuilder();
		File[] jarList = this.getJarlist();
		for (int i = 0; i < jarList.length; i++) {
			if(i > 0) bob.append(';');
			bob.append(jarList[i].getName());
		}
		return bob.toString();
	}
	
	/** @return entry for # */
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
	
	@Deprecated
	private void addPlaceholders(Map<String, String> props, String... propertyKeys) {
		for(String propertyKey : propertyKeys) {
			String replaced = props.put(propertyKey, "[placeholder:" + propertyKey + "]");
			assert(replaced == null) : "Property overwritten: " + propertyKey;
		}
	}

//> STATIC FACTORIES
	/** @return a new {@link JetPackProfile} configured from the supplied directory */
	public static JetPackProfile loadFromDirectory(File profileDirectory) throws IOException {
		Map<String, String> props = PropertyUtils.loadProperties(new File(profileDirectory, "pack.profile.properties"));
		
		JetPackProfile packProfile = new JetPackProfile(profileDirectory,
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
