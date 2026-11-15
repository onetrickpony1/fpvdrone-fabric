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
- **Camera per drone:** field of view, gimbal, free look and zoom steps come from each drone's `"Camera"` block
  (for the Mavic: gimbal tilt on the wheel or keys, zoom steps 1x/3x/7x).

## Explosions

- **Damage, blast and block power come from each projectile's `"Stats"` block** instead of the server config.
  *Why:* each warhead (RPG-7, RGD-5, TM-62, 40 mm, 70 mm rocket) can hit differently.
- **A kamikaze drone explodes with what it carries:** explosion power, fragment count, shaped-charge damage, how far
  the bang is heard, and the impact particles all come from the equipped projectile's file. With no projectile, or an
  unknown one, it is an RPG-7, as before.
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

- **Engine sound per drone:** each drone file can set its own engine sound; the bundled FPV drones use the shared one.
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

## Drawing drones

- **Each drone definition names its own model and texture,** so the Mavic, the thermal drone and data-pack drones each
  look like themselves.
- **The RPG and battery bones became general mounts** (`payload_mount`, `battery_mount`). The RPG is now just one
  payload among others, and any payload or battery that fits a drone is drawn from its own model. *Why:* data-driven
  payloads and batteries.
- **Hanging payloads swing:** a grenade on `payload_mount` hangs from a pivot below the mount and keeps hanging
  straight down while the drone tilts, so it sways.
- **Bomb bays are drawn station by station,** with one projectile per loaded station; a payload rack (for example the
  Mavic's VOG holder) disappears after the last drop.
- **Every drawn payload reports where it is,** so the drop key starts the bomb exactly there and it does not jump on
  release.
- **The Mavic's gimbal holds the camera level** against the drone's pitch and roll, keeping only the tilt you set; its
  tilted propellers spin about their own axes, and GPS drones hide their own propellers in first person.
- **Propeller blur only where the definition asks for it,** and other players' drones blur by their real throttle
  (sent with each update) instead of fixed values.

## Drawing the ramp and the loitering munitions

- **The launch ramp** is drawn from its ramp file's model and texture and turns with the ramp. An installed drone sits
  on the file's attach bone with the per-drone offset; its blur bones are hidden, rocket-pod mounts show only while
  loaded, and rockets loaded on the ramp sit at their stations.
- **Loitering munitions (FP-1, Lancet, Orlan)** are drawn from their fixed-wing models, including the poly-mesh
  fuselages: ailerons follow the controls, the back pair about axes tilted 40 degrees; the rotor spins and fades into a
  spinning blur disc between 25% and 55% throttle; the engine shakes with throttle where the file asks for it; freshly
  loaded rockets slide into their tubes over 7 ticks; exhaust and rocket launch particles come from the model's bones.
- **Seen from its own camera,** a munition with a fixed camera is hidden; a gimbal camera hides only its camera bone and
  turns it with the view. A damaged or crashed munition uses its "_damaged" texture.
- **With hitboxes shown (F3+B)** both draw their oriented collision boxes; the munition and the ramp's drone also show
  their interact zones in orange.
- *Port note:* 1.1.8 drew these with the simplebedrockmodel library; here the same files are read by GeckoLib and a small
  poly-mesh reader. 1.1.8 also shipped an unregistered GeckoLib munition renderer and a developer attach-offset tuner;
  neither is ported.

## Server side

- **A pilot who drops out mid-flight comes back to where they stood:** the spot is saved with the player when they
  enter FPV, so after a crash or kick the next login takes the goggles off and puts them back there. *Why:* the pilot's
  body sits at the drone while flying, and a crash used to leave it there.
- **Logging out no longer drops every drone:** a Mavic in the air with a battery keeps hovering; other drones in the
  air cut their motors; drones on the ground also stop loading chunks.
- **Chunks are loaded ahead of a flown drone** along its heading, at a distance set in its definition, and around
  its last reported position while the drone itself is not loaded. Pilots can't be riding anything while flying.

## Signal

- **Each drone has its own radio:** range, the distance where video starts to freeze, obstacle penalty, the height
  bonus and the ground-proximity penalty now come from the drone's definition file instead of one server setting.
  The server keeps one "signal range multiplier" that scales every drone's numbers. *Why:* the Mavic's link and a
  hobby FPV link shouldn't reach the same distance; with data-defined drones, the radio became part of the drone.
- **The maths is unchanged:** the same distance, obstacle, line-of-sight, height and ground factors as 1.1.4, just
  fed from the definition.
- **DJI link mode:** a Mavic flown from its controller has full signal at any distance. Only jammer zones cut it.
- **Jammer zones:** each EW jammer block makes a doughnut-shaped zone around it, 20 to 60 blocks out horizontally
  and up to 20 blocks above and below. Inside it the signal is capped at half, and it falls to nothing in the inner
  half of the doughnut's tube. This applies to every drone and both link types. Right next to the block (in the
  doughnut's hole) the signal is not affected; that is how 1.1.8 builds the zone.
  With entity hitboxes shown (F3+B) the zone is drawn as a white wireframe doughnut.
- **Left out on purpose:** 1.1.8 also multiplies the signal by 0.85 when its anti-tamper check decides the jar was
  modified. That is copy protection, not a feature, so this port doesn't have it.

## The pilot's body

- **The body falls:** in 1.1.4 it was frozen where the player stood, even in mid-air; now it falls and slides like
  a mob. A body of a creative (invulnerable) player still floats. *Why:* a body hanging in the air looked broken, and
  entering FPV mid-jump left it there.
- **Back to the body even after it is gone:** its position is remembered every tick, so leaving FPV still puts the
  player there after the body was unloaded or killed.
- **No more landing inside blocks:** the player returns to the first free spot at or up to 64 blocks above the body.
- **The body keeps its own chunks loaded** the same way drones do, and tells its owner where it is once a second
  (used for signal and the way home).
- **Logout, server stop and "drone not found" at login** all use the same full exit as pressing the exit key, so
  the player is saved standing at the body. 1.1.4 handled each case on its own and could leave the player at the
  drone.

## The interact menu

- **Look at a part of an aircraft on foot and a small menu appears under the crosshair**: battery, remote, goggles,
  payload, bomb bay, pick up on the quads; fuel, laptop (route planner), rocket pod and ramp actions on the fixed-wing
  aircraft and the ramp. It reaches 4 blocks, fades in after two frames on the same part and fades out with distance.
  The mouse wheel picks a line, the interact key (F, or a controller button) runs it; lines you can't use right now
  are grey with "(None)". Taking an aircraft off its ramp or picking up a wreck needs 8 left clicks in a row. While the
  menu is up, F does not also swap hands. *Why:* with many aircraft and parts, right-click with the right item no
  longer scaled; the menu lists what is possible and what is missing. Right-click still works as before.
- The parts come from the "InteractZones" in each drone and ramp file (a bone, a content type and optional required
  items), so a new aircraft gets its menu from data.
- **The BetaFlight item no longer opens the OSD configurator when used on a drone.** It is the "laptop" the menu asks
  for to plan a route. In 1.1.8 nothing else opens the OSD builder, so it is unreachable in game (kept in code).

## The "Can Opener" vest

- A chestplate that always carries a TM-62 (the definition's "Preloaded" mine, drawn on the vest's mount bone).
  There is no way to load or unload it, although its usage text says "right-click it with a TM-62".
- **The drop (bomb) key sets the mine off**, also when not flying. A wearer standing in person dies in the blast
  (creative players excepted); while flying, it goes off at the pilot body.
- **A hit that would kill the wearer cooks the mine off instead** (drone collisions and the mod's explosions; checked
  after armour). *Why (likely):* a suicide vest for PvP and a way to punish drone hunters.

## Small changes

- **Onboarding hints** (link a remote and goggles, put the goggles on, payload tips, ramp needs fuel / route) exist in
  1.1.8 but are switched off by a constant; none ever shows. Ported the same way (off).
- **Pets don't follow, and mobs don't target, a player flying a drone**; they see the pilot body. `/tp` to such a
  player lands at his body, not at the drone. *Why:* the player entity rides with the drone, far from the body.
- **Shrapnel debug view:** with entity hitboxes on (F3+B), every shrapnel ray of the explosions of the last 20 seconds
  is drawn, with a box where it hit. The server sends the rays to everyone within 64 blocks after each explosion.

## Thermal camera

1.1.4 already had the thermal camera; 1.1.8 keeps its picture almost unchanged and trims the controls.

- **The picture is the same:** blocks get a temperature from a per-block table (lava, fire, torches, furnaces hot;
  stone, wood and wool lukewarm; grass, sand, water, snow and ice cold; metal and glass reflect their surroundings).
  The block light level adds heat around torches and lava, faces turned to the sun warm up by day (metal more), and
  roofs facing the open sky lose a little heat. Every mob and player is drawn at one hot temperature. The sky is
  black. A chain of screen passes follows: a very light atmospheric haze (more in rain), sensor noise that changes
  every frame (NETD), a fixed noise pattern (FPN), the colour palette and a slight lens vignette and barrel
  distortion. The lens focuses on what it looks at and blurs the rest (depth of field). Walking mobs leave warm
  footprints that fade over 40 s.
- **Controls:** End toggles thermal, U runs a NUC calibration (the camera clicks; afterwards its fixed noise is 40%
  lower, wearing off over 3 minutes; it also runs by itself every 3-5 minutes), Home toggles the focus. A thermal
  drone switches thermal on by itself when you fly it and off when you leave.
  - 1.1.4's AGC key (G, cycling AUTO / ROI / MANUAL) is gone; G is now the exit key. 1.1.8 still has two fields named
    THERMAL_AGC_MODE_KEY / THERMAL_FOCUS_MODE_KEY, but they register the yaw left/right flying keys (A/D): leftovers of
    a rename, not thermal keys. Its Home handler, still named for AGC, toggles the focus.
  - 1.1.4's focus had three modes (off / auto / manual distance); 1.1.8 has auto focus on or off.
  - The End key now only works while watching a loitering munition whose camera allows thermal (`ThermalToggle`);
    1.1.4 allowed it in creative mode on any non-thermal drone. *Why (likely):* thermal became a property of the
    camera in the data files (`Thermal`, `ThermalToggle`), so the key follows the camera.
  - There is no palette key in either version: the palette code (white-hot, black-hot, ironbow) is complete, but
    nothing changes it, so the picture is always white-hot.
- **Code that does nothing in both versions:** the automatic gain control and detail enhancement passes only copy the
  picture, the bloom, blur and down/up-sampling passes are never run, the thermal OSD overlay draws nothing, the
  heat-source scanner and the per-block heat memory are never fed, and the sky and cloud thermal shaders are loaded but
  never used. The ambient temperature the shaders use is computed by a method nothing calls, so it stays 0. Kept that
  way in the port (and the dead parts left out).
- **Mods that draw differently:** with Embeddium 1.1.8 hooks Embeddium's chunk shader; with Oculus/Iris, Sodium or
  Rubidium it falls back to a screen-space version that guesses temperatures from the normal picture's brightness and
  distance (bright = hot, far = cool), with entities still drawn hot.

### Port notes (26.3)

- 26.3 has no shader instances to swap. The thermal chunk shader is a 26.3 terrain pipeline
  (`fpvdrone:core/thermal_terrain`, one shader with solid, cut-out and translucent variants, for both ways 26.3 draws
  chunks); vanilla's chunk drawing is pointed at it and at the thermal atlas while a thermal frame draws. 26.3 chunk
  vertices have no normal, so the shader derives each face's normal from its position.
- Entities: in 26.3 blending, culling and depth belong to the pipeline, so each vanilla entity pipeline gets a thermal
  twin with the same states and the thermal fragment shader.
- The thermal atlas is built from each sprite's own image (1.1.8 read the atlas back from the GPU). 26.3 groups the
  coloured and copper blocks; the groups are registered whole, so waxed copper stages count as metal too.
- The post passes are fullscreen 26.3 pipelines in `shaders/post/`; distances come from the depth buffer through the
  inverse projection (26.3 uses reversed depth).
- **Sodium and Iris:** the port follows 1.1.8: if Sodium or Iris is installed, the screen-space fallback is used
  (Embeddium does not exist on Fabric). Entities are still drawn hot through the same entity pipelines. With an Iris
  shader pack switched on, entities are left to the pack and the depth buffer is not read (the fallback then uses
  brightness only). Sodium and Iris are not build dependencies; `tools/run-smoke.sh -PwithSodium` or `-PwithIris`
  runs the in-game test with Sodium 0.9.2 (and Iris 1.11.7) from Modrinth, as the owner plays. Tested with both (no
  shader pack): the fallback is used and nothing breaks.
- The sky is skipped and the picture cleared to near black (1.1.8 cleared it after the sky stage). Clouds still draw
  normally, as in 1.1.8.
- The End key works as in 1.1.8: only while watching a loitering munition whose camera has `ThermalToggle`. A
  munition camera's zoom also widens the depth-of-field blur by its `ZoomBlur`.

## Loitering munitions, the tactical map and recon

1.1.8's fixed-wing aircraft (FP-1, Lancet-51, Lytyi, Orlan-10, test target), the launch ramp, the tactical map
(Mission Planner) and the recon layer are ported from its code: `LoiteringMunitionEntity`, `RampEntity`,
`MunitionClientState` (1.1.8 GAduSQ8Kp), `AimScreen` (Lancet sight), `TacticalMapScreen` with `MapPopupMenu`,
`client/map/*` and `map/ServerMapTiles`, and their packets. The items place them (`RampItem`, `LytuiDroneItem`), the
BetaFlight laptop opens the route planner, the interact menu works on them, shrapnel hits their nearest damage zone,
and their engines are heard.

### Port notes (26.3)

- **Saved chunks for the map:** 1.1.8 decoded region files with `PalettedContainer` codecs it built by hand; 26.3
  builds those codecs in `PalettedContainerFactory`, which the reader now uses. Chunk status is read with its codec.
- **Block colours:** a 26.3 block model is a list of parts; the top face's first quad (or any quad) gives the
  sprite and tint index, as 1.1.8 took from `getQuads`. Tints come from 26.3's per-layer tint sources.
- **Map drawing:** 26.3 GUI drawing has no global colour or blend state. Map tiles are 1024x1024 GPU atlases written
  with `writeToTexture`; each tile quad is a blit scaled into its exact float box. Waypoint pins carry their alpha in
  the blit colour, and the hover glow uses a pipeline with 1.1.8's additive blend. **Items on the map** (drone icons,
  the drag preview) cannot be tinted or faded in 26.3, so their brighten-on-hover and fade-out are not shown; they
  still grow and shrink. Z offsets became GUI strata.
- **Altitude field:** 26.3 `EditBox` has no input filter, so a non-digit edit (or more than 5 digits) is put back.
- **Input:** the screens take 26.3 key, character and mouse events; the 1.1.8 handlers are kept behind small adapters.
- **Forge events** became Fabric events (client tick, join/leave, server tick, disconnect).

## Munition camera and OSD profiles

- The view follows 1.1.8's `CameraMixin.adjustMunitionCamera` (camera offset along the banked frame, 4-block orbit in
  third person) with the angles from its `CameraManager` munition branch (bank, mouse-aim view for click-designate and
  gimbal-observe). Mouse movement steers the munition; the wheel zooms its optical zoom; its FOV is its own; free look
  works in third person and through the aim screen; the HUD and the pilot's own body are hidden; the last video frame
  is kept for when the link drops.
- **OSD profiles:** each camera chooses MAX7456 (the Betaflight OSD) or ZALA, and may switch the OSD off
  (`ShowOsd`). ZALA (Lancet, Orlan) is `render/ZalaOsd`: the thin reticle with sub-pixel antialiased edges, compass
  ring, tilt scale and target marker. F9/F10 change its size, a debug control 1.1.8 left in. Munitions on MAX7456 get
  1.1.8's fixed fixed-wing layout. As in 1.1.8, the OSD is drawn in first person only.
- **Drop key** commits the dive (click-designate scheme), **exit** leaves the munition view, **R** fires a rocket,
  **focus** works on the gimbal-observe camera, **M** opens the map.
- The aim screen lets F3 debug keys through (1.1.8 KeyboardHandlerMixin).

## Hitboxes, collisions and smaller items

- Arrows and other projectiles hit munitions and ramps on their oriented zone boxes, not their plain box
  (1.1.8 ProjectileUtilMixin; covered by the smoke test). Players are pushed out of the boxes and can stand on them
  (1.1.8 EntityMixin + AwIR2HC8b, here `ZonePushOut`).
- After the server's definitions arrive, the bone-box and collision caches are cleared (1.1.8 DefinitionSyncPacket
  client refresh).
- Bomb-bay drops accept every link 1.1.8 accepts (owner, linked drone, goggles, DJI controller, remote).
- The DJI digital feed skips the analog low-res downscale, as in 1.1.8.
- The tactical map's four waypoint icons are copied from 1.1.8.
- **Dedicated servers:** the goggles' tooltip read the client's level in a way that loaded a client class on a
  dedicated server and stopped it at start-up; it now goes through `ClientState.level()`. `tools/run-server-check.sh`
  boots a dedicated server with a test (`src/servertest`) that places the drones, Lytui and ramp, spawns the other
  munitions, ticks them and explodes a bomb.
- **Not ported:** 1.1.8's far-drone pass (`FsOzhg`), which draws drones in chunks the client has not loaded and only
  runs when Distant Horizons is installed.

(More sections follow as each part is ported.)
