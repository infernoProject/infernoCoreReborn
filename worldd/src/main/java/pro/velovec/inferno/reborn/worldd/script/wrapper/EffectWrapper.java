package pro.velovec.inferno.reborn.worldd.script.wrapper;


import pro.velovec.inferno.reborn.common.utils.ByteArray;
import pro.velovec.inferno.reborn.common.utils.ByteConvertible;
import pro.velovec.inferno.reborn.worldd.dao.script.CastAttribute;
import pro.velovec.inferno.reborn.worldd.dao.script.CastDirection;
import pro.velovec.inferno.reborn.worldd.dao.script.EffectType;
import pro.velovec.inferno.reborn.worldd.script.impl.EffectBase;
import pro.velovec.inferno.reborn.worldd.dao.script.DamageType;
import pro.velovec.inferno.reborn.worldd.world.creature.WorldCreature;
import pro.velovec.inferno.reborn.worldd.world.object.WorldObject;

public class EffectWrapper implements ByteConvertible {

    private final long id;

    private final EffectBase effect;
    private final EffectType type;
    private final WorldObject caster;

    private long duration;

    public EffectWrapper(EffectBase effect, WorldObject caster, long duration, EffectType type, long id) {
        this.effect = effect;
        this.type = type;
        this.caster = caster;

        this.duration = duration;
        this.id = id;
    }

    public void process(long diff, WorldCreature target) {
        duration -= diff;
    }

    public long getDuration() {
        return duration;
    }

    public EffectBase getEffect() {
        return effect;
    }

    public WorldObject getCaster() {
        return caster;
    }

    public EffectType getType() {
        return type;
    }

    public long getId() {
        return id;
    }

    public void extendDuration(long duration) {
        this.duration += duration;
    }

    public long processAttribute(CastDirection direction, CastAttribute attribute, long value, DamageType damageType) {
        if (direction == CastDirection.OFFENSE && damageType == DamageType.PHYSICAL) {
            return effect.processPhysicalOffence(attribute, value);
        } else if (direction == CastDirection.OFFENSE && damageType == DamageType.MAGIC) {
            return effect.processMagicOffence(attribute, value);
        } else if (direction == CastDirection.DEFENSE && damageType == DamageType.PHYSICAL) {
            return effect.processPhysicalDefence(attribute, value);
        } else if (direction == CastDirection.DEFENSE && damageType == DamageType.MAGIC) {
            return effect.processMagicDefence(attribute, value);
        }

        return value;
    }

    @Override
    public byte[] toByteArray() {
        return new ByteArray()
            .put(getId())
            .put(getCaster().getOID())
            .put(getType())
            .toByteArray();
    }
}
