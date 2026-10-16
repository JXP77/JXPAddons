package com.jxpaddons;

import java.util.ArrayList;
import java.util.List;

/** One draggable window in the click GUI. It shows the modules of a single category. */
final class Panel {

	static final int WIDTH = 128;
	static final int HEADER_HEIGHT = 18;
	static final int ROW_HEIGHT = 16;

	/** Marks a position that has not been chosen yet (no saved value). */
	static final int UNSET = Integer.MIN_VALUE;

	/** One line inside a panel: either a module, or one of an opened module's settings. */
	sealed interface Row permits ModuleRow, SettingRow {
		Module module();
	}

	record ModuleRow(Module module) implements Row {
	}

	record SettingRow(Module module, Setting setting) implements Row {
	}

	final Category category;
	final List<Module> modules;
	double x;
	double y;
	boolean collapsed;

	Panel(Category category) {
		this.category = category;
		this.modules = ModuleManager.inCategory(category);

		String key = configKey();
		this.x = GuiConfig.getInt(key + ".x", UNSET);
		this.y = GuiConfig.getInt(key + ".y", UNSET);
		this.collapsed = GuiConfig.getBoolean(key + ".collapsed", false);
	}

	private String configKey() {
		return "panel." + this.category.name().toLowerCase();
	}

	boolean hasPosition() {
		return this.x != UNSET && this.y != UNSET;
	}

	/** Every line to show, top to bottom. An opened module is followed by its settings. */
	List<Row> rows() {
		List<Row> rows = new ArrayList<>();
		for (Module module : this.modules) {
			rows.add(new ModuleRow(module));
			if (module.isSettingsOpen()) {
				for (Setting setting : module.getSettings()) {
					rows.add(new SettingRow(module, setting));
				}
			}
		}
		return rows;
	}

	int height() {
		return HEADER_HEIGHT + (this.collapsed ? 0 : rows().size() * ROW_HEIGHT);
	}

	boolean contains(double mouseX, double mouseY) {
		return mouseX >= this.x && mouseX < this.x + WIDTH
				&& mouseY >= this.y && mouseY < this.y + height();
	}

	boolean inHeader(double mouseX, double mouseY) {
		return contains(mouseX, mouseY) && mouseY < this.y + HEADER_HEIGHT;
	}

	/** The small +/- area at the right end of the header. */
	boolean inCollapseButton(double mouseX, double mouseY) {
		return inHeader(mouseX, mouseY) && mouseX >= this.x + WIDTH - 20;
	}

	/** Returns the row under the mouse, or null if the mouse is not over a row. */
	Row rowAt(double mouseX, double mouseY) {
		if (this.collapsed || !contains(mouseX, mouseY)) {
			return null;
		}
		double offset = mouseY - this.y - HEADER_HEIGHT;
		if (offset < 0) {
			return null;
		}
		List<Row> rows = rows();
		int index = (int) (offset / ROW_HEIGHT);
		return index >= 0 && index < rows.size() ? rows.get(index) : null;
	}

	/** For a slider row: where the mouse is along the slider, from 0.0 (left end) to 1.0 (right end). */
	double sliderFractionAt(double mouseX) {
		double left = this.x + 4;
		double width = WIDTH - 8;
		return Math.max(0, Math.min(1, (mouseX - left) / width));
	}

	/** Keeps the panel header on screen. */
	void clamp(int screenWidth, int screenHeight) {
		this.x = Math.max(0, Math.min(this.x, screenWidth - WIDTH));
		this.y = Math.max(0, Math.min(this.y, screenHeight - HEADER_HEIGHT));
	}

	void saveToConfig() {
		String key = configKey();
		GuiConfig.setInt(key + ".x", (int) Math.round(this.x));
		GuiConfig.setInt(key + ".y", (int) Math.round(this.y));
		GuiConfig.setBoolean(key + ".collapsed", this.collapsed);
	}
}
