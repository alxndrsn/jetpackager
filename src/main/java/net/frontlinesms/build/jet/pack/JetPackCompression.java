package net.frontlinesms.build.jet.pack;

public enum JetPackCompression {
	NONE(0),
	NORMAL(1),
	HIGH(2);
	
	private final int level;
	
	private JetPackCompression(int level) {
		this.level = level;
	}
	
	public int getLevel() {
		return level;
	}

	public static JetPackCompression getFromLevel(String level) {
		return getFromLevel(Integer.parseInt(level));
	}

	public static JetPackCompression getFromLevel(int level) {
		for(JetPackCompression compression : JetPackCompression.values()) {
			if(compression.getLevel() == level) {
				return compression;
			}
		}
		throw new IllegalStateException("Unrecognized compression level: " + level);
	}
}
