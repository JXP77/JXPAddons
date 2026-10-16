package com.jxpaddons;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * The blue and gray click GUI: four draggable panels (Combat, Mining, Farming, Misc).
 *
 * - Drag a panel's header to move it.
 * - Click a module to switch it on or off.
 * - Click the +/- at the right of a header to collapse or expand a panel.
 * - Press Esc to close. Positions and module states are saved automatically.
 */
public class ClickGuiScreen extends Screen {

	// ---- Blue and gray theme (ARGB colours) ----
	private static final int COLOR_BACKDROP_TOP = 0xAA14161A;
	private static final int COLOR_BACKDROP_BOTTOM = 0xCC1B2A44;
	private static final int COLOR_PANEL_BG = 0xEE2B2F36;
	private static final int COLOR_HEADER = 0xFF2563EB;
	private static final int COLOR_HEADER_HOVER = 0xFF3B82F6;
	private static final int COLOR_ROW = 0xEE3A3F48;
	private static final int COLOR_ROW_HOVER = 0xEE4B515C;
	private static final int COLOR_ROW_ON = 0xFF2F6FE0;
	private static final int COLOR_ROW_ON_HOVER = 0xFF4A8CFF;
	private static final int COLOR_BORDER = 0xFF6F7886;
	private static final int COLOR_TEXT = 0xFFFFFFFF;
	private static final int COLOR_TEXT_OFF = 0xFFC3C9D4;
	private static final int COLOR_DOT_OFF = 0xFF666E7C;
	private static final int COLOR_TITLE = 0xFF7FB2FF;
	private static final int COLOR_HINT = 0xFF9AA3B2;

	/** Drawn from first to last, so the last panel is on top. Clicking a panel moves it to the end. */
	final List<Panel> panels = new ArrayList<>();

	private Panel dragging;
	private int lastMouseX;
	private int lastMouseY;

	public ClickGuiScreen() {
		super(Component.literal("JXP Addons"));
		for (Category category : Category.values()) {
			this.panels.add(new Panel(category));
		}
	}

	@Override
	protected void init() {
		JXPAddonsClient.LOGGER.info("Click GUI initialised ({}x{})", this.width, this.height);

		// Panels without a saved position are lined up in a row, centred on the screen.
		int gap = 14;
		int count = Category.values().length;
		int total = count * Panel.WIDTH + (count - 1) * gap;
		int startX = Math.max(8, (this.width - total) / 2);

		for (Panel panel : this.panels) {
			if (!panel.hasPosition()) {
				panel.x = startX + panel.category.ordinal() * (Panel.WIDTH + gap);
				panel.y = 40;
			}
			panel.clamp(this.width, this.height);
		}
	}

	// ------------------------------------------------------------------ drawing

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		// Always call super first: it draws the default background.
		super.extractRenderState(graphics, mouseX, mouseY, delta);

		// Remember the mouse position; the click handlers below use it.
		this.lastMouseX = mouseX;
		this.lastMouseY = mouseY;

		graphics.fillGradient(0, 0, this.width, this.height, COLOR_BACKDROP_TOP, COLOR_BACKDROP_BOTTOM);
		drawCentered(graphics, "JXP Addons", this.width / 2, 12, COLOR_TITLE);

		Panel hoveredPanel = panelAt(mouseX, mouseY);
		for (Panel panel : this.panels) {
			drawPanel(graphics, panel, mouseX, mouseY, panel == hoveredPanel);
		}

		if (hoveredPanel != null) {
			Module hoveredModule = hoveredPanel.moduleAt(mouseX, mouseY);
			if (hoveredModule != null) {
				drawCentered(graphics, hoveredModule.getName() + ": " + hoveredModule.getDescription(),
						this.width / 2, this.height - 28, COLOR_TEXT);
			}
		}

		drawCentered(graphics,
				"Drag a header to move a panel  |  Click a module to toggle it  |  +/- collapses  |  Esc closes",
				this.width / 2, this.height - 14, COLOR_HINT);
	}

	private void drawPanel(GuiGraphicsExtractor graphics, Panel panel, int mouseX, int mouseY, boolean hovered) {
		int x = (int) Math.round(panel.x);
		int y = (int) Math.round(panel.y);
		int height = panel.height();

		// Body
		graphics.fill(x, y, x + Panel.WIDTH, y + height, COLOR_PANEL_BG);

		// Header
		boolean headerHovered = hovered && panel.inHeader(mouseX, mouseY);
		graphics.fill(x, y, x + Panel.WIDTH, y + Panel.HEADER_HEIGHT,
				headerHovered ? COLOR_HEADER_HOVER : COLOR_HEADER);
		graphics.text(this.font, panel.category.displayName(), x + 6, y + 5, COLOR_TEXT, true);
		graphics.text(this.font, panel.collapsed ? "+" : "-", x + Panel.WIDTH - 12, y + 5, COLOR_TEXT, true);

		// Module rows
		if (!panel.collapsed) {
			Module hoveredModule = hovered ? panel.moduleAt(mouseX, mouseY) : null;
			int rowY = y + Panel.HEADER_HEIGHT;
			for (Module module : panel.modules) {
				boolean rowHovered = module == hoveredModule;
				int background;
				if (module.isEnabled()) {
					background = rowHovered ? COLOR_ROW_ON_HOVER : COLOR_ROW_ON;
				} else {
					background = rowHovered ? COLOR_ROW_HOVER : COLOR_ROW;
				}
				graphics.fill(x, rowY, x + Panel.WIDTH, rowY + Panel.ROW_HEIGHT, background);

				String label = fit(module.getName(), Panel.WIDTH - 24);
				graphics.text(this.font, label, x + 6, rowY + 4,
						module.isEnabled() ? COLOR_TEXT : COLOR_TEXT_OFF, true);

				// On/off indicator dot
				graphics.fill(x + Panel.WIDTH - 10, rowY + 5, x + Panel.WIDTH - 4, rowY + 11,
						module.isEnabled() ? COLOR_TEXT : COLOR_DOT_OFF);

				rowY += Panel.ROW_HEIGHT;
			}
		}

		// Border
		graphics.outline(x, y, Panel.WIDTH, height, COLOR_BORDER);
	}

	private void drawCentered(GuiGraphicsExtractor graphics, String text, int centerX, int y, int color) {
		graphics.text(this.font, text, centerX - this.font.width(text) / 2, y, color, true);
	}

	/** Shortens text with "..." if it would be wider than maxWidth pixels. */
	private String fit(String text, int maxWidth) {
		if (this.font.width(text) <= maxWidth) {
			return text;
		}
		String result = text;
		while (result.length() > 1 && this.font.width(result + "...") > maxWidth) {
			result = result.substring(0, result.length() - 1);
		}
		return result + "...";
	}

	// ------------------------------------------------------------------ input

	/** The top-most panel under the given point, or null. */
	private Panel panelAt(double mouseX, double mouseY) {
		for (int i = this.panels.size() - 1; i >= 0; i--) {
			Panel panel = this.panels.get(i);
			if (panel.contains(mouseX, mouseY)) {
				return panel;
			}
		}
		return null;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		Panel panel = panelAt(this.lastMouseX, this.lastMouseY);
		if (panel == null) {
			return super.mouseClicked(event, doubleClick);
		}

		// Bring the clicked panel to the front.
		this.panels.remove(panel);
		this.panels.add(panel);

		if (panel.inCollapseButton(this.lastMouseX, this.lastMouseY)) {
			panel.collapsed = !panel.collapsed;
			persist();
		} else if (panel.inHeader(this.lastMouseX, this.lastMouseY)) {
			this.dragging = panel;
		} else {
			Module module = panel.moduleAt(this.lastMouseX, this.lastMouseY);
			if (module != null) {
				module.toggle();
				persist();
			}
		}
		return true;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		if (this.dragging != null) {
			this.dragging.x += dx;
			this.dragging.y += dy;
			this.dragging.clamp(this.width, this.height);
			return true;
		}
		return super.mouseDragged(event, dx, dy);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (this.dragging != null) {
			this.dragging = null;
			persist();
			return true;
		}
		return super.mouseReleased(event);
	}

	@Override
	public void onClose() {
		persist();
		super.onClose();
	}

	/** Saves panel positions, collapsed states and module states to the config file. */
	private void persist() {
		for (Panel panel : this.panels) {
			panel.saveToConfig();
		}
		GuiConfig.save();
	}
}
