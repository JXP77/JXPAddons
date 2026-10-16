package com.jxpaddons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;

/**
 * One toggleable entry in the click GUI.
 *
 * To give a module real behaviour, make a subclass (or an anonymous subclass) and override
 * {@link #onEnable()}, {@link #onDisable()} and/or {@link #onTick(Minecraft)}.
 *
 * To give a module options, call {@link #addSetting(Setting)} in its constructor. The options appear
 * when you right-click the module in the click GUI, and are saved automatically.
 */
public class Module {

	private final Category category;
	private final String name;
	private final String description;
	private final String id;
	private final List<Setting> settings = new ArrayList<>();
	private boolean enabled;
	private boolean settingsOpen;

	public Module(Category category, String name, String description) {
		this.category = category;
		this.name = name;
		this.description = description;
		// Used as the key in the config file, e.g. "combat.examplecombat1"
		this.id = category.name().toLowerCase() + "." + name.toLowerCase().replaceAll("[^a-z0-9]", "");
	}

	public Category getCategory() {
		return this.category;
	}

	public String getName() {
		return this.name;
	}

	public String getDescription() {
		return this.description;
	}

	public String getId() {
		return this.id;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		if (this.enabled == enabled) {
			return;
		}
		this.enabled = enabled;
		if (enabled) {
			onEnable();
		} else {
			onDisable();
		}
	}

	public void toggle() {
		setEnabled(!this.enabled);
	}

	// ------------------------------------------------------------------ settings

	/** Adds an option to this module. Call it from the module's constructor and keep the result. */
	protected <T extends Setting> T addSetting(T setting) {
		setting.bind(this.id);
		this.settings.add(setting);
		return setting;
	}

	public List<Setting> getSettings() {
		return Collections.unmodifiableList(this.settings);
	}

	public boolean hasSettings() {
		return !this.settings.isEmpty();
	}

	/** Whether the option rows are currently shown under this module in the GUI. */
	public boolean isSettingsOpen() {
		return this.settingsOpen && hasSettings();
	}

	public void toggleSettingsOpen() {
		if (hasSettings()) {
			this.settingsOpen = !this.settingsOpen;
		}
	}

	void loadSettings() {
		for (Setting setting : this.settings) {
			setting.load();
		}
	}

	void saveSettings() {
		for (Setting setting : this.settings) {
			setting.store();
		}
	}

	// ------------------------------------------------------------------ hooks

	/** Called when the module is switched on. */
	protected void onEnable() {
	}

	/** Called when the module is switched off. */
	protected void onDisable() {
	}

	/** Called once per client tick while the module is enabled. */
	public void onTick(Minecraft client) {
	}
}
