/**
 * 
 */
package net.frontlinesms.build.jet.pack;

import net.frontlinesms.build.jet.compile.JetCompileProfile;

/**
 * @author aga
 */
public class JetPacker {
	/** File encoding used for reading and writing .jpn files. */
	private static final String JPN_FILE_ENCODING = "UTF-16LE";
	
//> INSTANCE PROPERTIES
	private boolean initialised;
	
	public void doPack() {
		assert(this.initialised) : "You cannot pack until the packer has been properly initialised.  Call init() before calling this method.";
		generateJpnFile();
		executeXPack();
	}
	
	public void init(JetCompileProfile compileProfile) {
		assert(!initialised) : "Cannot re-initialise a packer.";
	}
	
//> INSTANCE HELPER METHODS
	private void generateJpnFile() {}
	private void executeXPack() {}
}
