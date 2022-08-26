package pro.velovec.inferno.reborn.realmd.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import pro.velovec.inferno.reborn.common.server.ListenerProperties;

@ConfigurationProperties(prefix = "inferno.realmd")
public class RealmServerProperties {

    private ListenerProperties listen = new ListenerProperties();

    public ListenerProperties getListen() {
        return listen;
    }

    public void setListen(ListenerProperties listen) {
        this.listen = listen;
    }
}
