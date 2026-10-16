package com.jxpaddons;

/** The four sections of the click GUI. Each one becomes a draggable panel. */
public enum Category {
	COMBAT("Combat"),
	MINING("Mining"),
	FARMING("Farming"),
	MISC("Misc");

	private final String displayName;

	Category(String displayName) {
		this.displayName = displayName;
	}

	public String displayName() {
		return this.displayName;
	}
}
