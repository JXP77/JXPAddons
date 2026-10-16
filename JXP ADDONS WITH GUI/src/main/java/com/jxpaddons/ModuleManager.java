package com.jxpaddons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;

/**
 * Holds every module shown in the click GUI.
 *
 * TO ADD A MODULE: add one line to the static block below, for example
 *     register(new Module(Category.MISC, "My Module", "What it does"));
 * It will appear in the matching panel automatically and its on/off state is saved.
 *
 * The modules below are PLACEHOLDERS: they can be toggled in the GUI but do not do anything yet.
 */
public final class ModuleManager {

	private static final List<Module> MODULES = new ArrayList<>();

	static {
		register(new Module(Category.COMBAT, "Example Combat 1", "Placeholder module. It does nothing yet."));
		register(new Module(Category.COMBAT, "Example Combat 2", "Placeholder module. It does nothing yet."));
		register(new Module(Category.COMBAT, "Example Combat 3", "Placeholder module. It does nothing yet."));

		register(new Module(Category.MINING, "Example Mining 1", "Placeholder module. It does nothing yet."));
		register(new Module(Category.MINING, "Example Mining 2", "Placeholder module. It does nothing yet."));
		register(new Module(Category.MINING, "Example Mining 3", "Placeholder module. It does nothing yet."));

		register(new Module(Category.FARMING, "Example Farming 1", "Placeholder module. It does nothing yet."));
		register(new Module(Category.FARMING, "Example Farming 2", "Placeholder module. It does nothing yet."));
		register(new Module(Category.FARMING, "Example Farming 3", "Placeholder module. It does nothing yet."));

		register(new Module(Category.MISC, "Example Misc 1", "Placeholder module. It does nothing yet."));
		register(new Module(Category.MISC, "Example Misc 2", "Placeholder module. It does nothing yet."));
		register(new Module(Category.MISC, "Example Misc 3", "Placeholder module. It does nothing yet."));
	}

	private ModuleManager() {
	}

	public static void register(Module module) {
		MODULES.add(module);
	}

	public static List<Module> all() {
		return Collections.unmodifiableList(MODULES);
	}

	public static List<Module> inCategory(Category category) {
		List<Module> result = new ArrayList<>();
		for (Module module : MODULES) {
			if (module.getCategory() == category) {
				result.add(module);
			}
		}
		return result;
	}

	/** Runs every enabled module's onTick. Called once per client tick. */
	public static void tick(Minecraft client) {
		for (Module module : MODULES) {
			if (module.isEnabled()) {
				module.onTick(client);
			}
		}
	}
}
