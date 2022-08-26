package pro.velovec.inferno.reborn.worldd.script.impl;

import pro.velovec.inferno.reborn.worldd.script.ScriptableObject;

public interface EffectBase extends ScriptableObject {

    long processPotential(long basicPotential);
    long processDuration(long duration);
    long processTickTime(long tickTime);
    long processCoolDown(long coolDown);
    long processCastTime(long castTime);

}
