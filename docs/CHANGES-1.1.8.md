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

## Long-range sync

1.1.8 no longer relies on vanilla entity tracking to show aircraft to other players. The server sends each aircraft's
position, velocity, angles and orientation to every player within the observer range (2048 blocks by default, or the
Distant Horizons render distance when that is larger). Clients smooth between these snapshots on a fixed 240 Hz step
and extrapolate for up to half a second. The pilot's own client is left alone. Chunks ahead of a fast aircraft are
loaded in advance along its heading. *Why:* fixed-wing drones fly kilometres; vanilla tracking stops at a few dozen
chunks and moves entities in visible jumps.

## The drone entity

- **Behaviour comes from the drone's definition:** weight, payload stations, camera, signal, damage zones and the
  clickable zones on the model are read from its data file instead of fixed fields. The drone type, projectile and
  loaded bombs are saved with the drone and kept when it is picked up.
- **Shift + right-click with an empty hand picks a disarmed drone up.** Parts (rocket, bomb bay, battery) come off with
  Shift + right-click while holding something. *Why:* with many aircraft, picking up became the common action. Our
  HD camera follows the same rule and comes off after the other parts.
- **Clicks are aimed at parts of the model:** interact zones are boxes placed on the model's bones (battery slot,
  payload mount and so on), so what you click on decides what happens.
- **Battery, config and camera state are synced to every client,** not only to the pilot, so other players see the
  right battery, rates and attachments.
- **The pilot's client sends one more value, the gimbal tilt,** with each position update, and the server passes the
  update on to nearby players through the long-range sync above. The pilot now only gets their own body's position
  back. *Why:* the Mavic's tilting camera, and one sync path for everyone.
- **Signal-loss falls retuned:** the spin-out spins slower (200-480 degrees a second, was 300-720) and no longer keeps
  some thrust; the flip-and-drop now really flips (180-360 degrees a second, was 0-5) with 10% more drag.
  *Why (likely):* the old flip barely moved, and the spin looked like a powered spiral.
- **The debug infinite bomb item** fills the bomb bay of any drone that has one and tells you when a drone has none.

## Dropped bombs

- **Bombs are real falling objects** (`dropped_bomb`), not instant hits: gravity plus air drag from each projectile's
  `"Drop"` block (mass, drag coefficient, cross-section), and the nose turns into the airflow. *Why:* dropping from
  height now needs judgement, and different munitions fall differently.
- **Impact fuze with an arming distance:** a bomb arms after its fuze delay and 1.25 blocks from the drone, so it
  cannot go off on the drone that carried it.
- **Grenades bounce:** the RGD-5 rolls, bounces off blocks and mobs, tumbles and goes off when its fuse runs out.
- **Bombs can be batted away:** hitting a falling bomb deflects it, makes the hitter its owner and keeps it disarmed
  against them for a short time.
- **The pilot sees the drop at once:** the pilot's client spawns a predicted bomb straight away, and other players see
  it leave from where it hung on the drone instead of popping into view. *Why:* on a server, the round trip made the
  release feel late.

## Flight physics

- **FPV quad flight is unchanged:** same thrust model, drag and rates, still worked out in 1/128 s sub-steps.
- **A second flight mode, GPS, for the Mavic:** the sticks ask for a tilt of up to 30 degrees and the throttle stick
  for a climb rate of up to 6 m/s, which the drone then holds; centred sticks keep it at its height. It meets a
  quarter of the quad's air drag. *Why:* the DJI-style aircraft should fly like the real one, not like a racing quad.
- **Hover hold (brake):** in GPS mode, the drone pushes against its own drift until it stops, while you can still turn
  it.
- **Return to home:** the drone climbs to at least 12 blocks above the home point, turns towards it and flies back at
  up to 10 m/s, climbing to stay 4 blocks over the terrain ahead, then descends at 2 m/s and lands on the spot.
  *Why:* the real Mavic's return-to-home on signal loss and low battery.
- **Releasing a payload kicks the drone:** dropping a grenade or bomb tips it by an amount that depends on the
  payload's weight and how far off-centre it hung. In GPS mode the kick goes into the stabilised tilt instead.
  *Why:* weight leaving one side of a drone upsets it, and pilots have to correct.
- **Hard crashes down a drone:** without a warhead, hitting something at 12 m/s or more puts the drone into a
  spin-out fall, or a tumble from 18 m/s. In 1.1.4 only the kamikaze drone reacted to impacts. *Why:* crashing
  should cost something.
- **Warheads decide when they go off:** the impact speed that sets a kamikaze drone off, and its power and fragment
  count, come from the projectile's file instead of fixed 2 m/s and the config. Impact explosions also carry the
  surface direction, so the blast and debris face away from the wall.
- **Dropped bombs don't count as obstacles** for the drone that just dropped them.

## Flying from the client

- **Smart return-to-home on low battery (Mavic):** the drone heads home by itself at 8% charge, or earlier when what is
  left would not cover the flight home (distance at 10 m/s plus 6 seconds, at 22 A or more) plus a 10% reserve. Home
  is the take-off point, or the pilot if that is unknown. When it lands, the motors stop and the view returns to the
  pilot. *Why:* the real Mavic's behaviour, and long flights made running flat on the way back common.
- **Leaving the view during return-to-home** hands the flight to the server, which finishes it while the drone is
  still in the air and has a battery.
- **The DJI controller is a second way to fly:** holding it (main or off hand) shows the linked Mavic's camera without
  goggles, and the view opens once the drone's chunk has loaded. *Why:* that is how a Mavic is flown.
- **Teleports of the pilot wait for the chunk to load,** and for 0.75 s afterwards a jump of more than 6 blocks is
  treated as a glitch. *Why:* entering a far-away drone's view could drop the pilot into unloaded terrain.
- **Payloads release at the end of the frame,** after the drone has been drawn, so the bomb you see falling starts
  exactly where it hung. Holding the bomb key on a bomb-bay drone keeps dropping, one bomb every 3 ticks.
- **Hover hold and return-to-home keys** work on GPS drones only and say so on other drones; a second press cancels
  return-to-home.

(More sections follow as each part is ported.)
