package pro.velovec.inferno.reborn.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "inferno.common")
public class InfernoCommonProperties {

    private CryptoProperties crypto = new CryptoProperties();
    private SessionProperties session = new SessionProperties();

    private CharacterProperties characters = new CharacterProperties();

    public CryptoProperties getCrypto() {
        return crypto;
    }

    public void setCrypto(CryptoProperties crypto) {
        this.crypto = crypto;
    }

    public SessionProperties getSession() {
        return session;
    }

    public void setSession(SessionProperties session) {
        this.session = session;
    }

    public CharacterProperties getCharacters() {
        return characters;
    }

    public void setCharacters(CharacterProperties characters) {
        this.characters = characters;
    }

    public static class CryptoProperties {

        private String salt;

        public String getSalt() {
            return salt;
        }

        public void setSalt(String salt) {
            this.salt = salt;
        }
    }

    public static class SessionProperties {

        private Integer ttl;

        public Integer getTtl() {
            return ttl;
        }

        public void setTtl(Integer ttl) {
            this.ttl = ttl;
        }
    }

    public static class CharacterProperties {

        private Integer deleteAfter;

        public Integer getDeleteAfter() {
            return deleteAfter;
        }

        public void setDeleteAfter(Integer deleteAfter) {
            this.deleteAfter = deleteAfter;
        }
    }
}
