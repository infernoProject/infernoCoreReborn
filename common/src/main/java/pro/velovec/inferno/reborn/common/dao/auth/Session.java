package pro.velovec.inferno.reborn.common.dao.auth;

import pro.velovec.inferno.reborn.common.dao.character.CharacterInfo;
import pro.velovec.libs.base.utils.HexBin;

import jakarta.persistence.*;
import java.net.SocketAddress;
import java.util.Date;
import java.util.Random;

@Entity
@Table(name = "sessions") // realmd
public class Session {

    @Id
    @GeneratedValue
    private int id;

    @OneToOne(fetch = FetchType.EAGER)
    private Account account;

    @Column(name = "session_key")
    private byte[] sessionKey;

    @Column(name = "session_address")
    private String address = null;

    @Column(name = "last_activity")
    private Date lastActivity;

    @Column(name = "vector")
    private byte[] vector;

    @OneToOne(fetch = FetchType.EAGER)
    private CharacterInfo characterInfo;

    public Session() {
        // Default constructor for SQLObjectWrapper
    }

    public Session(Account account, byte[] sessionKey, SocketAddress remoteAddress) {
        this.account = account;
        this.sessionKey = sessionKey;
        this.address = remoteAddress.toString();

        this.vector = generateVector();
    }

    private byte[] generateVector() {
        byte[] vector = new byte[32];

        new Random().nextBytes(vector);

        return vector;
    }

    public byte[] getKey() {
        return sessionKey;
    }

    public String getKeyHex() {
        return HexBin.encode(sessionKey);
    }

    public Account getAccount() {
        return account;
    }

    public byte[] getVector() {
        return vector;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(SocketAddress address) {
        this.address = address.toString();
    }

    public Date getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Date lastActivity) {
        this.lastActivity = lastActivity;
    }

    public CharacterInfo getCharacterInfo() {
        return characterInfo;
    }

    public void setCharacterInfo(CharacterInfo characterInfo) {
        this.characterInfo = characterInfo;
    }
}
