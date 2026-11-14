# What changed from 1.1.4 to 1.1.8

Notes made while porting 1.1.8 (Forge 1.20.1, 2026-08-14, marked beta) to Fabric 26.3. "Why" is our reading of
the code, not the authors' word, unless it quotes their own comments in the data files.

## In numbers

- Code: about 30,000 lines in 1.1.4, 70,000 in 1.1.8. 228 source files became 539 (718 compiled classes).
- Of the 221 files kept from 1.1.4: 62 unchanged, 69 changed a little, 61 moderately, 29 heavily.
- 1.1.8 ships obfuscated (scrambled names, calls routed through a dispatcher, encrypted constants, an anti-tamper
  hash check). 1.1.4 did not.

## The big change: drones defined by data

1.1.4 had one drone with its numbers in code and in the server config. 1.1.8 describes every drone, battery,
projectile, launch ramp and wearable in data files (`data/fpvdrone/drones|batteries|projectiles|ramps|armor`), read
at startup and sent to every client (`DefinitionSyncPacket`). A drone file holds its physics, rates, camera, signal,
damage zones, which batteries and projectiles fit, sounds, render settings and the zones you can click on it.

**Why (likely):** to add many aircraft without a class per aircraft. The FP-1, Lancet-51, Lytyi and Orlan-10 share
one entity type (`loitering_munition`) and differ only by data; the Mavic is the normal drone entity with a different
`Kind`. The data files are commented for people editing them, so modpacks and servers can tune or add drones.

Knock-on changes:
- The drone entity doubled (about 1,200 to 2,500 lines of logic): it now reads its behaviour from its definition.
- The server config shrank (544 to 371 lines); per-drone values moved into the data files. New global settings:
  signal range multiplier, explosions-break-blocks switch, shrapnel damage (lethal range, base, scale, cap).
- The config sync packet shrank to a fraction; the definitions travel in their own packet.
- The thermal drone became a data file; its item now just sets the drone type "thermal".

## New entities

- `loitering_munition`: fixed-wing aircraft (FP-1, Lancet-51, Lytyi, Orlan-10), 1.2 x 0.8 blocks, tracked from 128
  blocks away so long flights stay visible.
- `ramp`: the launch ramp is an entity, not a block, so it can sit at an angle and carry a drone on it.
- `dropped_bomb`: grenades released from a bomb bay.

Definitions are read only from the mod jar (not from datapacks), when the config loads, on both sides; the server's
copy then replaces the client's when a player joins.

## Controls and camera

- **Nine bindable actions,** each with a key and an optional controller switch or button: arm, exit view, drop bomb,
  zoom, return to home, hover hold, fire rocket, camera focus, interact. *Why:* the new aircraft and weapons needed
  inputs, and RC pilots fly with their hands on the transmitter, so actions belong on switches.
- **Setup wizard:** sticks (or keyboard), then one bind screen per action, then the gimbal tilt, then done. The old
  arm-switch step became the Arm bind screen. The keyboard step now also rebinds throttle and yaw keys.
- **Channel Mapping** became its own screen with a Gimbal Tilt axis and a Functions section (a switch and a key per
  action). Saved under "bindings" in `controller.json`.
- **Keyboard flying keys** are normal key bindings now, plus Climb/Descend for DJI-style (GPS) flying.
- **Inputs freeze while a screen is open or the window is unfocused:** sticks centre and throttle holds. *Why:* so
  opening the map or chat mid-flight doesn't drop the drone or send it off.
- **Third person** orbits 4 blocks out on a level heading, 15 degrees down, instead of following the drone's pitch;
  holding C gives free look. *Why:* a fixed-wing aircraft or a Mavic pitching around made the old follow view hard to
  watch.
- **Gimbal tilt and camera zoom** (mouse wheel, keys or a transmitter wheel). *Why:* the Mavic has a tilting camera
  gimbal and the fixed-wing drones scout with zoom.

## Explosions

- **Damage, blast and block power come from each projectile's `"Stats"` block** instead of the server config.
  *Why:* each warhead (RPG-7, RGD-5, TM-62, 40 mm, 70 mm rocket) can hit differently.
- **Explosions break blocks by default:** a new server switch (`explosionBreaksBlocks`, on by default, and
  `/fpvdrones explosive enable|disable`), with the strength taken from each projectile's `BlockPower`. The RPG-7 has
  0.5, so kamikaze hits now dig holes; in 1.1.4 they never did.
- **Shrapnel** now damages other drones (only the exploding one is spared), spares pilots who are flying and anyone
  inside an Immersive Vehicles vehicle, keeps chunks loaded briefly so mobs it kills drop their loot, leaves up to 48
  debris marks on blocks, and adds shaped-charge damage to vehicles within 2 blocks. The vanilla explosion particle is
  gone. *Why:* drones hunting drones, and fairness to the pilot whose body stands still while flying.
- **Global config left:** a signal range multiplier for every drone at once, the block-breaking switch, live map
  observation and parallel particle ticking. Per-drone numbers live in the drone files. In the authors' words: "The
  global "signalRangeMultiplier" in the mod config scales MaxRange and FreezeStartDistance for every drone at once."

## Sound and particles

- **Sounds are defined per drone and projectile,** with distance layers that cross-fade (a near crack and a far boom
  of the same explosion). Sources follow the camera, pause with the game and survive a sound-device change; 32
  engine voices; fixed-wing drones have engine sound and a launch sound. *Why:* many aircraft at long range need
  their own sound and believable distance.
- **A new particle engine** reads particle lists in the same JSON format as Immersive Vehicles (smoke, flame,
  debris, decals). *Why:* exhaust trails and warhead effects defined in data, matching the vehicles mod many players
  run alongside.

## Items and blocks

- **One item per aircraft:** Mavic, FP-1, Lancet-51, Lytyi, Orlan-10, each naming a drone file. The FP-1, Lancet and
  Orlan can't be placed by hand; they go on a launch ramp. The Lytyi sits on the ground. A picked-up aircraft keeps
  its type, payload, loaded bombs, fuel and rocket pods. *Why:* one entity plus a data file per aircraft.
- **Battery types:** a DJI battery for the Mavic. Volts per cell and weight come from the battery file (the default
  pack still weighs 471.57 g).
- **New gear and ammunition:** DJI controller (fly the Mavic GPS-style without goggles), fuel can (20 litres, for the
  petrol FP-1, Lytyi and Orlan), launch ramp, rocket pod mount, 70 mm rocket, RGD-5 grenade, 40 mm grenade, TM-62
  mine, and the "Can Opener" vest: a chestplate that carries a TM-62 and goes off on a lethal hit. A 120 mm mortar and
  its round are placeholders ("Not added yet").
- **Tooltips** show item icons for required, optional and installed parts, built from each drone file, and battery
  charge in %. *Why:* with many aircraft, text lines no longer scaled.
- **The BetaFlight item no longer opens on the drone itself;** the drone's interact menu and the ramp open the
  configurator and map planner.
- **EW jammer block (REB):** jams drone signal in a ring 40 blocks out and 20 blocks thick; inside about 20 blocks of
  the jammer, signal is fine. It has the crafting-table look and no recipe, so it reads as a beta placeholder.
- **Recipes are unchanged;** none of the new items can be crafted (creative or /give only). The creative tab drops
  the old bombs, the weight, the processor and the jammer.

(More sections follow as each part is ported.)
