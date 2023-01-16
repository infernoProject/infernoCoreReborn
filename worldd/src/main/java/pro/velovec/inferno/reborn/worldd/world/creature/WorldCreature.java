package pro.velovec.inferno.reborn.worldd.world.creature;



import pro.velovec.inferno.reborn.common.constants.CommonConstants;
import pro.velovec.inferno.reborn.common.utils.ByteArray;
import pro.velovec.inferno.reborn.worldd.constants.WorldEventType;
import pro.velovec.inferno.reborn.worldd.dao.script.DamageType;
import pro.velovec.inferno.reborn.worldd.dao.script.EffectAttribute;
import pro.velovec.inferno.reborn.worldd.dao.script.EffectDirection;
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
import java.util.stream.Collectors;

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
        hitPointChange = processEffects(EffectDirection.DEFENSE, EffectAttribute.POTENTIAL, hitPointChange, damageType);

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

    public void applyEffect(EffectBase effect, WorldObject caster, long duration, EffectType type, EffectDirection direction, long id, DamageType damageType) {
        duration = processEffects(EffectDirection.DEFENSE, EffectAttribute.DURATION, duration, damageType);

        switch (type) {
            case AURA -> applyAura(effect, caster, duration, type, direction, id);
            case BUFF, DEBUFF -> applyBuff(effect, caster, duration, type, direction, id);
        }
    }

    private void applyAura(EffectBase effect, WorldObject caster, long duration, EffectType type, EffectDirection direction, long id) {
        EffectWrapper wrapper = this.effects.parallelStream()
            .filter(effectWrapper -> effectWrapper.getId() == id && effectWrapper.getCaster().equals(caster))
            .findFirst().orElse(null);

        if (Objects.nonNull(wrapper)) {
            this.effects.remove(wrapper);
            currentCell.onEvent(this, WorldEventType.EFFECT_REMOVE, new ByteArray().put(id).put(caster.getOID()).put(type));
        } else {
            this.effects.add(new EffectWrapper(effect, caster, duration, type, direction, id));
            currentCell.onEvent(this, WorldEventType.EFFECT_ADD, new ByteArray().put(id).put(caster.getOID()).put(type).put(duration));
        }
    }

    private void applyBuff(EffectBase effect, WorldObject caster, long duration, EffectType type, EffectDirection direction, long id) {
        EffectWrapper wrapper = this.effects.parallelStream()
            .filter(effectWrapper -> effectWrapper.getId() == id && effectWrapper.getCaster().equals(caster))
            .findFirst().orElse(null);

        if (Objects.nonNull(wrapper)) {
            wrapper.extendDuration(duration);
            currentCell.onEvent(this, WorldEventType.EFFECT_UPDATE, new ByteArray().put(id).put(caster.getOID()).put(type).put(wrapper.getDuration()));
        } else {
            this.effects.add(new EffectWrapper(effect, caster, duration, type, direction, id));
            currentCell.onEvent(this, WorldEventType.EFFECT_ADD, new ByteArray().put(id).put(caster.getOID()).put(type).put(duration));
        }
    }

    public void applyDamageOverTime(DamageOverTimeBase damageOverTime, WorldObject caster, long duration, long tickInterval, long basicPotential, long id, DamageType damageType) {
        DamageOverTimeWrapper wrapper = this.damageOverTime.parallelStream()
            .filter(damageOverTimeWrapper -> damageOverTimeWrapper.getId() == id && damageOverTimeWrapper.getCaster().equals(caster))
            .findFirst().orElse(null);

        duration = processEffects(EffectDirection.DEFENSE, EffectAttribute.DURATION, duration, damageType);
        tickInterval = processEffects(EffectDirection.DEFENSE, EffectAttribute.TICK_TIME, tickInterval, damageType);

        if (Objects.nonNull(wrapper)) {
            wrapper.extendDuration(duration);
            currentCell.onEvent(this, WorldEventType.DOT_UPDATE, new ByteArray().put(id).put(caster.getOID()).put(wrapper.getDuration()));
        } else {
            this.damageOverTime.add(new DamageOverTimeWrapper(damageOverTime, caster, duration, tickInterval, basicPotential, id));
            currentCell.onEvent(this, WorldEventType.DOT_ADD, new ByteArray().put(id).put(caster.getOID()).put(duration));
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
                currentCell.onEvent(this, WorldEventType.EFFECT_REMOVE, new ByteArray().put(effect.getId()).put(effect.getCaster().getOID()).put(effect.getType()));
            });

        this.damageOverTime.parallelStream()
            .peek(dot -> dot.process(diff, this))
            .filter(dot -> dot.getDuration() <= 0)
            .forEach(dot -> {
                damageOverTime.remove(dot);
                currentCell.onEvent(this, WorldEventType.DOT_REMOVE, new ByteArray().put(dot.getId()).put(dot.getCaster().getOID()));
            });
    }

    // Effect processors

    public long processEffects(EffectDirection direction, EffectAttribute attribute, long value, DamageType damageType) {
        final long[] input = new long[] { value };

        List<EffectWrapper> effects = this.effects.stream()
            .filter(effect -> direction.equals(effect.getDirection()))
            .filter(effect -> effect.checkDamageType(damageType))
            .toList();

        switch (attribute) {
            case POTENTIAL -> effects.forEach(effect -> input[0] = effect.getEffect().processPotential(input[0]));
            case DURATION -> effects.forEach(effect -> input[0] = effect.getEffect().processDuration(input[0]));
            case TICK_TIME -> effects.forEach(effect -> input[0] = effect.getEffect().processTickTime(input[0]));
            case COOLDOWN -> effects.forEach(effect -> input[0] = effect.getEffect().processCoolDown(input[0]));
            case CAST_TIME -> effects.forEach(effect -> input[0] = effect.getEffect().processCastTime(input[0]));
            default -> logger.warn("Unknown effect attribute: {}", attribute);
        }

        return input[0];
    }
}
