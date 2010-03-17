/**
 * 
 */
package net.frontlinesms.build.jet.pack;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import net.frontlinesms.build.jet.PropertyUtils;

/**
 * @author aga
 *
 */
public class JetPackProfile {
	
	private JetPackProfile() {}

	public Map<String, String> getSubstitutionProperties() {
		// TODO Auto-generated method stub
		return Collections.emptyMap();
	}

//> STATIC FACTORIES
	public static JetPackProfile loadFromDirectory(File profileDirectory) throws IOException {
		Map<String, String> props = PropertyUtils.loadProperties(new File(profileDirectory, "pack.profile.properties"));
		
		JetPackProfile packProfile = new JetPackProfile();

		// Check all properties used
		assert(props.size() == 0) : "There are " + props.size() + " unused properties.";
		
		return packProfile;
	}
}
