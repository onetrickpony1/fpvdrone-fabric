# Feature parity with FPV Drones to Minecraft 1.1.8

Every feature of the original (Forge 1.20.1), and where it stands in this Fabric 26.3 port. The sections up to
"Compatibility" are 1.1.4, ported from its code. The 1.1.8 section lists what 1.1.8 (2026-08-14, marked beta)
adds. 1.1.8's code is obfuscated, so those features are rebuilt from its readable parts: the language file, the
commented data files (`data/fpvdrone/drones|batteries|projectiles|ramps|armor|weapon_damage`), models, textures,
sounds and config, plus how it behaves in game.

Status: **done** (ported and checked in game), **partial** (some of it), **missing**, **check** (ported but not
yet verified against the original's behaviour).

## Items and recipes

| Feature | Status | Notes |
|---|---|---|
| Drone 9", thermal drone 9" | check | thermal drone needs the thermal camera to matter |
| Remote controller, FPV goggles, battery (6S 3300 mAh) | done | |
| Parts: antenna, camera, electric motor, propeller, processor, thermal camera | done | crafting ingredients |
| Payloads: grenades Mk.1-3, bomb holder, RPG-7 round, dead weight | check | attach, drop, detonate; weight +100 g |
| BetaFlight configurator item | done | |
| 13 recipes (processor recipe disabled in the original too) | done | |
| Item tooltips (Hold Shift usage, battery info, link state) | check | |
| Creative tab "FPV to Minecraft" | done | |

## Flight

| Feature | Status | Notes |
|---|---|---|
| Physics core (motors, props, battery voltage sag, drag, 3D mode) | check | `physics/` ported line for line; battery drain fixed (it runs from the OSD, which was missing) |
| Arm / disarm (N), arm switch on a controller | partial | key works; controller arm switch to verify |
| Keyboard and mouse control, force KB&M option | check | 1.1.8 has no screen for force KB&M either (only the `forceKbm` config key); `FpvConfig`, F8 flight settings |
| Joystick / gamepad (SDL), trackpad | partial | needs a real controller test |
| Rates: rate, super rate, expo per axis, presets | check | 1.1.8 takes rates from the drone file (`Rates`); its rates editor exists but nothing opens it, same here |
| Drone build: motor KV/size, cells, props, frame, mass, pro/hero cam, toothpick, colours | check | from the drone file's `Physics` in 1.1.8; its build editor is never opened, same here |
| Camera angle (Page Up/Down), FOV | check | Page Up/Down tilt; FOV from the definition's camera; 1.1.8's angle/FOV list is never opened |
| Drone surfing (riding on a drone) | check | `DroneSurfHandler`; fixed: after a flight it could fling the pilot up to 10 blocks into the air (also in the original) |
| Collisions, fall types, drone damage, entity hits and kicks | check | |
| Chunk loading around a flying drone | check | |

## FPV and pilot

| Feature | Status | Notes |
|---|---|---|
| Enter / leave FPV with goggles, channels (Shift + scroll) | done | |
| Pilot body standing in for the player, its equipment | done | 1.1.8: falls with gravity, remembered position, free-spot return, own chunk loading |
| Pilot body render in FPV, remote players' lerp | done | `FpvPlayerBodyRenderer`, `RemotePlayerLerpMixin` |
| Goggles worn on the head (armour model) | done | GeckoLib armour renderer; seen on the pilot body and in the inventory. 1 armour point, 55 durability, leather repair (1.1.8 leather helmet; server test) |
| Inventory screen while flying | done | the player with goggles, posed by the mouse as in the original |
| Villagers panic at drones | done | they run from drones within 24 blocks and ring bells |
| Teleport to / switch drone, saved drone positions | check | |

## HUD, OSD and video

| Feature | Status | Notes |
|---|---|---|
| Goggles OSD: compass, timer, names, RSSI/LQ, speed, horizon, current, mAh, voltages, mode, warnings | done | |
| Stick overlay | done | toggle comes with the settings screens |
| F1 hides the HUD | done | as in the original; the old port also hid your own drone, that is now the clean feed (F6) |
| OSD: MAX7456 character grid, MCM font, all OSD elements | done | |
| OSD layouts: per drone, import/export, sync to server | check | data, packets and the builder screen ported; 1.1.8 has no way to open the builder (BetaFlight became the route laptop), same here |
| Low-resolution video, Cycle Resolution key (F7, creative), range-scaled resolution (survival) | done | GPU render passes instead of glBlitFramebuffer |
| Video freeze / stutter at range, frozen last frame on signal loss | done | stutter not yet watched at real range |
| Boot screen when the goggles start | check | `BootStateManager` |
| Near-plane fix in FPV (0.01) | done | camera orientation was already in the port's CameraMixin |

## Port additions (not in the original)

| Feature | Status | Notes |
|---|---|---|
| Clean feed (F6): no OSD, stick overlay, low-res/stutter, or own drone; signal loss still cuts the feed | done | creative: any drone; survival: needs an HD Digital Camera on the drone and draws 35% more current |
| HD Digital Camera part (FPV camera + 2 diamonds + 2 redstone) | done | right-click to fit, Shift + right-click to remove (after the RPG and the battery); kept in the drone item |
| Battery drains with the HUD hidden (F1) | done | the original's drain ran inside the OSD drawing, which 26.3 skips under F1 |
| Pilot no longer flung into the air after a flight | done | drone riding kept a stale drone (original bug) |
| Channel Mapping screen can bind controller switches | done | 1.1.8's screen never ticks its input listener and waits on "..." for ever |

## Signal and range

| Feature | Status | Notes |
|---|---|---|
| Signal strength: distance, obstacles (block attenuation), altitude, ground proximity | done | 1.1.8: tuned per drone definition, scaled by the server's signal range multiplier |
| DJI link (Mavic from its controller): full signal except in jammer zones | done | |
| Failsafe on signal loss (descend, damp) | check | |
| Out of range, drone not found, signal lost | check | |

## Explosions and damage

| Feature | Status | Notes |
|---|---|---|
| Kamikaze / RPG detonation, shrapnel rays, kill zone, blast wave, doors forced open | check | |
| Explosion audio engine (distance, muffling) | check | |
| Drone collision damage | check | |
| Shrapnel debug view | done | `ShrapnelDebugPacket`, `render/ShrapnelDebugRenderer` (gizmos, F3+B); rays sent to players within 64 blocks; the test client draws no debug gizmos, so only receipt is checked |

## Thermal camera

| Feature | Status | Notes |
|---|---|---|
| Thermal vision on thermal drones (switches on in FPV and through the goggles, off on leaving) | done | in-game test: `thermal-white_hot/black_hot/ironbow` screenshots; vanilla chunks use the thermal chunk pipelines + thermal atlas |
| Thermal toggle key (End) | check | works while watching a loitering munition whose camera has `ThermalToggle` (`ThermalKeys`, `MunitionClientState.wuhB`) |
| Palettes WHOT / BHOT / Ironbow | done | all three drawn in the test; as in 1.1.8 there is no key to change it (always WHOT in play) |
| NUC calibration (U), auto every 3-5 min | done | click, 0.8 s, fixed-pattern noise -40% fading over 3 min |
| Focus (Home): depth of field on/off | done | 1.1.8's "AGC mode" handler is this focus toggle; 1.1.8 has no AGC key (G is exit) |
| AGC, DDE | done | copy passes in 1.1.8 too |
| Thermal sky / clouds / blocks / entities | done | sky black (skipped + clear); clouds draw normally (1.1.8's sky/cloud thermal shaders were never used); blocks by per-block table, block light, sun; entities one hot temperature |
| Noise (NETD, FPN), atmospheric fog, vignette + distortion, depth of field | done | |
| Bloom, OSD overlay, heat-source scanner, per-block heat memory | n/a | present in 1.1.8 but never run (overlay draws nothing) |
| Footprints | done | warm footprints of walking mobs/players, 40 s; the test checks they are made (39 from a walk); too flat to see from the ground-level test camera |
| Sodium / Iris | done | screen-space fallback as in 1.1.8 (FallbackManager); tested with Sodium 0.9.2 and Sodium + Iris 1.11.7 (no shader pack); with a shader pack entities are left to the pack and depth is not read (untested) |

## Screens

| Feature | Status | Notes |
|---|---|---|
| "Remote-Controller Settings" button in the Controls screen | done | a row at the end of 26.3's Controls list (the original moved Done down instead) |
| Settings lists: channel mapping, rates, drone build, other settings (`ModelSettingsScreen`) | done | ported, but the original never opens them either; the port's F8 screen is an extra |
| Calibration wizard (controller yes/no, sticks, arm switch, keyboard, complete) | done | |
| BetaFlight configurator (rates, physics, 3D mode, OSD, drone name, presets) | done | opened with the BetaFlight item on a drone; check layout at your resolution |
| OSD editor and import | done | |

## Config and server

| Feature | Status | Notes |
|---|---|---|
| Server config (simple range, signal, video freeze, failsafe, explosion, damage, observer range), synced to clients | check | `FpvConfig`, `ConfigSyncPacket` |
| Drone tracking range | check | |
| Dedicated server | done | `tools/run-server-check.sh`: boots, places and ticks drones, munitions and ramp, explodes a bomb (start-up crash fixed) |

## Compatibility

| Feature | Status | Notes |
|---|---|---|
| Embeddium: FPV distance limiter, stability workaround, thermal chunk shaders | n/a | the limiter and workaround have no callers in 1.1.8; thermal: Sodium/Iris use the screen-space fallback as 1.1.8 did for Oculus/Sodium/Rubidium |
| Decorative effects: prop blur discs, "Kirpichiki" bricks | done | 1.1.8: blur comes from each drone model's own blur bones where its file enables it; bricks only for drones whose file asks for them, fading out over 7 s after lift-off, drawn out to the observer range |
| Full drone renderer (props, colours, accessories) | done | props fade into blur discs with throttle |

## Added in 1.1.8

### Drones (data-driven: `data/fpvdrone/drones/*.json`)

| Feature | Status | Notes |
|---|---|---|
| Drone definitions as data files (physics, rates, camera, signal, batteries, projectiles, damage zones, interact zones, sounds) | check | loaded and synced to clients; the drone entity, flight physics (`DroneDefinition.Physics`, 1.1.8 rebase) and renderer read the definition |
| DJI Mavic 3 Pro + DJI RC Pro controller + DJI battery | check | GPS flight, hover hold, return to home (also on low battery, finished by the server if you leave), gimbal and zoom, bomb bay, flown from the held DJI controller; drawn with its own model and blur (smoke test) |
| FP-1 loitering munition (fixed wing) | check | `LoiteringMunitionEntity` (method for method), ramp launch, route from the tactical map, rocket pods, fuel; drawn by `LoiteringMunitionRenderer` (in game) |
| Lancet-51 loitering munition | check | ramp launch, aim screen (click to designate), dive; ZALA OSD; drawn in game |
| Lytyi loitering munition | check | `LytuiDroneItem` places it with the item's fuel and rocket pods; route from the map |
| Orlan-10 reconnaissance drone | check | gimbal-observe camera, recon marks/tiles on the map, ZALA OSD, zoom/focus; drawn in game |
| Test target drone | check | `test_target.json`; hover dummy and respawn (`DelayedMunitionSpawns`) |
| 3-D collision shapes for drones (`geo/collision`) | check | `GeoBoneBoxes`, `MunitionCollision`, `BoxTerrainCollision`, `OrientedBoxGrid` for munitions and ramps (1.1.8's quads do not use them either); projectiles hit the zone boxes (`ProjectileUtilMixin`, smoke-tested); players are pushed out of them (`ZonePushOut`) |
| Bedrock-format models (`bedrock_geo`, simplebedrockmodel library) | check | loaded with GeckoLib 5's model loader instead of simplebedrockmodel; used by projectiles and dropped bombs. Poly meshes (fixed-wing fuselages) read by `BedrockPolyMeshes` and drawn by `FixedWingModelRenderer` |

### Launch, fuel and planning

| Feature | Status | Notes |
|---|---|---|
| Drone launch ramp (block) | check | `RampEntity`, `RampItem`, interact install/remove/refuel, launch; drawn by `RampRenderer` with its drone (in game) |
| Fuel can (20 L), fuel levels on drones | check | `FuelCanItem`, `DroneFuel`; refuel/drain on ramps and munitions, drain from wrecks |
| Tactical map (Mission Planner) | check | `TacticalMapScreen` (26.3 GUI drawing), `MapPopupMenu`, live terrain + region-file tiles (`ServerMapTiles`), routes, recon layer; M key / laptop |

### Weapons and payloads

| Feature | Status | Notes |
|---|---|---|
| Bomb bay + drop bomb key | check | drop key releases the hanging payload or the next bomb-bay station (hold to repeat on bomb bays); predicted on the pilot's client; checked in game with an RGD-5. Bombs start from the drawn payload once the 1.1.8 drone renderer lands |
| RGD-5 grenade | check | item and bouncing, timed-fuse grenade ported; released by the drop key |
| Rocket pod mount + 70mm NAR rockets + fire rocket key | check | mount/load via the interact menu; R fires (`MunitionPacketB`); fixed-wing only, as in 1.1.8 (no quad has pods) |
| TM-62 anti-tank mine + "Can Opener" mine vest (chestplate) | done | vest: `OpenerItem`, `OpenerCookOff`, `OpenerDetonatePacket`, mine drawn by `OpenerArmorRenderer`; drop key and lethal hit set it off; always preloaded (no loading, as in 1.1.8) |
| 120mm mortar + round | n/a | "not added yet" in 1.1.8 too |
| Debug infinite bomb loader | check | /give only; fills the bomb bay, or says the drone has none |
| Projectiles as data (`data/fpvdrone/projectiles`) | check | RPG-7 moved here as well |
| Shrapnel config (lethal range, base/scale damage, cap), explosion-breaks-blocks switch, `/fpvdrones explosive` command, signal range multiplier | check | replaces explosionBlockPower / kamikazeExplosionPower |
| EW jammer block | done | zone lowers signal on the client; F3+B wireframe ring drawn as debug gizmos (not visible in the test client, check in game) |
| New particles (ground debris, raindrops, 3-D scrap, spark clusters) and sounds (engines, rocket pods, distant booms, indoor ambience) | check | `FpvParticles` + textures, `sounds.json` identical; munition engine sound and exhaust (renderer) |

### Controls and screens

| Feature | Status | Notes |
|---|---|---|
| Own key-binding screen (keyboard or controller per action) | check | every action acts, incl. fire rocket and focus for munitions |
| Keyboard throttle/yaw keys, centre mouse stick | check | centre mouse stick read by the munition controls |
| Gimbal tilt setup in the wizard | done | tilting needs the Mavic |
| Interact menu (hover menu on a drone: battery, remote, goggles, projectiles, pick up, ramp actions) | check | drones, munitions and ramps; drone actions smoke-tested |
| Free look (third person, hold) | check | drones and munitions (also through the aim screen) |
| Munition camera and controls (view angles, bank, mouse aim, zoom, FOV, frozen last frame) | check | `CameraMixin` munition branch, `MunitionClientState`, `MouseHandlerMixin`, `VideoFeedMixin` |
| OSD profiles: MAX7456 or ZALA per camera, ShowOsd | check | `render/ZalaOsd`, `OsdFrame`, `GogglesHudOverlay.renderProfile`; OSD in first person only |
| DJI digital feed (no analog low-res pass) | check | `LowResRenderer` |
| Aim screen lets F3 debug keys through | check | `KeyboardHandlerAimScreenMixin` |
| Far-drone pass (drones in chunks the client has not loaded, with Distant Horizons) | check | `FarDrones` + `LevelExtractorFarDroneMixin` (normal entity pass, full-bright), 1.1.8 tracking range (`DroneTrackingRangeMixin`); needs a look with Distant Horizons installed |
| Onboarding hints | done | switched off in 1.1.8 (constant false, handlers empty); ported the same way, `client/interact/OnboardingHints` |
| Tooltips with required / optional / installed parts | done | `DroneTooltipIcons` + `ItemIconTooltipRenderer` (drone items, rocket pod mount, vest) |
| Pets don't follow, mobs don't target, `/tp` lands at the pilot body while a player flies | done | `FollowOwnerGoalMixin`, `TargetingConditionsMixin`, `TeleportCommandMixin` |

### Compatibility

| Feature | Status | Notes |
|---|---|---|
| Immersive Vehicles (MTS) compatibility, weapon damage data | n/a | Immersive Vehicles has no Fabric 26.3 version |
