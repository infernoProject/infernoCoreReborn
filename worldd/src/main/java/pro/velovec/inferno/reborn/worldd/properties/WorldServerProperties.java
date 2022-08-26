package pro.velovec.inferno.reborn.worldd.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import pro.velovec.inferno.reborn.common.server.ListenerProperties;

@ConfigurationProperties(prefix = "inferno.worldd")
public class WorldServerProperties {
    private ListenerProperties listen = new ListenerProperties();
    private MapProperties map = new MapProperties();

    private String name;

    public ListenerProperties getListen() {
        return listen;
    }

    public void setListen(ListenerProperties listen) {
        this.listen = listen;
    }

    public MapProperties getMap() {
        return map;
    }

    public void setMap(MapProperties map) {
        this.map = map;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public class MapProperties {

        private String dataPath;

        public String getDataPath() {
            return dataPath;
        }

        public void setDataPath(String dataPath) {
            this.dataPath = dataPath;
        }
    }
}
