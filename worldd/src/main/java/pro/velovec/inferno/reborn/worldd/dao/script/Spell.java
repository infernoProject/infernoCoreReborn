package pro.velovec.inferno.reborn.worldd.dao.script;

import org.springframework.context.ConfigurableApplicationContext;
import pro.velovec.inferno.reborn.common.dao.data.ClassInfo;
import pro.velovec.inferno.reborn.common.utils.ByteArray;
import pro.velovec.inferno.reborn.common.utils.ByteConvertible;
import pro.velovec.inferno.reborn.worldd.script.ScriptManager;
import pro.velovec.inferno.reborn.worldd.script.impl.SpellBase;
import pro.velovec.inferno.reborn.worldd.world.creature.WorldCreature;
import pro.velovec.inferno.reborn.worldd.world.object.WorldObject;

import javax.persistence.*;
import javax.script.ScriptException;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "spells") // objects
public class Spell implements ByteConvertible {

    @Id
    @GeneratedValue
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private SpellType type;


    @ManyToOne(fetch = FetchType.EAGER)
    private ClassInfo requiredClass;

    @Column(name = "required_level")
    private long requiredLevel;

    @Column(name = "cool_down")
    private long coolDown;

    @Column(name = "distance")
    private float distance;

    @Column(name = "radius")
    private float radius;

    @Column(name = "basic_potential")
    private long basicPotential;


    @ManyToOne(fetch = FetchType.EAGER)
    private Effect effect;


    @ManyToOne(fetch = FetchType.EAGER)
    private DamageOverTime damageOverTime;


    @ManyToOne(fetch = FetchType.EAGER)
    private Script script;

    private DamageType damageType;

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

    public SpellType getType() {
        return type;
    }

    public void setType(SpellType type) {
        this.type = type;
    }

    public ClassInfo getRequiredClass() {
        return requiredClass;
    }

    public void setRequiredClass(ClassInfo requiredClass) {
        this.requiredClass = requiredClass;
    }

    public long getRequiredLevel() {
        return requiredLevel;
    }

    public void setRequiredLevel(long requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    public long getCoolDown() {
        return coolDown;
    }

    public void setCoolDown(long coolDown) {
        this.coolDown = coolDown;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public long getBasicPotential() {
        return basicPotential;
    }

    public void setBasicPotential(long basicPotential) {
        this.basicPotential = basicPotential;
    }

    public Effect getEffect() {
        return effect;
    }

    public void setEffect(Effect effect) {
        this.effect = effect;
    }

    public DamageOverTime getDamageOverTime() {
        return damageOverTime;
    }

    public void setDamageOverTime(DamageOverTime damageOverTime) {
        this.damageOverTime = damageOverTime;
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

    public void cast(ConfigurableApplicationContext ctx, WorldObject caster, List<WorldObject> targets) throws ScriptException {
        SpellBase spellBase = (SpellBase) ctx.getBean(ScriptManager.class).eval(script);

        final long basicPotential = ((WorldCreature) caster).processEffects(EffectDirection.OFFENSE, EffectAttribute.POTENTIAL, this.basicPotential, this.damageType);

        targets.parallelStream().forEach(
            target -> spellBase.cast(ctx, caster, target, basicPotential)
        );

        if (Objects.nonNull(damageOverTime)) {
            damageOverTime.apply(ctx, caster, targets);
        }

        if (Objects.nonNull(effect)) {
            effect.apply(ctx, caster, targets);
        }

        long coolDown = ((WorldCreature) caster).processEffects(EffectDirection.OFFENSE, EffectAttribute.COOLDOWN, this.coolDown, this.damageType);
        caster.addCoolDown(id, coolDown);
    }

    @Override
    public byte[] toByteArray() {
        return new ByteArray()
            .put(id).put(name).put(type.toString().toLowerCase())
            .put(distance).put(radius).put(basicPotential)
            .put(coolDown).put(damageType)
            .toByteArray();
    }
}
