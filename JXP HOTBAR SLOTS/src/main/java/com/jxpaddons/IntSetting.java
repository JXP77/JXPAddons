package com.jxpaddons;

/** A whole-number option shown as a slider. Click or drag across the row to set it. */
public class IntSetting extends Setting {

	private final int defaultValue;
	private final int min;
	private final int max;
	private final int step;
	private final String suffix;
	private int value;

	/**
	 * @param min    smallest value (left end of the slider)
	 * @param max    largest value (right end of the slider)
	 * @param step   the value moves in jumps of this size
	 * @param suffix text added after the number, for example "%" or " px"
	 */
	public IntSetting(String name, int defaultValue, int min, int max, int step, String suffix) {
		super(name);
		this.min = min;
		this.max = max;
		this.step = Math.max(1, step);
		this.suffix = suffix;
		this.defaultValue = snap(defaultValue);
		this.value = this.defaultValue;
	}

	public int get() {
		return this.value;
	}

	public void set(int value) {
		this.value = snap(value);
	}

	/** Forces a value into the min..max range and onto the step grid. */
	private int snap(int raw) {
		int clamped = Math.max(this.min, Math.min(this.max, raw));
		int steps = Math.round((clamped - this.min) / (float) this.step);
		return Math.min(this.max, this.min + steps * this.step);
	}

	@Override
	public String valueText() {
		return this.value + this.suffix;
	}

	@Override
	public void click(boolean leftClick) {
		// Sliders are handled through setSliderFraction by the GUI.
	}

	@Override
	public double sliderFraction() {
		if (this.max == this.min) {
			return 0;
		}
		return (this.value - this.min) / (double) (this.max - this.min);
	}

	@Override
	public void setSliderFraction(double fraction) {
		double f = Math.max(0, Math.min(1, fraction));
		set((int) Math.round(this.min + f * (this.max - this.min)));
	}

	@Override
	void load() {
		this.value = snap(GuiConfig.getInt(configKey(), this.defaultValue));
	}

	@Override
	void store() {
		GuiConfig.setInt(configKey(), this.value);
	}
}
