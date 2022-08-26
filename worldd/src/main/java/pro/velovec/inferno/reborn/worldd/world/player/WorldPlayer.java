package pro.velovec.inferno.reborn.worldd.world.player;

import pro.velovec.inferno.reborn.common.dao.character.CharacterInfo;
import pro.velovec.inferno.reborn.common.utils.ByteArray;
import pro.velovec.inferno.reborn.worldd.world.WorldNotificationListener;
import pro.velovec.inferno.reborn.worldd.world.creature.WorldCreature;
import pro.velovec.inferno.reborn.worldd.world.movement.WorldPosition;
import pro.velovec.inferno.reborn.worldd.world.object.WorldObjectType;

public class WorldPlayer extends WorldCreature {

    private final CharacterInfo characterInfo;

    public WorldPlayer(WorldNotificationListener notificationListener, CharacterInfo characterInfo) {
        super(notificationListener, String.format(
            "%s %s", characterInfo.getFirstName(), characterInfo.getLastName()
        ));

        setType(WorldObjectType.PLAYER);

        this.characterInfo = characterInfo;

        setPosition(new WorldPosition(
            characterInfo.getLocation(),
            characterInfo.getPositionX(),
            characterInfo.getPositionY(),
            characterInfo.getPositionZ(),
            characterInfo.getOrientation()
        ));

        setLevel(characterInfo.getLevel());
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
            .put(getMaxHitPoints())
            .put(getCurrentHitPoints())
            .put(getStatus());
    }
}
