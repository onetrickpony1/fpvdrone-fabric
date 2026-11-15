# FPV Drones to Minecraft: unofficial Fabric port

An unofficial port of **FPV Drones to Minecraft** 1.1.4 and 1.1.8 (Forge 1.20.1) by **JagerGe** (programmer, modeller)
and **Mr.Mulle** (designer, icon artist) to **Minecraft 26.3 on Fabric**. All of the mod's design, models,
textures, sounds and flight model are theirs; this repository only carries it to a newer game and loader.

> This port is not made or endorsed by the original authors. Please report problems with it
> [here](../../issues), not to them. It is licensed, like the original, under the
> [PolyForm Noncommercial License 1.0.0](LICENSE.md): **no commercial use**.

## Status

The port has reached **feature parity with 1.1.8** (2026-08-14, the latest original release) in code:
[docs/PARITY.md](docs/PARITY.md) lists every original feature and its state. Rows marked **check** are ported
but still need an in-game look against the original. The automated tests (an in-game client test and a
dedicated-server test) pass.

Included: the 1.1.8 data-driven drones (FPV quads, thermal drone, Mavic, FP-1, Lytui, Lancet, Orlan and more), batteries,
remote and goggles, the flight model with keyboard/mouse or a controller (SDL), the OSD and OSD builder, video
effects, signal and jammer zones, bombs, rockets and loitering munitions with launch ramps, the tactical map, the
thermal camera, the interact menu and the pilot body.

**Additions in this port** (not in the original; see [docs/UPCOMING.md](docs/UPCOMING.md)):

- **Clean feed** (F6) and the **HD Digital Camera** part.
- **Turtle mode** (crash flip), behind the `turtleMode` config (off by default).
- **GPS Module** part and **GPS Rescue** (P), also on signal loss.
- **Wind** (on by default, mild): quads drift and must be corrected, falling drones drift, GPS drones lean into
  the wind; walls, hills and trees upwind give shelter, and wind is stronger higher up, with turbulence and gusts that rock the drone. With
  [Weathery](https://github.com/onetrickpony1/weathery) installed the drones fly in Weathery's wind (storms, gusts,
  high-wind spells); without it a built-in wind rises in rain and thunder. `/fpvdrones wind` shows the wind where you
  stand; config `windEnabled`, `windStrength` (0-3), `windSheltering`, `windTurbulence` (0-3), `windGusts`. Wind off flies exactly as 1.1.8. More to come
  (bombs, munitions): [docs/WIND.md](docs/WIND.md).
- Fixes: battery drain with F1, pilot flung up after a flight, controller switches in Channel Mapping, dedicated
  servers.

## Requirements

- Minecraft **26.3**, Java **25**
- Fabric Loader **0.19.5** or newer, Fabric API **0.161.0+26.3** or newer
- GeckoLib **5.5.7** for Fabric 26.3

Optional: **Weathery** (wind source), **Sodium** 0.9.2 / **Iris** 1.11.7 (tested; the thermal camera uses its
screen-space fallback there), **Distant Horizons** (far drones are drawn beyond loaded chunks).

Don't install the original Forge jar alongside this one. Try it in a test world first.

## Flying

1. Get the drone, battery, remote and FPV goggles from the FPV creative tab, or craft them.
2. Place the drone on solid ground and right-click it with the battery.
3. Right-click it with the remote and then the goggles to link them. Keep the remote in your inventory.
4. Wear the goggles and press **N** to arm or disarm. Keyboard: **W/S** throttle, **A/D** yaw, mouse for
   pitch and roll.
5. Take the goggles off to return to your body.

**G** switches between the drone view and your body, **H** holds a hover (GPS drones), **P** returns home,
**Y** turtle mode (when enabled). **F8** (while disarmed) opens the controller settings. **Page Up/Down** tilts the camera,
**Shift + scroll** changes the goggles' channel, **F7** (creative) changes the video resolution. All keys
can be changed in Minecraft's Controls menu.

**Clean feed (F6, an addition of this port):** a recording view without the OSD, stick overlay, video effects or
your own drone's props. Creative: any drone. Survival: fit an **HD Digital Camera** to the drone first (craft it
from an FPV camera, 2 diamonds and 2 redstone; right-click the drone with it, Shift + right-click takes it off);
the clean feed then draws about 35% more current. Losing the signal still cuts the feed. Settings are saved in `config/fpvdrone/`.

## Building

```sh
cp env.sh.example env.sh   # point it at a Java 25 JDK
source env.sh
cd fpvdrone-fabric
./gradlew build            # jar in build/libs/
```

Tests (from the repository root): `tools/run-smoke.sh` (in-game client test on an invisible desktop, screenshots
in `fpvdrone-fabric/build/smoke-run/screenshots`) and `tools/run-server-check.sh` (dedicated-server game tests).
Both take `-PwithWeathery` (Weathery jar from `../Weathery/mod/build/libs`), and the smoke test also takes
`-PwithSodium` / `-PwithIris`.

`fpvdrone-fabric/orig-forge/` keeps the original's Forge metadata for reference.

## Credits

- **FPV Drones to Minecraft**: JagerGe and Mr.Mulle. Original mod, design, assets and code.
- **Fabric port**: onetrickpony1.
- Uses [GeckoLib](https://github.com/bernie-g/geckolib) and [Fabric API](https://github.com/FabricMC/fabric).
- Wind research and sources: [docs/WIND.md](docs/WIND.md).
