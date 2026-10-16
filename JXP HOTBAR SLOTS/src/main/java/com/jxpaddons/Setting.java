package com.jxpaddons;

/**
 * One option of a module, shown when you right-click the module in the click GUI.
 * There are three kinds: {@link BooleanSetting} (on/off), {@link IntSetting} (a slider) and
 * {@link ChoiceSetting} (pick from a list). Values are saved in config/jxpaddons.properties.
 */
public abstract class Setting {

	private final String name;
	private final String id;
	private String ownerId = "";

	protected Setting(String name) {
		this.name = name;
		// Used as part of the key in the config file, e.g. "color" or "offsetx"
		this.id = name.toLowerCase().replaceAll("[^a-z0-9]", "");
	}

	public String getName() {
		return this.name;
	}

	/** Called by {@link Module#addSetting} so the setting knows which module it belongs to. */
	final void bind(String moduleId) {
		this.ownerId = moduleId;
	}

	/** The key used in the config file, e.g. "module.misc.hotbarslots.setting.color". */
	protected final String configKey() {
		return "module." + this.ownerId + ".setting." + this.id;
	}

	/** The text shown after the name, for example "On", "Gray" or "80%". */
	public abstract String valueText();

	/** Handles a click on the setting's row. leftClick is false for a right-click. */
	public abstract void click(boolean leftClick);

	/** Reads the saved value from the config. */
	abstract void load();

	/** Writes the current value to the config (the file itself is written by GuiConfig.save()). */
	abstract void store();

	/**
	 * For settings drawn as a slider: how full the slider is, from 0.0 to 1.0.
	 * Returns a negative number for settings that are not sliders.
	 */
	public double sliderFraction() {
		return -1;
	}

	/** Sets a slider setting from a position between 0.0 (left end) and 1.0 (right end). */
	public void setSliderFraction(double fraction) {
	}

	public boolean isSlider() {
		return sliderFraction() >= 0;
	}
}
