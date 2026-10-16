# JXP Addons (Fabric, Minecraft Java 26.1.2)

A client-side mod with a blue and gray click GUI. Open it with **Right Shift** (rebindable in Options > Controls > Key Binds > JXP Addons) or the chat command `/jxp`.

The GUI has four draggable panels: **Combat**, **Mining**, **Farming** and **Misc**.

- Drag a panel's header to move it.
- Left-click a module to switch it on or off.
- Right-click a module that has a `>` arrow to open its settings (the arrow turns into `v`). Right-click again to close them.
- In settings: left-click flips a toggle or goes to the next choice, right-click goes back a choice, and sliders can be clicked or dragged.
- Click the +/- at the right of a header to collapse or expand a panel.
- Esc closes the GUI. Panel positions, module states and settings are saved to `config/jxpaddons.properties`.

## Modules

| Panel | Module | What it does |
| --- | --- | --- |
| Misc | **Auto Sprint** | Sprints automatically while you hold W. It follows normal sprint rules: no sprinting while sneaking or using an item, when pushing into a wall, or when hunger is 3 drumsticks or less. |
| Misc | **Hotbar Slots** | Draws the numbers 1-9 on the nine hotbar slots, grey, in the top right corner of each slot. Right-click it for options (below). |

### Hotbar Slots options

| Option | Choices | Default |
| --- | --- | --- |
| Color | Gray, Dark Gray, Light Gray, White, Blue, Yellow, Green, Red | Gray |
| Opacity | 10% to 100% (slider) | 100% |
| Corner | Top Right, Top Left, Bottom Right, Bottom Left | Top Right |
| Style | Numbers (1-9) or Roman (I-IX) | Numbers |
| Shadow | On / Off | On |
| Offset X / Offset Y | -6 to 6 pixels (slider), to fine-tune the position | 0 |

Settings are saved in `config/jxpaddons.properties` as `module.misc.hotbarslots.setting.*`.

Every other module ("Example Combat 1" and so on) is a placeholder. It toggles in the GUI but does nothing yet.

## Build it

1. Install JDK 25 and make sure `java -version` says 25. On macOS: `brew install --cask temurin@25`, then `export JAVA_HOME=$(/usr/libexec/java_home -v 25)`.
2. In this folder run `./gradlew build` (Windows: `gradlew.bat build`). On macOS you may need `chmod +x gradlew` first.
3. The build produces exactly one jar: `build/libs/jxpaddons-1.0.0.jar`.

To try it without installing: `./gradlew runClient`.

## Install it in Minecraft (the mods folder)

You need all four of these:

1. **Minecraft 26.1.2** with the **Fabric Loader** installed (use the Fabric installer from fabricmc.net, then pick the "fabric-loader-26.1.2" profile in the launcher).
2. **Fabric API** for 26.1.2 (download from Modrinth or CurseForge). `./gradlew runClient` supplies it automatically, but the real game does not.
3. **`jxpaddons-1.0.0.jar`** from `build/libs`.
4. Both jars in the `mods` folder:
   - macOS: `~/Library/Application Support/minecraft/mods`
   - Windows: `%appdata%\.minecraft\mods`
   - Linux: `~/.minecraft/mods`

Delete any older copies first: old `jxpaddons`, `jpxaddons` or `parry-guide` jars. Two jars for the same mod stop it loading.

Start the **Fabric** profile. When you join a world, chat shows **"JXP Addons loaded. Press Right Shift or type /jxp to open the menu."** If you see that line, the mod is working. (To hide it, set `joinMessage=false` in `config/jxpaddons.properties`.)

## If it still does not work in the mods folder

| What you see | Meaning | Fix |
| --- | --- | --- |
| A Fabric error window or the game closes at startup, mentioning `fabric-api` | Fabric API is missing | Put the Fabric API jar for 26.1.2 in `mods` |
| An error window mentioning "duplicate" / the same mod twice | Two jars of this mod are in `mods` | Keep only one `jxpaddons` jar and delete the rest |
| An error window mentioning `minecraft`, `fabricloader` or `java` | Wrong game, Loader or Java version | Use Minecraft 26.1.x, Fabric Loader 0.19.2 or newer, and the Java that the launcher bundles |
| The game starts but there is no "JXP Addons loaded" line | The mod is not loaded | You are probably on a profile without Fabric. Check the title screen's Mods list, or `latest.log` for `jxpaddons` |
| The line appears but Right Shift does nothing | Key is bound to something else | Rebind it under Controls > Key Binds > JXP Addons, or use `/jxp` |

The log is `.minecraft/logs/latest.log` (macOS: `~/Library/Application Support/minecraft/logs/latest.log`). Search it for `jxpaddons`. Healthy order: `JXP Addons is loading` -> `Registered the /jxp and /parry commands` (each time you join a world) -> `Open-GUI command was run` -> `Opening the click GUI` -> `Click GUI initialised`.

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
    AutoSprintModule.java  the Auto Sprint module (a good example to copy)
    HotbarSlotsModule.java Hotbar Slots: HUD drawing plus a list of settings (example of module options)
    Setting.java, BooleanSetting.java, IntSetting.java, ChoiceSetting.java   the options shown on right-click
    ModuleManager.java     the list of modules (add yours here)
    GuiConfig.java         saves positions and states
```
