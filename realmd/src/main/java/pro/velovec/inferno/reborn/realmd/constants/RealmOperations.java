package pro.velovec.inferno.reborn.realmd.constants;

public class RealmOperations {

    public static final short CRYPTO_CONFIG = 0x00;

    public static final short SIGN_UP = 0x01;
    public static final short LOG_IN_STEP1 = 0x02;
    public static final short LOG_IN_STEP2 = 0x03;

    public static final short REALM_LIST = 0x04;

    public static final short SESSION_TOKEN = 0x05;

    public static final short CHARACTER_LIST = 0x06;
    public static final short CHARACTER_CREATE = 0x07;
    public static final short CHARACTER_DELETE = 0x08;
    public static final short CHARACTER_SELECT = 0x09;

    public static final short CHARACTER_RESTORABLE_LIST = 0x0A;
    public static final short CHARACTER_RESTORE = 0x0B;
    public static final short CHARACTER_RENAME = 0x0C;

    public static final short RACE_LIST = 0x0D;

    private RealmOperations() {
        // Prevent class instantiation
    }
}
