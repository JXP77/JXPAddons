# JXP Addons (Fabric, Minecraft Java 26.1.2)

A client-side mod with a blue and gray click GUI. Open it with **Right Shift** (rebindable in Options > Controls > Key Binds > JXP Addons) or the chat command `/jxp`.

The GUI has four draggable panels: **Combat**, **Mining**, **Farming** and **Misc**.

- Drag a panel's header to move it.
- Click a module to switch it on or off.
- Click the +/- at the right of a header to collapse or expand a panel.
- Esc closes the GUI. Panel positions and module states are saved to `config/jxpaddons.properties`.

The modules that ship with it are placeholders (Example Combat 1, and so on). They toggle in the GUI but do nothing yet.

## What you need

| Thing | Version |
| --- | --- |
| Minecraft Java Edition | 26.1.2 |
| Fabric Loader | 0.19.5 or newer |
| Fabric API | 0.155.3+26.1.2 (or any build for 26.1.2) |
| JDK (Java) | 25 |

## Build it (macOS / Linux / Windows)

1. Install JDK 25 and make sure `java -version` says 25. On macOS: `brew install --cask temurin@25`, then `export JAVA_HOME=$(/usr/libexec/java_home -v 25)`.
2. In this folder run `./gradlew build` (Windows: `gradlew.bat build`). On macOS you may need `chmod +x gradlew` first.
3. Your mod is `build/libs/jxpaddons-1.0.0.jar` (ignore the `-sources` jar).
4. Put that jar **and Fabric API** in your `mods` folder, launch the Fabric 26.1.2 profile, join a world and press Right Shift.

To try it without installing: `./gradlew runClient`.

## Adding your own modules

Open `ModuleManager.java` and add a line in the static block:

```java
register(new Module(Category.MISC, "My Module", "What it does"));
```

It appears in the matching panel and its on/off state is saved. To make a module actually do something, override `onEnable()`, `onDisable()` or `onTick(Minecraft)` in an anonymous subclass:

```java
register(new Module(Category.MISC, "My Module", "What it does") {
	@Override
	public void onTick(Minecraft client) {
		// runs every tick while the module is on
	}
});
```

## Project layout

```
src/main/resources/fabric.mod.json          mod id, entrypoint, dependencies
src/main/resources/assets/jxpaddons/lang    key bind names
src/main/java/com/jxpaddons/
    JXPAddonsClient.java   entrypoint: key bind, /jxp command, tick loop
    JxpCommand.java        /jxp (and /parry) commands
    ClickGuiScreen.java    the GUI: drawing, dragging, clicking, colours at the top
    Panel.java             one draggable panel
    Category.java          Combat, Mining, Farming, Misc
    Module.java            one toggleable feature
    ModuleManager.java     the list of modules (add yours here)
    GuiConfig.java         saves positions and states
```

## Troubleshooting

Logs: `.minecraft/logs/latest.log` (macOS: `~/Library/Application Support/minecraft/logs/latest.log`). Search for `jxpaddons`.

| What you see | Meaning | Fix |
| --- | --- | --- |
| "Unknown or incomplete command" and no `jxpaddons` log lines | The mod is not loaded | Use `jxpaddons-1.0.0.jar` (not `-sources`), put it in `mods` with Fabric API, and use the **Fabric** 26.1.2 profile |
| Game will not start | Wrong Loader or Java | Fabric Loader 0.19.5+ and Java 25 |
| Chat says "Opening JXP Addons..." but no window | Command ran, screen did not show | Send the `jxpaddons` and nearby `ERROR` lines from `latest.log` |

Healthy log order: `JXP Addons is loading` -> `Registered the /jxp and /parry commands` (each time you join a world) -> `Open-GUI command was run` -> `Opening the click GUI` -> `Click GUI initialised`.
