# Upcoming features

New features beyond the original 1.1.4. Work on these starts once [PARITY.md](PARITY.md) is complete.
Effort: S (small), M (medium), L (large).

## Flight controller

| Feature | Effort | Notes |
|---|---|---|
| Angle / Horizon flight modes | M | Self-levelling modes on a switch beside Acro. The OSD shows the real mode (it always says ACRO now). |
| Turtle mode | S | Spin the motors in reverse to flip a crashed drone upright from the controller. |
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
