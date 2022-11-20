package pro.velovec.inferno.reborn.worldd.map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.velovec.inferno.reborn.common.utils.ByteWrapper;
import pro.velovec.inferno.reborn.common.dao.map.Location;
import pro.velovec.inferno.reborn.common.dao.map.LocationRepository;
import pro.velovec.inferno.reborn.worldd.world.movement.WorldPosition;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class WorldMapManager {

    @Autowired
    private LocationRepository locationRepository;

    private final Map<Integer, WorldMap> maps = new HashMap<>();

    public void update(Long diff) {
        maps.values().parallelStream()
            .forEach(map -> map.update(diff));
    }

    public WorldMap getMap(WorldPosition position) {
        return getMapById(position.getLocation());
    }

    public List<WorldMap> getMaps() {
        return new ArrayList<>(maps.values());
    }

    public void readMapData(File mapDataPath) throws SQLException {
        if (!mapDataPath.exists())
            throw new IllegalStateException("MapData folder doesn't exist");

        locationRepository.findAll().forEach(location -> {
                try {
                    readMapData(mapDataPath, location);
                } catch (IOException e) {
                    throw new IllegalStateException(String.format(
                        "Unable to read MapData file '%s': %s", location.getName(), e.getMessage()
                    ));
                }
            });
    }

    private void readMapData(File mapDataPath, Location location) throws IOException {
        File mapDataFile = new File(mapDataPath, String.format("%s.map", location.getName()));
        if (mapDataFile.exists()) {
            ByteWrapper mapData = ByteWrapper.readFile(mapDataFile);
            WorldMap worldMap = new WorldMap(location, mapData);

            maps.put(location.getId(), worldMap);
        } else {
            throw new IllegalStateException(String.format("MapData file for '%s' doesn't exist", location.getName()));
        }
    }

    public WorldMap getMapById(int locationId) {
        WorldMap worldMap = maps.get(locationId);
        if (worldMap == null)
            throw new IllegalStateException(String.format(
                "MapData for location ID%d doesn't exist", locationId
            ));

        return worldMap;
    }
}
