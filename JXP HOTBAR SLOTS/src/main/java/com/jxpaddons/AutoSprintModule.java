package com.jxpaddons;

import net.minecraft.client.Minecraft;

/**
 * Auto Sprint: while you hold W (the forward key) you sprint automatically, so you never have to
 * double-tap W or hold the sprint key.
 *
 * It follows the same rules as normal sprinting. It does not start a sprint while you are sneaking,
 * eating or using an item, pushing into a wall, or when your hunger is too low (3 drumsticks or less).
 */
public class AutoSprintModule extends Module {

	/** Minecraft only lets you start sprinting when the food level is above 6 (out of 20). */
	private static final int MIN_FOOD_LEVEL_TO_SPRINT = 6;

	public AutoSprintModule() {
		super(Category.MISC, "Auto Sprint", "Sprints automatically while you hold W.");
	}

	@Override
	public void onTick(Minecraft client) {
		var player = client.player;
		if (player == null) {
			return;
		}

		// Only act while the forward key (W by default) is held down.
		if (!client.options.keyUp.isDown()) {
			return;
		}

		// Already sprinting: nothing to do.
		if (player.isSprinting()) {
			return;
		}

		// Do not start a sprint in situations where Minecraft would not allow one.
		if (player.isShiftKeyDown() || player.isUsingItem() || player.horizontalCollision) {
			return;
		}
		if (player.getFoodData().getFoodLevel() <= MIN_FOOD_LEVEL_TO_SPRINT) {
			return;
		}

		player.setSprinting(true);
	}
}
