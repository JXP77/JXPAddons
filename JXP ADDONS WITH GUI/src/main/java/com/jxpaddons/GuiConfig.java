package com.jxpaddons;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Saves panel positions and module on/off states to config/jxpaddons.properties,
 * so the GUI looks the same the next time you start the game.
 */
public final class GuiConfig {

	private static final Properties PROPS = new Properties();

	private GuiConfig() {
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("jxpaddons.properties");
	}

	/** Reads the config file (if there is one) and applies the saved module states. */
	public static void load() {
		Path file = file();
		if (Files.exists(file)) {
			try (BufferedReader reader = Files.newBufferedReader(file)) {
				PROPS.load(reader);
			} catch (IOException e) {
				JXPAddonsClient.LOGGER.warn("Could not read the JXPAddons config file", e);
			}
		}
		for (Module module : ModuleManager.all()) {
			module.setEnabled(getBoolean("module." + module.getId() + ".enabled", false));
		}
	}

	/** Writes module states and any values set with setInt/setBoolean to disk. */
	public static void save() {
		for (Module module : ModuleManager.all()) {
			setBoolean("module." + module.getId() + ".enabled", module.isEnabled());
		}
		Path file = file();
		try {
			Files.createDirectories(file.getParent());
			try (BufferedWriter writer = Files.newBufferedWriter(file)) {
				PROPS.store(writer, "JXP Addons settings");
			}
		} catch (IOException e) {
			JXPAddonsClient.LOGGER.warn("Could not save the JXPAddons config file", e);
		}
	}

	public static int getInt(String key, int fallback) {
		String value = PROPS.getProperty(key);
		if (value == null) {
			return fallback;
		}
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	public static void setInt(String key, int value) {
		PROPS.setProperty(key, Integer.toString(value));
	}

	public static boolean getBoolean(String key, boolean fallback) {
		String value = PROPS.getProperty(key);
		return value == null ? fallback : Boolean.parseBoolean(value.trim());
	}

	public static void setBoolean(String key, boolean value) {
		PROPS.setProperty(key, Boolean.toString(value));
	}
}
