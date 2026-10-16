package com.jxpaddons;

/** An on/off option. Click it to flip it. */
public class BooleanSetting extends Setting {

	private final boolean defaultValue;
	private boolean value;

	public BooleanSetting(String name, boolean defaultValue) {
		super(name);
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	public boolean get() {
		return this.value;
	}

	public void set(boolean value) {
		this.value = value;
	}

	@Override
	public String valueText() {
		return this.value ? "On" : "Off";
	}

	@Override
	public void click(boolean leftClick) {
		this.value = !this.value;
	}

	@Override
	void load() {
		this.value = GuiConfig.getBoolean(configKey(), this.defaultValue);
	}

	@Override
	void store() {
		GuiConfig.setBoolean(configKey(), this.value);
	}
}
