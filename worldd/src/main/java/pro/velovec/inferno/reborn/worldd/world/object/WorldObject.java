package pro.velovec.inferno.reborn.worldd.world.object;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pro.velovec.inferno.reborn.common.oid.OID;
import pro.velovec.inferno.reborn.common.oid.OIDGenerator;
import pro.velovec.inferno.reborn.common.utils.ByteArray;
import pro.velovec.inferno.reborn.common.utils.ByteConvertible;
import pro.velovec.inferno.reborn.common.utils.ByteWrapper;
import pro.velovec.inferno.reborn.worldd.constants.WorldEventType;
import pro.velovec.inferno.reborn.worldd.map.WorldCell;
import pro.velovec.inferno.reborn.worldd.map.WorldMap;
import pro.velovec.inferno.reborn.worldd.world.WorldNotificationListener;
import pro.velovec.inferno.reborn.worldd.world.interest.InterestArea;
import pro.velovec.inferno.reborn.worldd.world.movement.WorldPosition;


import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class WorldObject implements Comparable<WorldObject> {

    private final OID id;
    private String name;
    private WorldPosition position;
    private WorldObjectType type;

    private final InterestArea interestArea;
    protected WorldCell currentCell;

    private final Map<Integer, Long> cooldownMap = new ConcurrentHashMap<>();

    protected static final Logger logger = LoggerFactory.getLogger(WorldObject.class);

    public static final WorldObject WORLD = new WorldObject(null, "World");

    public WorldObject(WorldNotificationListener notificationListener, String name) {
        this.id = OIDGenerator.getOID();
        this.name = name;

        this.interestArea = new InterestArea(this, notificationListener);

        setType(WorldObjectType.OBJECT);
    }

    public void onEvent(WorldCell cell, short type, ByteConvertible data) {
        interestArea.onEvent(cell, type, ByteWrapper.fromBytes(data));
    }

    public OID getOID() {
        return id;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void updatePosition(WorldPosition position, WorldMap map) {
        WorldCell targetCell = map.getCellByPosition(position);

        List<WorldCell> innerInterestArea = map.calculateInnerInterestArea(position);
        List<WorldCell> outerInterestArea = map.calculateOuterInterestArea(position, innerInterestArea);

        this.interestArea.updateInterestArea(targetCell, innerInterestArea, outerInterestArea);

        if (targetCell != currentCell) {
            if (currentCell != null) {
                currentCell.onEvent(
                    this, WorldEventType.LEAVE,
                    new ByteArray().put(targetCell.getX()).put(targetCell.getZ())
                );

                logger.debug("{} is leaving {}", this, currentCell);
            }
            targetCell.onEvent(
                this, WorldEventType.ENTER,
                new ByteArray().put(targetCell.getX()).put(targetCell.getZ())
            );
            currentCell = targetCell;

            logger.debug("{} is entering {}", this, currentCell);
        }

        targetCell.onEvent(this, WorldEventType.MOVE, new ByteArray().put(position));

        setPosition(position);
    }

    protected void setPosition(WorldPosition position) {
        this.position = position;
    }

    public WorldPosition getPosition() {
        return position;
    }

    public void destroy() {
        if (currentCell != null) {
            currentCell.onEvent(
                this, WorldEventType.LEAVE,
                new ByteArray().put(-1).put(-1)
            );

            interestArea.destroy();

            currentCell = null;
        }
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object target) {
        return WorldObject.class.isAssignableFrom(target.getClass()) && id.compareTo(((WorldObject) target).id) == 0;
    }

    @Override
    public int compareTo(WorldObject target) {
        return id.compareTo(target.id);
    }

    @Override
    public String toString() {
        return String.format("WorldObject(id='%s', name='%s')", id, name);
    }

    public WorldObjectType getType() {
        return type;
    }

    protected void setType(WorldObjectType type) {
        this.type = type;
    }

    public ByteArray getAttributes() {
        ByteArray attributes = new ByteArray()
            .put(id).put(type.toString().toLowerCase()).put(name);

        if (Objects.nonNull(position)) {
            attributes
                .put(position.getX()).put(position.getY()).put(position.getZ())
                .put(position.getOrientation());
        } else {
            attributes.put(0f).put(0f).put(0f).put(0f);
        }

        return attributes;
    }

    public boolean hasCoolDown(int spellId) {
        return cooldownMap.containsKey(spellId);
    }

    public void addCoolDown(int spellId, long duration) {
        cooldownMap.put(spellId, duration);
    }

    public void update(long diff) {
        cooldownMap.entrySet().parallelStream()
            .peek(cooldown -> cooldown.setValue(cooldown.getValue() - diff))
            .filter(cooldown -> cooldown.getValue() <= 0)
            .forEach(cooldown -> cooldownMap.remove(cooldown.getKey()));
    }

    public long getCoolDown(int spellId) {
        return cooldownMap.getOrDefault(spellId, 0L);
    }
}
