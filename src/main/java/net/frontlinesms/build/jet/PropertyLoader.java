/**
 * 
 */
package net.frontlinesms.build.jet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author aga
 *
 */


class PropertyLoader {
	public static final Map<String, String> loadProperties(File file) throws IOException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			Map<String, String> props = PropertyLoader.loadProperties(fis);
			return props;
		} finally {
			if(fis != null) try { fis.close(); } catch(IOException ex) {}
		}
	}
	
	public static final Map<String, String> loadProperties(InputStream in) throws IOException {
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
