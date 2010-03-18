/**
 * 
 */
package net.frontlinesms.build.jet.pack;

import java.io.File;
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
	
//> CONSTRUCTORS
	private JetPackProfile(File workingDirectory, String productName,
				String productVersion, String productVersionStandardised,
				String productDescription, String productVendor) {
		this.workingDirectory = workingDirectory;
		this.productName = productName;
		this.productVersion = productVersion;
		this.productVersionStandardised = productVersionStandardised;
		this.productDescription = productDescription;
		this.productVendor = productVendor;
	}
	
//> INSTANCE METHODS
	public Map<String, String> getSubstitutionProperties() {
		Map<String, String> props = new HashMap<String, String>();
		
		props.put(PROP_WORKING_DIRECTORY, workingDirectory.getAbsolutePath());
		props.put(PROP_PRODUCT_NAME, this.productName);
		props.put(PROP_PRODUCT_VERSION, this.productVersion);
		props.put(PROP_PRODUCT_VERSION_STANDARDISED, this.productVersionStandardised);
		props.put(PROP_PRODUCT_DESCRIPTION, this.productDescription);
		props.put(PROP_PRODUCT_VENDOR, this.productVendor);

		addPlaceholders(props, PROP_PACK_CONTENT, PACK_CONTENT_LINE_FORMAT,
				PROP_RESOURCEPATH, PROP_CLASSPATH_COMMANDLINE,
				PROP_EXECUTABLE_NAME,
				PROP_PROGRAM_FILES_HOME, PROP_START_MENU_PROGRAM_FOLDER_ROOT);
		
		return props;
	}

	//> PROPERTY GENERATION
	@Deprecated
	private void addPlaceholders(Map<String, String> props, String... propertyKeys) {
		for(String propertyKey : propertyKeys) {
			String replaced = props.put(propertyKey, "[placehodler:" + propertyKey + "]");
			assert(replaced == null) : "Property overwritten: " + propertyKey;
		}
	}

//> STATIC FACTORIES
	/** @return a new {@link JetPackProfile} configured from the supplied directory */
	public static JetPackProfile loadFromDirectory(File profileDirectory) throws IOException {
		Map<String, String> props = PropertyUtils.loadProperties(new File(profileDirectory, "pack.profile.properties"));
		
		JetPackProfile packProfile = new JetPackProfile(profileDirectory,
				props.get(PROP_PRODUCT_NAME),
				props.get(PROP_PRODUCT_VERSION),
				props.get(PROP_PRODUCT_VERSION_STANDARDISED),
				props.get(PROP_PRODUCT_DESCRIPTION),
				props.get(PROP_PRODUCT_VENDOR)
				);

		// Check all properties used
		assert(props.size() == 0) : "There are " + props.size() + " unused properties.";
		
		return packProfile;
	}
}
