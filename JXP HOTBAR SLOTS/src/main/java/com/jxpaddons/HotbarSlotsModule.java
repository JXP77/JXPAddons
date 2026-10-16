package com.jxpaddons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Hotbar Slots: draws the number of each of the 9 hotbar slots (1 to 9) in the corner of the slot.
 * By default the numbers are grey and sit at the top right of each slot.
 *
 * Right-click the module in the click GUI to change the colour, opacity, corner, number style,
 * shadow and position.
 */
public class HotbarSlotsModule extends Module {

	/** Number of slots in the hotbar. */
	static final int SLOT_COUNT = 9;

	/** Width and height of one slot in GUI pixels. */
	static final int SLOT_SIZE = 20;

	private static final String[] COLOR_NAMES =
			{"Gray", "Dark Gray", "Light Gray", "White", "Blue", "Yellow", "Green", "Red"};
	private static final int[] COLOR_RGB =
			{0xAAAAAA, 0x555555, 0xDDDDDD, 0xFFFFFF, 0x5AA0FF, 0xFFD84A, 0x55FF55, 0xFF5555};

	private static final String[] CORNERS = {"Top Right", "Top Left", "Bottom Right", "Bottom Left"};
	private static final String[] STYLES = {"Numbers", "Roman"};
	private static final String[] ROMAN = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};

	/** Height in pixels of a digit as drawn by Minecraft's font. */
	private static final int TEXT_HEIGHT = 8;

	// ---- Options (shown when you right-click the module in the GUI) ----
	final ChoiceSetting color = addSetting(new ChoiceSetting("Color", "Gray", COLOR_NAMES));
	final IntSetting opacity = addSetting(new IntSetting("Opacity", 100, 10, 100, 10, "%"));
	final ChoiceSetting corner = addSetting(new ChoiceSetting("Corner", "Top Right", CORNERS));
	final ChoiceSetting style = addSetting(new ChoiceSetting("Style", "Numbers", STYLES));
	final BooleanSetting shadow = addSetting(new BooleanSetting("Shadow", true));
	final IntSetting offsetX = addSetting(new IntSetting("Offset X", 0, -6, 6, 1, " px"));
	final IntSetting offsetY = addSetting(new IntSetting("Offset Y", 0, -6, 6, 1, " px"));

	public HotbarSlotsModule() {
		super(Category.MISC, "Hotbar Slots", "Shows the number 1-9 on each hotbar slot. Right-click for options.");
	}

	// ------------------------------------------------------------------ layout helpers

	/** X position (GUI pixels) of the left edge of a hotbar slot. Slot 0 is the first one. */
	static int slotLeft(int screenWidth, int slot) {
		// The hotbar is 182 pixels wide and centred; slots are 20 pixels apart with a 1 pixel frame.
		return screenWidth / 2 - 90 + slot * SLOT_SIZE;
	}

	/** Y position (GUI pixels) of the top edge of the hotbar slots. */
	static int slotTop(int screenHeight) {
		// The hotbar is 22 pixels tall and sits at the very bottom of the screen.
		return screenHeight - 21;
	}

	/** The text drawn for a slot (0-8) in the chosen style. */
	String labelFor(int slot) {
		return this.style.get().equals("Roman") ? ROMAN[slot] : Integer.toString(slot + 1);
	}

	/** The colour as ARGB, combining the chosen colour with the opacity. */
	int argbColor() {
		int rgb = COLOR_RGB[this.color.getIndex()];
		int alpha = Math.round(this.opacity.get() * 2.55f);
		return (alpha << 24) | rgb;
	}

	/** X position of the label text for a slot, given the text width. */
	int labelX(int screenWidth, int slot, int textWidth) {
		int left = slotLeft(screenWidth, slot);
		boolean right = this.corner.get().endsWith("Right");
		int x = right ? left + SLOT_SIZE - textWidth - 1 : left + 2;
		return x + this.offsetX.get();
	}

	/** Y position of the label text for a slot. */
	int labelY(int screenHeight) {
		int top = slotTop(screenHeight);
		boolean bottom = this.corner.get().startsWith("Bottom");
		int y = bottom ? top + SLOT_SIZE - TEXT_HEIGHT - 1 : top + 1;
		return y + this.offsetY.get();
	}

	// ------------------------------------------------------------------ drawing

	/** Called by the HUD every frame (registered in JXPAddonsClient). */
	public void extractHud(GuiGraphicsExtractor graphics) {
		Minecraft mc = Minecraft.getInstance();
		// Spectators have no hotbar, so there is nothing to label.
		if (!isEnabled() || mc.player == null || mc.player.isSpectator()) {
			return;
		}
		drawLabels(graphics, mc);
	}

	/** Draws the nine labels. Split from extractHud so it does not depend on the player. */
	void drawLabels(GuiGraphicsExtractor graphics, Minecraft mc) {
		int screenWidth = mc.getWindow().getGuiScaledWidth();
		int screenHeight = mc.getWindow().getGuiScaledHeight();
		int color = argbColor();
		boolean withShadow = this.shadow.get();
		int y = labelY(screenHeight);

		for (int slot = 0; slot < SLOT_COUNT; slot++) {
			String text = labelFor(slot);
			int x = labelX(screenWidth, slot, mc.font.width(text));
			graphics.text(mc.font, text, x, y, color, withShadow);
		}
	}
}
