/**
 * 
 */
package net.frontlinesms.build.jet.pack;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import net.frontlinesms.build.jet.PropertyLoader;

/**
 * @author aga
 *
 */
public class JetPackProfile {
	
	private JetPackProfile() {}

//> STATIC FACTORIES
	public static JetPackProfile loadFromDirectory(File profileDirectory) throws IOException {
		Map<String, String> props = PropertyLoader.loadProperties(new File(profileDirectory, "pack.profile.properties"));
		
		JetPackProfile packProfile = new JetPackProfile();

		// Check all properties used
		assert(props.size() == 0) : "There are " + props.size() + " unused properties.";
		
		return packProfile;
	}
}
