package pro.velovec.inferno.reborn.worldd.world.creature;

import pro.velovec.inferno.reborn.worldd.constants.StatsConversionRates;
import pro.velovec.inferno.reborn.common.dao.character.CharacterData;
import pro.velovec.inferno.reborn.common.utils.ByteArray;
import pro.velovec.inferno.reborn.common.utils.ByteConvertible;
import pro.velovec.inferno.reborn.worldd.dao.script.DamageType;
import pro.velovec.inferno.reborn.worldd.dao.script.CastAttribute;
import pro.velovec.inferno.reborn.worldd.dao.script.CastDirection;

public class WorldCreatureStats implements ByteConvertible {

    private int vitality;
    private int strength;
    private int intelligence;
    private int control;
    private int agility;


    // Main Stats
    public int getVitality() {
        return vitality;
    }

    public void setVitality(int vitality) {
        this.vitality = vitality;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getIntelligence() {
        return intelligence;
    }

    public void setIntelligence(int intelligence) {
        this.intelligence = intelligence;
    }

    public int getControl() {
        return control;
    }

    public void setControl(int control) {
        this.control = control;
    }

    public int getAgility() {
        return agility;
    }

    public void setAgility(int agility) {
        this.agility = agility;
    }


    // Extra Stats
    public int getMaxHealth() {
        return Math.round(
            vitality * StatsConversionRates.VITALITY_TO_HEALTH
        );
    }

    public int getMaxMana() {
        return Math.round(
            control * StatsConversionRates.CONTROL_TO_MANA
        );
    }

    public int getMaxEnergy() {
        return Math.round(
            vitality * StatsConversionRates.VITALITY_TO_ENERGY
        );
    }

    public int getPhysicalProtection() {
        return Math.round(
            vitality * StatsConversionRates.VITALITY_TO_PROTECTION
        );
    }

    public int getMagicalProtection() {
        return Math.round(
            control * StatsConversionRates.CONTROL_TO_PROTECTION
        );
    }

    public float getEvasion() {
        return agility * StatsConversionRates.AGILITY_TO_EVASION;
    }

    public double getMaxWeight() {
        return vitality * StatsConversionRates.VITALITY_TO_WEIGHT;
    }

    public float getMaxSpeed() {
        return agility * StatsConversionRates.AGILITY_TO_SPEED;
    }

    public int getDamageHand() {
        return Math.round(
            strength * StatsConversionRates.STRENGTH_TO_DAMAGE
        );
    }

    public int getDamageWeapon() {
        return Math.round(
            strength * StatsConversionRates.STRENGTH_TO_DAMAGE
        );
    }

    public int getDamageMagic() {
        return Math.round(
            intelligence * StatsConversionRates.INTELLIGENCE_TO_DAMAGE
        );
    }

    public float getRangeWeapon() {
        return strength * StatsConversionRates.STRENGTH_TO_RANGE;
    }

    public float getRangeMagic() {
        return intelligence * StatsConversionRates.INTELLIGENCE_TO_RANGE;
    }

    public float getAccuracy() {
        return agility * StatsConversionRates.AGILITY_TO_ACCURACY;
    }

    @Override
    public byte[] toByteArray() {
        return new ByteArray()
            .put(vitality).put(strength).put(intelligence).put(control).put(agility)
            .put(getMaxHealth()).put(getMaxMana()).put(getMaxEnergy())
            .put(getMaxWeight()).put(getMaxSpeed())
            .put(getPhysicalProtection()).put(getMagicalProtection())
            .put(getEvasion()).put(getAccuracy())
            .put(getDamageHand()).put(getDamageWeapon()).put(getDamageMagic())
            .put(getRangeWeapon()).put(getRangeMagic())
            .toByteArray();
    }

    public static WorldCreatureStats fromCharacterData(CharacterData characterData) {
        WorldCreatureStats stats = new WorldCreatureStats();

        stats.setVitality(characterData.getVitality());
        stats.setStrength(characterData.getStrength());
        stats.setIntelligence(characterData.getIntelligence());
        stats.setControl(characterData.getControl());
        stats.setAgility(characterData.getAgility());

        return stats;
    }

    public long processAttribute(CastDirection direction, CastAttribute attribute, long value, DamageType damageType) {
        if (direction == CastDirection.OFFENSE && damageType == DamageType.PHYSICAL) {
            return processPhysicalOffence(attribute, value);
        } else if (direction == CastDirection.OFFENSE && damageType == DamageType.MAGIC) {
            return processMagicOffence(attribute, value);
        } else if (direction == CastDirection.DEFENSE && damageType == DamageType.PHYSICAL) {
            return processPhysicalDefence(attribute, value);
        } else if (direction == CastDirection.DEFENSE && damageType == DamageType.MAGIC) {
            return processMagicDefence(attribute, value);
        }

        return value;
    }

    private long processMagicDefence(CastAttribute attribute, long value) {
        return value;
    }

    private long processPhysicalDefence(CastAttribute attribute, long value) {
        return value;
    }

    private long processMagicOffence(CastAttribute attribute, long value) {
        return value;
    }

    private long processPhysicalOffence(CastAttribute attribute, long value) {
        return value;
    }

}
