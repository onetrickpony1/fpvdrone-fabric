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
| Pilot body standing in for the player, its equipment | done | |
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
| F1 hides HUD and own drone | done | |
| OSD: MAX7456 character grid, MCM font, all OSD elements | done | |
| OSD layouts: per drone, import/export, sync to server | partial | data and packets ported; editor missing |
| Low-resolution video, Cycle Resolution key (F7, creative), range-scaled resolution (survival) | done | GPU render passes instead of glBlitFramebuffer |
| Video freeze / stutter at range, frozen last frame on signal loss | done | stutter not yet watched at real range |
| Boot screen when the goggles start | check | `BootStateManager` |
| Near-plane fix in FPV (0.01) | done | camera orientation was already in the port's CameraMixin |

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
| Decorative effects: prop blur discs, "Kirpichiki" bricks | done | bricks under a drone until it is first armed (as in the original, they do not fade) |
| Full drone renderer (props, colours, accessories) | done | props fade into blur discs with throttle |
