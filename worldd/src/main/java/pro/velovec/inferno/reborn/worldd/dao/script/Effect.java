package pro.velovec.inferno.reborn.worldd.dao.script;


import org.springframework.context.ConfigurableApplicationContext;
import pro.velovec.inferno.reborn.common.utils.ByteArray;
import pro.velovec.inferno.reborn.common.utils.ByteConvertible;
import pro.velovec.inferno.reborn.worldd.script.ScriptManager;
import pro.velovec.inferno.reborn.worldd.script.impl.EffectBase;
import pro.velovec.inferno.reborn.worldd.world.creature.WorldCreature;
import pro.velovec.inferno.reborn.worldd.world.object.WorldObject;

import javax.persistence.*;
import javax.script.ScriptException;
import java.util.List;

@Entity
@Table(name = "spell_effects") // objects
public class Effect implements ByteConvertible {

    @Id
    @GeneratedValue
    private int id;

    private String name;

    private long duration;

    private EffectType type;

    private EffectDirection direction;

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

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public EffectType getType() {
        return type;
    }

    public void setType(EffectType type) {
        this.type = type;
    }

    public EffectDirection getDirection() {
        return direction;
    }

    public void setDirection(EffectDirection direction) {
        this.direction = direction;
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

    public void setDamageType(DamageType damageType) {
        this.damageType = damageType;
    }

    public void apply(ConfigurableApplicationContext ctx, WorldObject caster, List<WorldObject> targets) throws ScriptException {
        EffectBase effectBase = (EffectBase) ctx.getBean(ScriptManager.class).eval(script);

        final long duration = ((WorldCreature) caster).processEffects(EffectDirection.OFFENSE, EffectAttribute.DURATION, this.duration, damageType);

        targets.parallelStream()
            .filter(target -> WorldCreature.class.isAssignableFrom(target.getClass()))
            .forEach(
                target -> ((WorldCreature) target).applyEffect(effectBase, caster, duration, type, direction, id, damageType)
            );
    }

    @Override
    public byte[] toByteArray() {
        return new ByteArray()
            .put(id).put(name)
            .put(duration)
            .put(damageType)
            .toByteArray();
    }
}
