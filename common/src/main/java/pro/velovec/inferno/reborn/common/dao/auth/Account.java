package pro.velovec.inferno.reborn.common.dao.auth;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts") // realmd
public class Account {

    @Id
    @GeneratedValue
    private int id;

    private AccountLevel accessLevel;

    private String login;

    private String email;

    private byte[] salt;

    private byte[] verifier;

    public Account() {
        // Default constructor for SQLObjectWrapper
    }

    public Account(String login, AccountLevel accessLevel, String email, byte[] salt, byte[] verifier) {
        this.login = login;
        this.accessLevel = accessLevel;
        this.email = email;
        this.salt = salt;
        this.verifier = verifier;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public AccountLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(AccountLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public byte[] getVerifier() {
        return verifier;
    }

    public void setVerifier(byte[] verifier) {
        this.verifier = verifier;
    }
}
