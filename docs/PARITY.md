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
| Keyboard and mouse control, force KB&M option | partial | option lives in the missing settings screen |
| Joystick / gamepad (SDL), trackpad | partial | needs a real controller test |
| Rates: rate, super rate, expo per axis, presets | partial | stored; no editor yet |
| Drone build: motor KV/size, cells, props, frame, mass, pro/hero cam, toothpick, colours | partial | stored and used by physics; no editor |
| Camera angle (Page Up/Down), FOV | partial | tilt works; FOV/default angle editor missing |
| Drone surfing (riding on a drone) | check | `DroneSurfHandler`; fixed: after a flight it could fling the pilot up to 10 blocks into the air (also in the original) |
| Collisions, fall types, drone damage, entity hits and kicks | check | |
| Chunk loading around a flying drone | check | |

## FPV and pilot

| Feature | Status | Notes |
|---|---|---|
| Enter / leave FPV with goggles, channels (Shift + scroll) | done | |
| Pilot body standing in for the player, its equipment | done | 1.1.8: falls with gravity, remembered position, free-spot return, own chunk loading |
| Pilot body render in FPV, remote players' lerp | done | `FpvPlayerBodyRenderer`, `RemotePlayerLerpMixin` |
| Goggles worn on the head (armour model) | done | GeckoLib armour renderer; seen on the pilot body and in the inventory. The original's 1 armour point is not ported yet |
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
| OSD layouts: per drone, import/export, sync to server | partial | data and packets ported; editor missing |
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
| Shrapnel debug view | missing | debug only |

## Thermal camera

| Feature | Status | Notes |
|---|---|---|
| Thermal vision (key: End) on thermal drones | missing | 35 classes + 19 core and 26 post shaders |
| Palettes WHOT / BHOT / Ironbow, NUC calibration (U), AGC mode (G), focus mode (Home) | missing | |
| Thermal sky/clouds/blocks/entities, bloom, noise (NETD, FPN), DDE, vignette | missing | |

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
| Dedicated server | check | not yet run |

## Compatibility

| Feature | Status | Notes |
|---|---|---|
| Embeddium: FPV distance limiter, stability workaround, thermal chunk shaders | missing | becomes Sodium (and Iris) compatibility |
| Decorative effects: prop blur discs, "Kirpichiki" bricks | done | 1.1.8: blur comes from each drone model's own blur bones where its file enables it; bricks only for drones whose file asks for them, fading out over 7 s after lift-off, drawn out to the observer range |
| Full drone renderer (props, colours, accessories) | done | props fade into blur discs with throttle |

## Added in 1.1.8

### Drones (data-driven: `data/fpvdrone/drones/*.json`)

| Feature | Status | Notes |
|---|---|---|
| Drone definitions as data files (physics, rates, camera, signal, batteries, projectiles, damage zones, interact zones, sounds) | partly | loaded and synced to clients; the drone entity reads its definition (weight, payloads, interact zones on the model); physics and renderer still use the 1.1.4 code |
| DJI Mavic 3 Pro + DJI RC Pro controller + DJI battery | partly | ported: GPS flight, hover hold, return to home (also on low battery, finished by the server if you leave), gimbal and zoom, view from the held controller; not yet checked in game; model drawing comes with the 1.1.8 drone renderer. Planned in full: GPS-style flight (climb/descend keys), stabilised gimbal with tilt keys, camera zoom, hover hold (brake), return to home (incl. on low battery), bomb bay, flown from the controller held in hand (no goggles) |
| FP-1 loitering munition (fixed wing) | missing | petrol engine, launched from a ramp, route from the tactical map, rocket pod |
| Lancet-51 loitering munition | missing | ramp launch, targeting sight, dive on target |
| Lytyi loitering munition | missing | placed on the ground, fuelled, route from the map |
| Orlan-10 reconnaissance drone | missing | ramp launch, recon role, zoom/focus camera |
| Test target drone | missing | debug / target practice |
| 3-D collision shapes for drones (`geo/collision`) | missing | |
| Bedrock-format models (`bedrock_geo`, simplebedrockmodel library) | check | loaded with GeckoLib 5's model loader instead of simplebedrockmodel; used by projectiles and dropped bombs |

### Launch, fuel and planning

| Feature | Status | Notes |
|---|---|---|
| Drone launch ramp (block) | missing | install a drone, refuel, launch; `data/fpvdrone/ramps` |
| Fuel can (20 L), fuel levels on drones | missing | refuel, drain fuel from wrecks, pour out |
| Tactical map (Mission Planner) | missing | waypoints with height and speed (slow/cruise/fast), loiter circles, terminal modes loiter/dive/destroy, route time and ETA, observe a drone in flight, take control, first-person view |

### Weapons and payloads

| Feature | Status | Notes |
|---|---|---|
| Bomb bay + drop bomb key | check | drop key releases the hanging payload or the next bomb-bay station (hold to repeat on bomb bays); predicted on the pilot's client; checked in game with an RGD-5. Bombs start from the drawn payload once the 1.1.8 drone renderer lands |
| RGD-5 grenade | check | item and bouncing, timed-fuse grenade ported; released by the drop key |
| Rocket pod mount + 70mm NAR rockets + fire rocket key | missing | one-way upgrade, load tubes |
| TM-62 anti-tank mine + "Can Opener" mine vest (chestplate) | missing | `data/fpvdrone/armor/opener.json` |
| 120mm mortar + round | n/a | "not added yet" in 1.1.8 too |
| Debug infinite bomb loader | check | /give only; fills the bomb bay, or says the drone has none |
| Projectiles as data (`data/fpvdrone/projectiles`) | check | RPG-7 moved here as well |
| Shrapnel config (lethal range, base/scale damage, cap), explosion-breaks-blocks switch, `/fpvdrones explosive` command, signal range multiplier | check | replaces explosionBlockPower / kamikazeExplosionPower |
| EW jammer block | done | zone lowers signal on the client; the hitbox-view (F3+B) wireframe ring is not drawn yet |
| New particles (ground debris, raindrops, 3-D scrap, spark clusters) and sounds (engines, rocket pods, distant booms, indoor ambience) | missing | |

### Controls and screens

| Feature | Status | Notes |
|---|---|---|
| Own key-binding screen (keyboard or controller per action) | partly | screens and saving work; arm, exit view, drop, zoom, return to home and hover hold act; fire rocket, camera focus and interact wait for the loitering munitions and the interact menu |
| Keyboard throttle/yaw keys, centre mouse stick | partly | throttle/yaw/climb/descend keys done; centre mouse stick comes with the fixed-wing drones |
| Gimbal tilt setup in the wizard | done | tilting needs the Mavic |
| Interact menu (hover menu on a drone: battery, remote, goggles, projectiles, pick up, ramp actions) | missing | |
| Free look (third person, hold) | partly | key and orbit view ported; off for FPV quads (as in 1.1.8), used by Mavic and fixed-wing |
| Onboarding hints | missing | |
| Tooltips with required / optional / installed parts | missing | |

### Compatibility

| Feature | Status | Notes |
|---|---|---|
| Immersive Vehicles (MTS) compatibility, weapon damage data | n/a | Immersive Vehicles has no Fabric 26.3 version |
