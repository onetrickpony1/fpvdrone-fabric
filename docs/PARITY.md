# Feature parity with FPV Drones to Minecraft 1.1.4

Every feature of the original (Forge 1.20.1), from its classes, language file, keys, config, recipes and
assets, and where it stands in this Fabric 26.3 port.

Status: **done** (ported and checked in game), **partial** (some of it), **missing**, **check** (ported but not
yet verified against the original's behaviour).

## Items and recipes

| Feature | Status | Notes |
|---|---|---|
| Drone 9", thermal drone 9" | check | thermal drone needs the thermal camera to matter |
| Remote controller, FPV goggles, battery (6S 3300 mAh) | done | |
| Parts: antenna, camera, electric motor, propeller, processor, thermal camera | done | crafting ingredients |
| Payloads: grenades Mk.1-3, bomb holder, RPG-7 round, dead weight | check | attach, drop, detonate; weight +100 g |
| BetaFlight configurator item | missing | opens the OSD/drone configurator (GUI) |
| 13 recipes (processor recipe disabled in the original too) | done | |
| Item tooltips (Hold Shift usage, battery info, link state) | check | |
| Creative tab "FPV to Minecraft" | done | |

## Flight

| Feature | Status | Notes |
|---|---|---|
| Physics core (motors, props, battery voltage sag, drag, 3D mode) | check | `physics/` ported line for line |
| Arm / disarm (N), arm switch on a controller | partial | key works; controller arm switch to verify |
| Keyboard and mouse control, force KB&M option | partial | option lives in the missing settings screen |
| Joystick / gamepad (SDL), trackpad | partial | needs a real controller test |
| Rates: rate, super rate, expo per axis, presets | partial | stored; no editor yet |
| Drone build: motor KV/size, cells, props, frame, mass, pro/hero cam, toothpick, colours | partial | stored and used by physics; no editor |
| Camera angle (Page Up/Down), FOV | partial | tilt works; FOV/default angle editor missing |
| Drone surfing (riding on a drone) | check | `DroneSurfHandler` |
| Collisions, fall types, drone damage, entity hits and kicks | check | |
| Chunk loading around a flying drone | check | |

## FPV and pilot

| Feature | Status | Notes |
|---|---|---|
| Enter / leave FPV with goggles, channels (Shift + scroll) | done | |
| Pilot body standing in for the player, its equipment | done | |
| Pilot body render in FPV, remote players' lerp | partial | `FpvPlayerBodyRenderer`, `LivingEntityLerpMixin` missing |
| Goggles worn on the head (armour model) | missing | `FpvGogglesArmorRenderer`, goggles geo model |
| Inventory screen while flying | partial | original draws the pilot body there |
| Villagers panic at drones | missing | `VillagerPanicHandler` |
| Teleport to / switch drone, saved drone positions | check | |

## HUD, OSD and video

| Feature | Status | Notes |
|---|---|---|
| Goggles HUD: armed, no remote, low/no signal, status lines, battery | missing | `GogglesHudOverlay` parked |
| Stick overlay | missing | `StickHudOverlay` parked |
| F1 hides HUD and own drone | done | |
| OSD: MAX7456 character grid, MCM font, all OSD elements | partial | element model ported; renderer parked |
| OSD layouts: per drone, import/export, sync to server | partial | data and packets ported; editor missing |
| Low-resolution video, Cycle Resolution key (F7) | missing | `LowResRenderer`, `VideoResolution` |
| Video freeze / stutter at range | missing | `FrameFreezer`, `FrozenFrameManager` |
| Boot screen when the goggles start | check | `BootStateManager` |
| Near-plane and camera fixes in FPV | missing | `GameRendererMixin` (237 lines, 16 ported) |

## Signal and range

| Feature | Status | Notes |
|---|---|---|
| Signal strength: distance, obstacles (block attenuation), altitude, ground proximity | check | |
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
| Remote-Controller Settings (controller, channel mapping, rates, drone build, other settings) | partial | a simple F8 screen stands in |
| Calibration wizard (sticks, arm switch, complete) | partial | simple calibration only |
| BetaFlight configurator (rates, physics, 3D mode, OSD, drone name, presets) | missing | |
| OSD editor and import | missing | parked |

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
| Decorative effects: prop blur discs, "Kirpichiki" bricks | missing | `BlurDiscRenderer`, `KirpichikiRenderer` parked |
| Full drone renderer (props, colours, accessories) | partial | 175 of 362 lines |
