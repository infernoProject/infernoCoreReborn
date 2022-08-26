package pro.velovec.inferno.reborn.worldd.script.impl;

import org.springframework.context.ConfigurableApplicationContext;

import pro.velovec.inferno.reborn.worldd.script.ScriptableObject;
import pro.velovec.inferno.reborn.worldd.world.object.WorldObject;

@FunctionalInterface
public interface SpellBase extends ScriptableObject {

    void cast(ConfigurableApplicationContext ctx, WorldObject caster, WorldObject target, long basicPotential);
}
