package pro.velovec.inferno.reborn.worldd.world.creature;



import pro.velovec.inferno.reborn.common.constants.CommonConstants;
import pro.velovec.inferno.reborn.common.utils.ByteArray;
import pro.velovec.inferno.reborn.worldd.constants.WorldEventType;
import pro.velovec.inferno.reborn.worldd.dao.script.DamageType;
import pro.velovec.inferno.reborn.worldd.dao.script.CastAttribute;
import pro.velovec.inferno.reborn.worldd.dao.script.CastDirection;
import pro.velovec.inferno.reborn.worldd.dao.script.EffectType;
import pro.velovec.inferno.reborn.worldd.script.impl.DamageOverTimeBase;
import pro.velovec.inferno.reborn.worldd.script.impl.EffectBase;
import pro.velovec.inferno.reborn.worldd.script.wrapper.DamageOverTimeWrapper;
import pro.velovec.inferno.reborn.worldd.script.wrapper.EffectWrapper;
import pro.velovec.inferno.reborn.worldd.world.WorldNotificationListener;
import pro.velovec.inferno.reborn.worldd.world.object.WorldObject;
import pro.velovec.inferno.reborn.worldd.world.object.WorldObjectType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class WorldCreature extends WorldObject {

    private volatile int level = 1;

    protected WorldCreatureStats creatureStats;

    private volatile long currentHitPoints;
    private WorldCreatureStatus status = WorldCreatureStatus.ALIVE;

    private final List<EffectWrapper> effects;
    private final List<DamageOverTimeWrapper> damageOverTime;

    public WorldCreature(WorldNotificationListener notificationListener, String name, WorldCreatureStats creatureStats) {
        super(notificationListener, name);

        setType(WorldObjectType.CREATURE);

        this.creatureStats = creatureStats;

        this.effects = new CopyOnWriteArrayList<>();
        this.damageOverTime = new CopyOnWriteArrayList<>();

        this.currentHitPoints = creatureStats.getMaxHealth();
    }

    @Override
    public ByteArray getAttributes() {
        return super.getAttributes()
            .put(level)
            .put(creatureStats)
            .put(currentHitPoints)
            .put(status);
    }


    public int getAvailableStatPoints() {
        Integer[] stats = new Integer[] {
            creatureStats.getVitality(),
            creatureStats.getStrength(),
            creatureStats.getIntelligence(),
            creatureStats.getControl(),
            creatureStats.getAgility()
        };

        int statsSum = Arrays.stream(stats).mapToInt((x) -> x).sum();
        int statsMax = CommonConstants.INITIAL_STATS_SUM + CommonConstants.STAT_POINTS_PER_LEVEL * getLevel();

        return statsMax - statsSum;
    }

    public long getCurrentHitPoints() {
        return currentHitPoints;
    }

    protected void setCurrentHitPoints(long currentHitPoints) {
        this.currentHitPoints = currentHitPoints;
    }

    public WorldCreatureStatus getStatus() {
        return status;
    }

    protected void setLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public synchronized void processHitPointChange(long hitPointChange, DamageType damageType) {
        hitPointChange = processEffects(CastDirection.DEFENSE, CastAttribute.POTENTIAL, hitPointChange, damageType);

        currentHitPoints = Math.min(Math.max(currentHitPoints + hitPointChange, 0), creatureStats.getMaxHealth());
        currentCell.onEvent(this, WorldEventType.ATTRIBUTE_CHANGE, getAttributes());

        if (currentHitPoints == 0)
            processStatusChange(WorldCreatureStatus.DEAD);
    }

    public void processStatusChange(WorldCreatureStatus newStatus) {
        if (status != newStatus) {
            status = newStatus;

            currentCell.onEvent(this, WorldEventType.ATTRIBUTE_CHANGE, getAttributes());
        }
    }

    public void processStatsChange(WorldCreatureStats creatureStats) {
        this.creatureStats = creatureStats;

        currentCell.onEvent(this, WorldEventType.ATTRIBUTE_CHANGE, getAttributes());
    }

    public void applyEffect(EffectBase effect, WorldObject caster, long duration, EffectType type, long id, DamageType damageType) {
        duration = processEffects(CastDirection.DEFENSE, CastAttribute.DURATION, duration, damageType);

        switch (type) {
            case AURA -> applyAura(effect, caster, duration, type, id);
            case BUFF, DEBUFF -> applyBuff(effect, caster, duration, type, id);
        }
    }

    private void applyAura(EffectBase effect, WorldObject caster, long duration, EffectType type, long id) {
        EffectWrapper wrapper = this.effects.parallelStream()
            .filter(effectWrapper -> effectWrapper.getId() == id && effectWrapper.getCaster().equals(caster))
            .findFirst().orElse(null);

        if (Objects.nonNull(wrapper)) {
            this.effects.remove(wrapper);
            currentCell.onEvent(this, WorldEventType.EFFECT_REMOVE, wrapper);
        } else {
            wrapper = new EffectWrapper(effect, caster, duration, type, id);
            this.effects.add(wrapper);
            currentCell.onEvent(this, WorldEventType.EFFECT_ADD, wrapper);
        }
    }

    private void applyBuff(EffectBase effect, WorldObject caster, long duration, EffectType type, long id) {
        EffectWrapper wrapper = this.effects.parallelStream()
            .filter(effectWrapper -> effectWrapper.getId() == id && effectWrapper.getCaster().equals(caster))
            .findFirst().orElse(null);

        if (Objects.nonNull(wrapper)) {
            wrapper.extendDuration(duration);
            currentCell.onEvent(this, WorldEventType.EFFECT_UPDATE, wrapper);
        } else {
            wrapper = new EffectWrapper(effect, caster, duration, type, id);
            this.effects.add(wrapper);
            currentCell.onEvent(this, WorldEventType.EFFECT_ADD, wrapper);
        }
    }

    public void applyDamageOverTime(DamageOverTimeBase damageOverTime, WorldObject caster, long duration, long tickInterval, long basicPotential, long id, DamageType damageType) {
        DamageOverTimeWrapper wrapper = this.damageOverTime.parallelStream()
            .filter(damageOverTimeWrapper -> damageOverTimeWrapper.getId() == id && damageOverTimeWrapper.getCaster().equals(caster))
            .findFirst().orElse(null);

        duration = processEffects(CastDirection.DEFENSE, CastAttribute.DURATION, duration, damageType);
        tickInterval = processEffects(CastDirection.DEFENSE, CastAttribute.TICK_TIME, tickInterval, damageType);

        if (Objects.nonNull(wrapper)) {
            wrapper.extendDuration(duration);
            currentCell.onEvent(this, WorldEventType.DOT_UPDATE, wrapper);
        } else {
            wrapper = new DamageOverTimeWrapper(damageOverTime, caster, duration, tickInterval, basicPotential, id);
            this.damageOverTime.add(wrapper);
            currentCell.onEvent(this, WorldEventType.DOT_ADD, wrapper);
        }
    }

    @Override
    public void update(long diff) {
        super.update(diff);

        this.effects.parallelStream()
            .peek(effect -> effect.process(diff, this))
            .filter(effect -> effect.getDuration() <= 0)
            .forEach(effect -> {
                effects.remove(effect);
                currentCell.onEvent(
                    this, WorldEventType.EFFECT_REMOVE, effect
                );
            });

        this.damageOverTime.parallelStream()
            .peek(dot -> dot.process(diff, this))
            .filter(dot -> dot.getDuration() <= 0)
            .forEach(dot -> {
                damageOverTime.remove(dot);
                currentCell.onEvent(
                    this, WorldEventType.DOT_REMOVE, dot
                );
            });
    }

    // Effect processors

    public long processEffects(CastDirection direction, CastAttribute attribute, long value, DamageType damageType) {
        final long[] input = new long[] { value };

        this.effects.forEach(
            effect -> input[0] = effect.processAttribute(direction, attribute, input[0], damageType)
        );

        input[0] = this.creatureStats.processAttribute(direction, attribute, input[0], damageType);

        LOGGER.debug(String.format(
            "WorldCreature<%s>: Processed effect attribute <%s:%s> from '%s' to '%s'",
            getOID(), direction, attribute, value, input[0]
        ));

        return input[0];
    }

    public double processEffects(CastDirection direction, CastAttribute attribute, double value, DamageType damageType) {
        return processEffects(direction, attribute, Math.round(value * 1000), damageType) / 1000.0;
    }
}
