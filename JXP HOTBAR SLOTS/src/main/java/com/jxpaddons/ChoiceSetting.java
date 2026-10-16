package com.jxpaddons;

import java.util.List;

/** An option where you pick one entry from a list. Left-click = next entry, right-click = previous. */
public class ChoiceSetting extends Setting {

	private final List<String> options;
	private final int defaultIndex;
	private int index;

	public ChoiceSetting(String name, String defaultOption, String... options) {
		super(name);
		this.options = List.of(options);
		int found = this.options.indexOf(defaultOption);
		this.defaultIndex = Math.max(0, found);
		this.index = this.defaultIndex;
	}

	/** The currently selected entry, for example "Gray". */
	public String get() {
		return this.options.get(this.index);
	}

	/** The position of the selected entry in the list (0 is the first). */
	public int getIndex() {
		return this.index;
	}

	public void setIndex(int index) {
		this.index = Math.floorMod(index, this.options.size());
	}

	@Override
	public String valueText() {
		return get();
	}

	@Override
	public void click(boolean leftClick) {
		setIndex(this.index + (leftClick ? 1 : -1));
	}

	@Override
	void load() {
		String saved = GuiConfig.getString(configKey(), this.options.get(this.defaultIndex));
		int found = this.options.indexOf(saved);
		this.index = found >= 0 ? found : this.defaultIndex;
	}

	@Override
	void store() {
		GuiConfig.setString(configKey(), get());
	}
}
