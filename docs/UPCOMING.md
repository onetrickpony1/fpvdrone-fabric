# Upcoming features

New features beyond the original 1.1.4. Work on these starts once [PARITY.md](PARITY.md) is complete.
Effort: S (small), M (medium), L (large).

## Flight controller

| Feature | Effort | Notes |
|---|---|---|
| Angle / Horizon flight modes | M | Self-levelling modes on a switch beside Acro. The OSD shows the real mode (it always says ACRO now). |
| Turtle mode | S | Spin the motors in reverse to flip a crashed drone upright from the controller. |
| GPS Rescue / return to home | M | On failsafe or a switch the drone flies back to its launch point. Needs a "GPS module" part. |

## Physics

| Feature | Effort | Notes |
|---|---|---|
| Wind, gusts, prop wash, ground effect | L | Large project: believable wind is hard. Could start with prop wash and ground effect, which don't need a wind model. |

## Video and radio

| Feature | Effort | Notes |
|---|---|---|
| DVR recording | M-L | Record the goggles feed. Works with the F6 clean feed. |
| Analog vs digital video, channel clashes | M | Analog breaks up into snow and tearing; digital freezes and turns blocky. Ties in with the HD camera part. Two pilots on the same channel interfere. |

## Damage, parts and batteries

| Feature | Effort | Notes |
|---|---|---|
| Per-part damage and field repair | M | Bent props, a burnt motor, a broken camera or antenna, each changing how the drone flies; props can be swapped in the field. |
| Charger block, battery types | M | Charging speed, storage charge, puffed packs after over-discharge; 4S/6S/LiHV/Li-ion. |

## Planned by the original authors

From Mr_Mulle's CurseForge announcement of the next update. Their later 1.1.8 changelog describes some of these (fixed-wing drones, launch ramps, guided missiles, fuel, a waypoint map). This port is written from scratch, so the code here will be our own.

| Feature | Effort | Notes |
|---|---|---|
| Mavic drones | L | Camera drones: GPS-stabilised, slower, hovering, with a gimbal camera. |
| Drone grenade dropping | M | Check first what the existing payloads (grenades Mk.1-3, bomb holder) already do; extend them (e.g. a Mavic drop mechanism). |
| Grenade deflection ("Spicy tennis") | S-M | Players hit falling or landed grenades away before they go off. |
| Long-range drones | L | Bigger drones with longer range and endurance. |
| Drone launch ramps | M | A block or item to launch drones from, mainly fixed-wing ones. |
| Programmable flight paths | L | Waypoints with speed and altitude, loiter, return. |
| Gasoline siphoning from landed or shot-down drones | M | Fuel for petrol-engined drones; siphon it from wrecks. |
