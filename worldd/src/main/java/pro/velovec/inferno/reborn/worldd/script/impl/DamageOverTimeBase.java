package pro.velovec.inferno.reborn.worldd.script.impl;


import pro.velovec.inferno.reborn.worldd.script.ScriptableObject;
import pro.velovec.inferno.reborn.worldd.world.object.WorldObject;

@FunctionalInterface
public interface DamageOverTimeBase extends ScriptableObject {

    void tick(WorldObject caster, WorldObject target, long basicPotential);
}
