package com.jxpaddons;

import net.minecraft.client.Minecraft;

/**
 * One toggleable entry in the click GUI.
 *
 * To give a module real behaviour, make a subclass (or an anonymous subclass) and override
 * {@link #onEnable()}, {@link #onDisable()} and/or {@link #onTick(Minecraft)}.
 */
public class Module {

	private final Category category;
	private final String name;
	private final String description;
	private final String id;
	private boolean enabled;

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
