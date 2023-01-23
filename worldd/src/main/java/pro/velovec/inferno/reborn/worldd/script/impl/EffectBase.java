package pro.velovec.inferno.reborn.worldd.script.impl;

import pro.velovec.inferno.reborn.worldd.dao.script.CastAttribute;
import pro.velovec.inferno.reborn.worldd.script.ScriptableObject;

public interface EffectBase extends ScriptableObject {

    long processPhysicalOffence(CastAttribute attribute, long value);

    long processMagicOffence(CastAttribute attribute, long value);

    long processPhysicalDefence(CastAttribute attribute, long value);

    long processMagicDefence(CastAttribute attribute, long value);
}
