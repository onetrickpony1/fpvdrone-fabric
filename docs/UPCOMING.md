# Upcoming features

New features beyond the original 1.1.8 ([PARITY.md](PARITY.md) is complete in code). Done ones are marked **done** and
described in the Notes.
Effort: S (small), M (medium), L (large).

## Flight controller

| Feature | Effort | Notes |
|---|---|---|
| Angle / Horizon flight modes | M | Self-levelling modes on a switch beside Acro. The OSD shows the real mode (it always says ACRO now). |
| Turtle mode | **done** | Config `turtleMode` (server, synced; **off** by default = 1.1.8, drones right themselves on the ground). On: a drone resting more than 90° over stays on its back. The Turtle Mode action (key Y, or a controller button/switch in the binding screen) toggles it: no lift, OSD warning `CRASH FLIP`, pitch or roll (mouse or sticks) rolls the drone over that edge, yaw spins it on its back; up to 270°/s, slower for heavy drones. It switches itself off once upright (as Betaflight 4.5's crash-flip auto re-arm). Smoke-tested. |
| GPS Rescue / return to home for FPV drones | M | 1.1.8 has return to home on the Mavic only; this adds it to FPV quads (on failsafe or a switch), with a "GPS module" part. |

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

## Languages

| Feature | Effort | Notes |
|---|---|---|
| Maltese localisation (`mt_mt`) | S-M | All in-game text: items, keys, screens, OSD, messages, config comments. About 530 keys. |

## Planned by the original authors

From Mr_Mulle's CurseForge announcement. Most of these arrived in 1.1.8 and are now in [PARITY.md](PARITY.md):
Mavic drones (Mavic 3 Pro), drone grenade dropping (bomb bay), long-range drones (FP-1, Lancet-51, Lytyi,
Orlan-10), launch ramps, programmable flight paths (tactical map) and fuel (fuel can, draining wrecks).

| Feature | Effort | Notes |
|---|---|---|
| Grenade deflection ("Spicy tennis") | S-M | Not in 1.1.8. Hit falling or landed grenades away before they go off. |
