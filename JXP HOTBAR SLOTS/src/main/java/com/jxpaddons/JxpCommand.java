package com.jxpaddons;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

/** Chat commands that open the click GUI: /jxp (and /parry, kept from the earlier version). */
public final class JxpCommand {

	private JxpCommand() {
	}

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		Command<FabricClientCommandSource> open = context -> {
			JXPAddonsClient.LOGGER.info("Open-GUI command was run");
			// This chat line proves the command ran, even if the window fails to show.
			context.getSource().sendFeedback(Component.literal("Opening JXP Addons..."));
			JXPAddonsClient.requestOpenGui();
			return Command.SINGLE_SUCCESS;
		};

		dispatcher.register(ClientCommands.literal("jxp").executes(open));
		dispatcher.register(ClientCommands.literal("parry").executes(open));
	}
}
