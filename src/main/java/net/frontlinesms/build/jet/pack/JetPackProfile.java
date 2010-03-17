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
	/** This is the directory that all paths in the package configuration are relative to. */
	private final File workingDirectory;
	
	private JetPackProfile(File workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public Map<String, String> getSubstitutionProperties() {
		Map<String, String> props = new HashMap<String, String>();
		
		props.put("workingDirectory", workingDirectory.getAbsolutePath());
		
		return props;
	}

//> STATIC FACTORIES
	public static JetPackProfile loadFromDirectory(File profileDirectory) throws IOException {
		Map<String, String> props = PropertyUtils.loadProperties(new File(profileDirectory, "pack.profile.properties"));
		
		JetPackProfile packProfile = new JetPackProfile(profileDirectory);

		// Check all properties used
		assert(props.size() == 0) : "There are " + props.size() + " unused properties.";
		
		return packProfile;
	}
}
