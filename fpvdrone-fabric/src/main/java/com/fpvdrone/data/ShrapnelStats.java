package com.fpvdrone.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Damage numbers of one warhead: the {@code "Stats"} block of a projectile file in
 * {@code data/fpvdrone/projectiles/}. In 1.1.4 these were global config values.
 *
 * @param lethalRange inner radius (blocks) with flat peak shrapnel damage
 * @param baseDamage  shrapnel peak = baseDamage + scaleDamage * power
 * @param scaleDamage shrapnel peak added per unit of explosion power
 * @param damageCap   largest non-instant-kill damage dealt to one entity
 * @param blastRadius radius of the blast-wave pass
 * @param blastDamage blast-wave damage at the edge of the kill zone
 * @param killZone    instant-kill radius
 * @param blockPower  block-breaking strength as a fraction of the explosion power (needs explosionBreaksBlocks)
 * @param opensDoors  blast forces doors, trapdoors and gates open
 */
public record ShrapnelStats(
    double lethalRange,
    float baseDamage,
    float scaleDamage,
    float damageCap,
    float blastRadius,
    float blastDamage,
    double killZone,
    float blockPower,
    boolean opensDoors
) {
    public static final Codec<ShrapnelStats> CODEC = RecordCodecBuilder.create(
        i -> i.group(
                Codec.doubleRange(0.0, 8.0).optionalFieldOf("LethalRange", 1.5).forGetter(ShrapnelStats::lethalRange),
                Codec.floatRange(0.0F, 100.0F).optionalFieldOf("BaseDamage", 10.0F).forGetter(ShrapnelStats::baseDamage),
                Codec.floatRange(0.0F, 50.0F).optionalFieldOf("ScaleDamage", 3.0F).forGetter(ShrapnelStats::scaleDamage),
                Codec.floatRange(1.0F, 200.0F).optionalFieldOf("DamageCap", 60.0F).forGetter(ShrapnelStats::damageCap),
                Codec.floatRange(1.0F, 32.0F).optionalFieldOf("BlastRadius", 10.0F).forGetter(ShrapnelStats::blastRadius),
                Codec.floatRange(0.0F, 200.0F).optionalFieldOf("BlastDamage", 40.0F).forGetter(ShrapnelStats::blastDamage),
                Codec.doubleRange(0.0, 16.0).optionalFieldOf("KillZone", 2.0).forGetter(ShrapnelStats::killZone),
                Codec.floatRange(0.0F, 2.0F).optionalFieldOf("BlockPower", 0.0F).forGetter(ShrapnelStats::blockPower),
                Codec.BOOL.optionalFieldOf("OpensDoors", true).forGetter(ShrapnelStats::opensDoors)
            )
            .apply(i, ShrapnelStats::new)
    );

    /** The code fallback (1.1.8 ProjectileDefinitions' default): the 1.1.4 config defaults, no block damage. */
    public static final ShrapnelStats DEFAULT = new ShrapnelStats(1.5, 10.0F, 3.0F, 60.0F, 10.0F, 40.0F, 2.0, 0.0F, true);

    /** Peak shrapnel damage for an explosion of this power. */
    public float peakDamage(float power) {
        return this.baseDamage + this.scaleDamage * power;
    }

    /** Shrapnel reach: at least one block past the lethal range, else three blocks per unit of power. */
    public double shrapnelRadius(float power) {
        return Math.max(this.lethalRange + 1.0, power * 3.0);
    }

    /** The widest radius any pass of this explosion reaches (chunk hold, vehicle passes). */
    public double outerRadius(float power) {
        return Math.max(this.shrapnelRadius(power), this.blastRadius);
    }
}
