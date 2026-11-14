# FPV Drones to Minecraft: unofficial Fabric port

An unofficial port of **FPV Drones to Minecraft** 1.1.4 (Forge 1.20.1) by **JagerGe** (programmer, modeller)
and **Mr.Mulle** (designer, icon artist) to **Minecraft 26.3 on Fabric**. All of the mod's design, models,
textures, sounds and flight model are theirs; this repository only carries it to a newer game and loader.

> This port is not made or endorsed by the original authors. Please report problems with it
> [here](../../issues), not to them. It is licensed, like the original, under the
> [PolyForm Noncommercial License 1.0.0](LICENSE.md): **no commercial use**.

## Status

Work in progress. The goal is **full feature parity** with 1.1.4: every item, recipe, screen, setting and
effect the original has, on current Minecraft. [docs/PARITY.md](docs/PARITY.md) lists each original feature
and where it stands in the port.

What works today: the drone, battery, remote and goggles; linking; arming; flight physics with keyboard and
mouse or a controller (SDL); the FPV camera; the pilot body left standing while you fly; the F8 controller
screen (axes, inversion, calibration); camera tilt; goggles channels; motor sounds; the crafting recipes.

## Requirements

- Minecraft **26.3**, Java **25**
- Fabric Loader **0.19.5** or newer, Fabric API **0.161.0+26.3** or newer
- GeckoLib **5.5.7** for Fabric 26.3

Don't install the original Forge jar alongside this one. Try it in a test world first.

## Flying

1. Get the drone, battery, remote and FPV goggles from the FPV creative tab, or craft them.
2. Place the drone on solid ground and right-click it with the battery.
3. Right-click it with the remote and then the goggles to link them. Keep the remote in your inventory.
4. Wear the goggles and press **N** to arm or disarm. Keyboard: **W/S** throttle, **A/D** yaw, mouse for
   pitch and roll.
5. Take the goggles off to return to your body.

**F8** (while disarmed) opens the controller settings. **Page Up/Down** tilts the camera,
**Shift + scroll** changes the goggles' channel, **F1** hides the HUD and your own drone's model. All keys
can be changed in Minecraft's Controls menu. Settings are saved in `config/fpvdrone/`.

## Building

```sh
cp env.sh.example env.sh   # point it at a Java 25 JDK
source env.sh
cd fpvdrone-fabric
./gradlew build            # jar in build/libs/
./gradlew runSmoke         # automated in-game test in a temporary world
```

`fpvdrone-fabric/orig-forge/` keeps the original's Forge metadata for reference.

## Credits

- **FPV Drones to Minecraft**: JagerGe and Mr.Mulle. Original mod, design, assets and code.
- **Fabric port**: onetrickpony1.
- Uses [GeckoLib](https://github.com/bernie-g/geckolib) and [Fabric API](https://github.com/FabricMC/fabric).
