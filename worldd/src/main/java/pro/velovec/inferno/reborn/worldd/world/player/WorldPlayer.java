package pro.velovec.inferno.reborn.worldd.world.player;

import pro.velovec.inferno.reborn.common.constants.CommonConstants;
import pro.velovec.inferno.reborn.common.dao.character.CharacterClass;
import pro.velovec.inferno.reborn.common.dao.character.CharacterData;
import pro.velovec.inferno.reborn.common.dao.character.CharacterInfo;
import pro.velovec.inferno.reborn.common.utils.ByteArray;
import pro.velovec.inferno.reborn.worldd.map.WorldMap;
import pro.velovec.inferno.reborn.worldd.world.WorldNotificationListener;
import pro.velovec.inferno.reborn.worldd.world.creature.WorldCreature;
import pro.velovec.inferno.reborn.worldd.world.creature.WorldCreatureStats;
import pro.velovec.inferno.reborn.worldd.world.movement.WorldPosition;
import pro.velovec.inferno.reborn.worldd.world.object.WorldObjectType;

import java.util.List;
import java.util.stream.Collectors;

public class WorldPlayer extends WorldCreature {

    private final CharacterInfo characterInfo;
    private final CharacterData characterData;
    private final List<CharacterClass> classList;

    private volatile long currentManaPoints = 10L;
    private volatile long currentEnergyPoints = 10L;

    public WorldPlayer(WorldNotificationListener notificationListener, CharacterInfo characterInfo, CharacterData characterData, List<CharacterClass> classList) {
        super(notificationListener, String.format(
            "%s %s", characterInfo.getFirstName(), characterInfo.getLastName()
        ), WorldCreatureStats.fromCharacterData(characterData));

        setType(WorldObjectType.PLAYER);

        this.characterInfo = characterInfo;
        this.characterData = characterData;
        this.classList = classList;

        setPosition(new WorldPosition(
            characterData.getLocation(),
            characterData.getPositionX(),
            characterData.getPositionY(),
            characterData.getPositionZ(),
            characterData.getOrientation()
        ));

        setLevel(characterData.getLevel());
    }

    public CharacterInfo getCharacterInfo() {
        return characterInfo;
    }

    @Override
    public ByteArray getAttributes() {
        return super.getAttributes()
            .put(characterInfo.getBody());
    }

    public ByteArray getState() {
        return new ByteArray()
            .put(creatureStats)
            .put(classList)
            .put(getCurrentHitPoints())
            .put(getCurrentManaPoints())
            .put(getCurrentEnergyPoints())
            .put(getAvailableStatPoints())
            .put(getAvailableClassPoints())
            .put(getStatus());
    }

    public long getCurrentManaPoints() {
        return currentManaPoints;
    }

    public void setCurrentManaPoints(long currentManaPoints) {
        this.currentManaPoints = currentManaPoints;
    }

    public long getCurrentEnergyPoints() {
        return currentEnergyPoints;
    }

    public void setCurrentEnergyPoints(long currentEnergyPoints) {
        this.currentEnergyPoints = currentEnergyPoints;
    }

    public CharacterData getCharacterData() {
        return characterData;
    }

    public List<CharacterClass> getClassList() {
        return classList;
    }

    public int getAvailableClassPoints() {
        int classPoints = classList.stream()
            .mapToInt(CharacterClass::getLevel)
            .sum();

        return CommonConstants.CLASS_POINTS_PER_LEVEL * characterData.getLevel() - classPoints;
    }

    public void processClassListChange(List<CharacterClass> classList) {
        for (CharacterClass characterClass: classList) {
            if (!this.classList.contains(characterClass)) {
                this.classList.add(characterClass);
            }
        }
    }
}
