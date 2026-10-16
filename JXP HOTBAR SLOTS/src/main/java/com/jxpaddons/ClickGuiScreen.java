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
 * - Left-click a module to switch it on or off.
 * - Right-click a module to open or close its settings (modules with a > or v arrow have settings).
 * - Settings: left-click flips or advances them, right-click goes back, sliders can be dragged.
 * - Click the +/- at the right of a header to collapse or expand a panel.
 * - Press Esc to close. Positions, module states and settings are saved automatically.
 */
public class ClickGuiScreen extends Screen {

	private static final int BUTTON_LEFT = 0;
	private static final int BUTTON_RIGHT = 1;

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
	private static final int COLOR_SETTING_ROW = 0xEE30343C;
	private static final int COLOR_SETTING_ROW_HOVER = 0xEE3F444F;
	private static final int COLOR_SLIDER_FILL = 0xCC2F6FE0;
	private static final int COLOR_BORDER = 0xFF6F7886;
	private static final int COLOR_TEXT = 0xFFFFFFFF;
	private static final int COLOR_TEXT_OFF = 0xFFC3C9D4;
	private static final int COLOR_DOT_OFF = 0xFF666E7C;
	private static final int COLOR_TITLE = 0xFF7FB2FF;
	private static final int COLOR_HINT = 0xFF9AA3B2;

	/** Drawn from first to last, so the last panel is on top. Clicking a panel moves it to the end. */
	final List<Panel> panels = new ArrayList<>();

	private Panel dragging;
	private Panel sliderPanel;
	private Setting sliderSetting;
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

		// Panels without a saved position are placed in a row, centred on the screen.
		// If the screen is too narrow for one row, they wrap onto a second row.
		int gap = 10;
		int margin = 8;
		int count = Category.values().length;
		int perRow = Math.max(1, Math.min(count, (this.width - 2 * margin + gap) / (Panel.WIDTH + gap)));
		int rowWidth = perRow * Panel.WIDTH + (perRow - 1) * gap;
		int startX = Math.max(margin, (this.width - rowWidth) / 2);

		for (Panel panel : this.panels) {
			if (!panel.hasPosition()) {
				int index = panel.category.ordinal();
				panel.x = startX + (index % perRow) * (Panel.WIDTH + gap);
				panel.y = 40 + (index / perRow) * 100;
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
			String tooltip = tooltipFor(hoveredPanel.rowAt(mouseX, mouseY));
			if (tooltip != null) {
				drawCentered(graphics, tooltip, this.width / 2, this.height - 28, COLOR_TEXT);
			}
		}

		drawCentered(graphics,
				"Drag a header to move  |  Left-click toggles  |  Right-click opens settings  |  Esc closes",
				this.width / 2, this.height - 14, COLOR_HINT);
	}

	private String tooltipFor(Panel.Row row) {
		if (row instanceof Panel.ModuleRow moduleRow) {
			Module module = moduleRow.module();
			String text = module.getName() + ": " + module.getDescription();
			return module.hasSettings() ? text + " (right-click for settings)" : text;
		}
		if (row instanceof Panel.SettingRow settingRow) {
			Setting setting = settingRow.setting();
			if (setting.isSlider()) {
				return setting.getName() + ": click or drag to set";
			}
			if (setting instanceof ChoiceSetting) {
				return setting.getName() + ": left-click = next, right-click = previous";
			}
			return setting.getName() + ": click to switch";
		}
		return null;
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

		// Rows (modules, and the settings of any module that has been right-clicked open)
		if (!panel.collapsed) {
			Panel.Row hoveredRow = hovered ? panel.rowAt(mouseX, mouseY) : null;
			int rowY = y + Panel.HEADER_HEIGHT;
			for (Panel.Row row : panel.rows()) {
				boolean rowHovered = row.equals(hoveredRow);
				if (row instanceof Panel.ModuleRow moduleRow) {
					drawModuleRow(graphics, moduleRow.module(), x, rowY, rowHovered);
				} else if (row instanceof Panel.SettingRow settingRow) {
					drawSettingRow(graphics, settingRow.setting(), x, rowY, rowHovered);
				}
				rowY += Panel.ROW_HEIGHT;
			}
		}

		// Border
		graphics.outline(x, y, Panel.WIDTH, height, COLOR_BORDER);
	}

	private void drawModuleRow(GuiGraphicsExtractor graphics, Module module, int x, int rowY, boolean rowHovered) {
		int background;
		if (module.isEnabled()) {
			background = rowHovered ? COLOR_ROW_ON_HOVER : COLOR_ROW_ON;
		} else {
			background = rowHovered ? COLOR_ROW_HOVER : COLOR_ROW;
		}
		graphics.fill(x, rowY, x + Panel.WIDTH, rowY + Panel.ROW_HEIGHT, background);

		int textColor = module.isEnabled() ? COLOR_TEXT : COLOR_TEXT_OFF;
		String label = fit(module.getName(), Panel.WIDTH - (module.hasSettings() ? 36 : 24));
		graphics.text(this.font, label, x + 6, rowY + 4, textColor, true);

		// Arrow: shown only for modules that have settings. > = closed, v = open.
		if (module.hasSettings()) {
			graphics.text(this.font, module.isSettingsOpen() ? "v" : ">", x + Panel.WIDTH - 21, rowY + 4,
					textColor, true);
		}

		// On/off indicator dot
		graphics.fill(x + Panel.WIDTH - 10, rowY + 5, x + Panel.WIDTH - 4, rowY + 11,
				module.isEnabled() ? COLOR_TEXT : COLOR_DOT_OFF);
	}

	private void drawSettingRow(GuiGraphicsExtractor graphics, Setting setting, int x, int rowY, boolean rowHovered) {
		graphics.fill(x, rowY, x + Panel.WIDTH, rowY + Panel.ROW_HEIGHT,
				rowHovered ? COLOR_SETTING_ROW_HOVER : COLOR_SETTING_ROW);

		// Sliders show how full they are as a blue bar behind the text.
		if (setting.isSlider()) {
			int barWidth = (int) Math.round((Panel.WIDTH - 8) * setting.sliderFraction());
			if (barWidth > 0) {
				graphics.fill(x + 4, rowY + 2, x + 4 + barWidth, rowY + Panel.ROW_HEIGHT - 2, COLOR_SLIDER_FILL);
			}
		}

		boolean off = setting instanceof BooleanSetting booleanSetting && !booleanSetting.get();
		String label = fit(setting.getName() + ": " + setting.valueText(), Panel.WIDTH - 16);
		graphics.text(this.font, label, x + 8, rowY + 4, off ? COLOR_TEXT_OFF : COLOR_TEXT, true);
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

		boolean left = event.button() == BUTTON_LEFT;
		boolean right = event.button() == BUTTON_RIGHT;
		if (!left && !right) {
			return true;
		}

		if (panel.inCollapseButton(this.lastMouseX, this.lastMouseY)) {
			if (left) {
				panel.collapsed = !panel.collapsed;
				persist();
			}
			return true;
		}
		if (panel.inHeader(this.lastMouseX, this.lastMouseY)) {
			if (left) {
				this.dragging = panel;
			}
			return true;
		}

		Panel.Row row = panel.rowAt(this.lastMouseX, this.lastMouseY);
		if (row instanceof Panel.ModuleRow moduleRow) {
			if (left) {
				moduleRow.module().toggle();
			} else {
				moduleRow.module().toggleSettingsOpen();
			}
			persist();
		} else if (row instanceof Panel.SettingRow settingRow) {
			Setting setting = settingRow.setting();
			if (setting.isSlider()) {
				if (left) {
					this.sliderPanel = panel;
					this.sliderSetting = setting;
					setting.setSliderFraction(panel.sliderFractionAt(this.lastMouseX));
				}
			} else {
				setting.click(left);
			}
			persist();
		}
		return true;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		if (this.sliderSetting != null) {
			this.sliderSetting.setSliderFraction(this.sliderPanel.sliderFractionAt(this.lastMouseX));
			return true;
		}
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
		if (this.sliderSetting != null || this.dragging != null) {
			this.sliderSetting = null;
			this.sliderPanel = null;
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

	/** Saves panel positions, collapsed states, module states and settings to the config file. */
	private void persist() {
		for (Panel panel : this.panels) {
			panel.saveToConfig();
		}
		GuiConfig.save();
	}
}
