package pro.velovec.inferno.reborn.worldd.map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pro.velovec.inferno.reborn.common.oid.OID;
import pro.velovec.inferno.reborn.common.utils.ByteArray;
import pro.velovec.inferno.reborn.common.utils.ByteConvertible;
import pro.velovec.inferno.reborn.common.utils.ByteWrapper;
import pro.velovec.inferno.reborn.worldd.constants.WorldSize;
import pro.velovec.inferno.reborn.common.dao.map.Location;
import pro.velovec.inferno.reborn.worldd.utils.MathUtils;
import pro.velovec.inferno.reborn.worldd.world.movement.WorldPosition;
import pro.velovec.inferno.reborn.worldd.world.object.WorldObject;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class WorldMap implements ByteConvertible {

    private final Location location;
    private final WorldCell[][] cells = new WorldCell[WorldSize.CELL_TOTAL][WorldSize.CELL_TOTAL];

    private final List<WorldObstacle> obstacles = new ArrayList<>();
    private final float[][] heightMap;
    private final float waterLevel;

    private static final Logger logger = LoggerFactory.getLogger(WorldMap.class);

    public WorldMap(Location location, ByteWrapper mapData) {
        this.location = location;

        for (int x = 0; x < WorldSize.CELL_TOTAL; x++) {
            cells[x] = new WorldCell[WorldSize.CELL_TOTAL];

            for (int z = 0; z < WorldSize.CELL_TOTAL; z++) {
                cells[x][z] = new WorldCell(x, z);
            }
        }

        waterLevel = mapData.getFloat();
        heightMap = mapData.getFloatMatrix();

        if (heightMap.length != WorldSize.MAP_SIZE + 1 || heightMap[0].length != WorldSize.MAP_SIZE + 1) {
            throw new IllegalStateException(String.format(
                "MapData: heightmap size is invalid (%dx%d) expected: (%dx%d)",
                heightMap.length, heightMap[0].length,
                (int) WorldSize.MAP_SIZE + 1, (int) WorldSize.MAP_SIZE + 1
            ));
        }

        for (ByteWrapper obstacleData: mapData.getList()) {
            obstacles.add(new WorldObstacle(location, obstacleData));
        }
    }

    public WorldCell getCellByPosition(WorldPosition position) {
        return getCellByPosition(position.getX(), position.getY());
    }

    public WorldCell getCellByPosition(float positionX, float positionZ) {
        int x = Math.min((int) Math.floor(positionX / WorldSize.CELL_SIZE) + WorldSize.CENTER_CELL_ID, WorldSize.CELL_TOTAL - 1);
        int z = Math.min((int) Math.floor(positionZ / WorldSize.CELL_SIZE) + WorldSize.CENTER_CELL_ID, WorldSize.CELL_TOTAL - 1);

        return cells[x][z];
    }

    public WorldObject findObjectById(OID id) {
        return Arrays.asList(cells).parallelStream()
            .map(worldCells -> Arrays.asList(worldCells).parallelStream()
                .map(worldCell -> worldCell.findObjectById(id))
                .collect(Collectors.toList())
            )
            .flatMap(List::stream)
            .filter(Objects::nonNull)
            .distinct()
            .findFirst().orElse(null);
    }

    public List<WorldObject> findObjectsInArea(WorldPosition position, float radius) {
        return Arrays.asList(cells).parallelStream()
            .map(worldCells -> Arrays.asList(worldCells).parallelStream()
                .map(WorldCell::getSubscribers)
                .flatMap(List::stream)
                .collect(Collectors.toList())
            )
            .flatMap(List::stream)
            .distinct()
            .filter(worldObject -> MathUtils.calculateDistance(position, worldObject.getPosition()) <= radius)
            .collect(Collectors.toList());
    }

    public List<WorldCell> calculateInnerInterestArea(WorldPosition position) {
        WorldCell bottomLeft = getCellByPosition(
            Math.max(position.getX() - WorldSize.INNER_INTEREST_AREA_RADIUS, -WorldSize.MAP_HALFSIZE),
            Math.max(position.getZ() - WorldSize.INNER_INTEREST_AREA_RADIUS, -WorldSize.MAP_HALFSIZE)
        );
        WorldCell topRight = getCellByPosition(
            Math.min(position.getX() + WorldSize.INNER_INTEREST_AREA_RADIUS, WorldSize.MAP_HALFSIZE),
            Math.min(position.getZ() + WorldSize.INNER_INTEREST_AREA_RADIUS, WorldSize.MAP_HALFSIZE)
        );

        List<WorldCell> interestArea = new ArrayList<>();

        for (int x = bottomLeft.getX(); x <= topRight.getX(); x++) {
            for (int z = bottomLeft.getZ(); z <= topRight.getZ(); z++) {
                interestArea.add(cells[x][z]);
            }
        }

        return interestArea;
    }

    public List<WorldCell> calculateOuterInterestArea(WorldPosition position, List<WorldCell> innerInterestArea) {
        WorldCell bottomLeft = getCellByPosition(
            Math.max(position.getX() - WorldSize.OUTER_INTEREST_AREA_RADIUS, -WorldSize.MAP_HALFSIZE),
            Math.max(position.getZ() - WorldSize.OUTER_INTEREST_AREA_RADIUS, -WorldSize.MAP_HALFSIZE)
        );
        WorldCell topRight = getCellByPosition(
            Math.min(position.getX() + WorldSize.OUTER_INTEREST_AREA_RADIUS, WorldSize.MAP_HALFSIZE),
            Math.min(position.getZ() + WorldSize.OUTER_INTEREST_AREA_RADIUS, WorldSize.MAP_HALFSIZE)
        );

        List<WorldCell> interestArea = new ArrayList<>();

        for (int x = bottomLeft.getX(); x <= topRight.getX(); x++) {
            for (int z = bottomLeft.getZ(); z <= topRight.getZ(); z++) {
                if (!innerInterestArea.contains(cells[x][z])) {
                    interestArea.add(cells[x][z]);
                }
            }
        }

        return interestArea;
    }

    public Location getLocation() {
        return location;
    }


    public boolean isLegalMove(WorldPosition currentPosition, WorldPosition newPosition) {
        float distance = MathUtils.calculateDistance(currentPosition, newPosition);

        if (distance > WorldSize.MAX_SPEED)
            return false;

        return !obstacles.parallelStream()
            .map(obstacle -> obstacle.isPathInsideObstacle(currentPosition, newPosition))
            .filter(result -> result)
            .findAny().orElse(false);
    }

    public void update(long diff) {
        Arrays.asList(cells).parallelStream()
            .map(worldCells -> Arrays.asList(worldCells).parallelStream()
                .map(WorldCell::getSubscribers)
                .flatMap(List::stream)
                .collect(Collectors.toList())
            )
            .flatMap(List::stream)
            .forEach(worldObject -> worldObject.update(diff));
    }

    public void onEvent(WorldObject source, short eventType, ByteConvertible eventData) {
        Arrays.asList(cells).parallelStream()
            .map(Arrays::asList)
            .flatMap(List::stream)
            .forEach(worldCell -> worldCell.onEvent(source, eventType, eventData));
    }

    @Override
    public byte[] toByteArray() {
        return new ByteArray()
            .put(location.getId())
            .put(waterLevel)
            .put(heightMap)
            .toByteArray();
    }
}
