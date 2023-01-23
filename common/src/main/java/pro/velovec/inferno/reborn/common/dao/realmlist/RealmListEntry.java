package pro.velovec.inferno.reborn.common.dao.realmlist;

import pro.velovec.inferno.reborn.common.utils.ByteArray;
import pro.velovec.inferno.reborn.common.utils.ByteConvertible;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "realm_list_entries") // realmd
public class RealmListEntry implements ByteConvertible {

    @Id
    @GeneratedValue
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private int type;

    @Column(name = "server_host")
    private String serverHost;

    @Column(name = "server_port")
    private int serverPort;

    @Column(name = "online")
    private int online;

    @Column(name = "last_seen")
    private Date lastSeen;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public int getOnline() {
        return online;
    }

    public void setOnline(int online) {
        this.online = online;
    }

    public Date getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }

    @Override
    public byte[] toByteArray() {
        return new ByteArray()
            .put(id).put(name)
            .put(type).put(serverHost).put(serverPort)
            .toByteArray();
    }

    @Override
    public String toString() {
        return String.format(
            "RealmListEntry(name='%s', host=%s:%d, type=%d)",
            name, serverHost, serverPort, type
        );
    }
}
