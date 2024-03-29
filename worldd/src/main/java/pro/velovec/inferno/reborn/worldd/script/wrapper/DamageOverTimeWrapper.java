package pro.velovec.inferno.reborn.worldd.script.wrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pro.velovec.inferno.reborn.common.utils.ByteArray;
import pro.velovec.inferno.reborn.common.utils.ByteConvertible;
import pro.velovec.inferno.reborn.worldd.script.impl.DamageOverTimeBase;
import pro.velovec.inferno.reborn.worldd.world.creature.WorldCreature;
import pro.velovec.inferno.reborn.worldd.world.object.WorldObject;

public class DamageOverTimeWrapper implements ByteConvertible {

    private final long id;

    private final DamageOverTimeBase damageOverTime;
    private final WorldObject caster;

    private long duration;
    private final long basicPotential;
    private final long tickInterval;

    private volatile long nextTick;

    private static final Logger LOGGER = LoggerFactory.getLogger(DamageOverTimeWrapper.class);

    public DamageOverTimeWrapper(DamageOverTimeBase damageOverTime, WorldObject caster, long duration, long tickInterval, long basicPotential, long id) {
        this.damageOverTime = damageOverTime;
        this.caster = caster;

        this.duration = duration;
        this.nextTick = duration;

        this.basicPotential = basicPotential;
        this.tickInterval = tickInterval;

        this.id = id;
    }

    public synchronized void process(long diff, WorldCreature target) {
        duration -= diff;

        if (nextTick >= duration) {
            damageOverTime.tick(caster, target, basicPotential);
            nextTick -= tickInterval;
        }
    }

    public long getDuration() {
        return duration;
    }

    public long getId() {
        return id;
    }

    public WorldObject getCaster() {
        return caster;
    }

    public void extendDuration(long duration) {
        this.duration += duration;
        this.nextTick += duration;
    }

    @Override
    public byte[] toByteArray() {
        return new ByteArray()
            .put(getId())
            .put(getCaster().getOID())
            .toByteArray();
    }
}
