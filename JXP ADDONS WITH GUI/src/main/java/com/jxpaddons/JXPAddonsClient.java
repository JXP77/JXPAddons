package com.jxpaddons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/**
 * Entry point of the mod. Fabric calls {@link #onInitializeClient()} once when the game starts
 * (it is listed under "entrypoints" -> "client" in fabric.mod.json).
 */
public class JXPAddonsClient implements ClientModInitializer {

	public static final String MOD_ID = "jxpaddons";

	// Messages appear in .minecraft/logs/latest.log with the tag [jxpaddons]
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Set to true by the key or the command; the GUI is opened on the next client tick.
	// Only ever touched from the client thread, so no locking is needed.
	private static boolean openRequested = false;

	private static KeyMapping openGuiKey;

	@Override
	public void onInitializeClient() {
		LOGGER.info("JXPAddons is loading");

		// Load saved module states and panel positions.
		GuiConfig.load();

		// Keybind: Right Shift opens the GUI (change it in Options > Controls > Key Binds > JXPAddons).
		KeyMapping.Category category = KeyMapping.Category.register(
				Identifier.fromNamespaceAndPath(MOD_ID, "main"));
		openGuiKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.jxpaddons.open_gui",
				InputConstants.Type.KEYSYM,
				InputConstants.KEY_RSHIFT,
				category));

		// Chat commands. Fabric fires this every time you join a world.
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {
			JxpCommand.register(dispatcher);
			LOGGER.info("Registered the /jxp and /parry commands");
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openGuiKey.consumeClick()) {
				// Only open from the key while in a world.
				if (client.player != null) {
					openRequested = true;
				}
			}

			// Opening the screen here (instead of directly inside the command) makes sure the chat window
			// has finished closing first, so it can't close our screen again.
			if (openRequested) {
				openRequested = false;
				LOGGER.info("Opening the click GUI");
				client.setScreen(new ClickGuiScreen());
			}

			ModuleManager.tick(client);
		});
	}

	/** Asks the mod to open the click GUI on the next tick. */
	public static void requestOpenGui() {
		openRequested = true;
	}
}
