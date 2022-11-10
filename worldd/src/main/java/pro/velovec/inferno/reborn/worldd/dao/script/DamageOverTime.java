package pro.velovec.inferno.reborn.worldd.dao.script;

import org.springframework.context.ConfigurableApplicationContext;

import pro.velovec.inferno.reborn.common.utils.ByteArray;
import pro.velovec.inferno.reborn.common.utils.ByteConvertible;
import pro.velovec.inferno.reborn.worldd.script.ScriptManager;
import pro.velovec.inferno.reborn.worldd.script.impl.DamageOverTimeBase;
import pro.velovec.inferno.reborn.worldd.world.creature.WorldCreature;
import pro.velovec.inferno.reborn.worldd.world.object.WorldObject;

import javax.persistence.*;
import javax.script.ScriptException;
import java.util.List;

@Entity
@Table(name = "spell_damage_over_time_effects") // objects
public class DamageOverTime implements ByteConvertible {

    @Id
    @GeneratedValue
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "basic_potential")
    private long basicPotential;

    @Column(name = "tick_interval")
    private long tickInterval;

    @Column(name = "duration")
    private long duration;

    @Column(name = "type")
    private DamageType damageType;

    @ManyToOne(fetch = FetchType.EAGER)
    private Script script;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getBasicPotential() {
        return basicPotential;
    }

    public void setBasicPotential(long basicPotential) {
        this.basicPotential = basicPotential;
    }

    public long getTickInterval() {
        return tickInterval;
    }

    public void setTickInterval(long tickInterval) {
        this.tickInterval = tickInterval;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }

    public DamageType getDamageType() {
        return damageType;
    }

    public void setDamageType(DamageType type) {
        this.damageType = type;
    }

    public void apply(ConfigurableApplicationContext ctx, WorldObject caster, List<WorldObject> targets) throws ScriptException {
        DamageOverTimeBase dotBase = (DamageOverTimeBase) ctx.getBean(ScriptManager.class).eval(script);

        final long basicPotential = ((WorldCreature) caster).processEffects(EffectDirection.OFFENSE, EffectAttribute.POTENTIAL, this.basicPotential, damageType);
        final long duration = ((WorldCreature) caster).processEffects(EffectDirection.OFFENSE, EffectAttribute.DURATION, this.duration, damageType);
        final long tickInterval = ((WorldCreature) caster).processEffects(EffectDirection.OFFENSE, EffectAttribute.TICK_TIME, this.tickInterval, damageType);

        targets.parallelStream()
            .filter(target -> WorldCreature.class.isAssignableFrom(target.getClass()))
            .forEach(
                target -> ((WorldCreature) target).applyDamageOverTime(dotBase, caster, duration, tickInterval, basicPotential, id, damageType)
            );
    }

    @Override
    public byte[] toByteArray() {
        return new ByteArray()
            .put(id).put(name)
            .put(basicPotential)
            .put(tickInterval).put(duration)
            .put(damageType)
            .toByteArray();
    }
}
