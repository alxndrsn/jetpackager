package net.frontlinesms.build.jet;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import net.frontlinesms.build.jet.compile.JetCompiler;

public class FileUtils {

	public static final String[] readFileFromClasspath(String filename, String encoding) throws IOException {
		InputStream is = JetCompiler.class.getResourceAsStream(filename);
		InputStreamReader isr = new InputStreamReader(is, encoding);
		BufferedReader reader = new BufferedReader(isr);
		
		String line;
		List<String> lines = new ArrayList<String>();
		while((line = reader.readLine()) != null) {
			lines.add(line);
		}
		return lines.toArray(new String[lines.size()]);
	}
	
	public static final void writeFile(File targetFile, String encoding, String[] fileContent) throws IOException {
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		BufferedWriter writer = null;
		try {
			fos = new FileOutputStream(targetFile);
			osw = new OutputStreamWriter(fos, encoding);
			writer = new BufferedWriter(osw);
			for(String line : fileContent) {
				writer.write(line);
				writer.write('\n');
			}
		} finally {
			if(writer != null) try { writer.close(); } catch(IOException ex) {}
			if(osw != null) try { osw.close(); } catch(IOException ex) {}
			if(fos != null) try { fos.close(); } catch(IOException ex) {}
		}
	}

}
