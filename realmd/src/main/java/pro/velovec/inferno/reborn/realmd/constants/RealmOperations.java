package pro.velovec.inferno.reborn.realmd.constants;

public class RealmOperations {

    private static final short SYSTEM_BASE  = 0x0000;
    public static final short CRYPTO_CONFIG = SYSTEM_BASE + 0x00;
    public static final short SIGN_UP       = SYSTEM_BASE + 0x01;
    public static final short LOG_IN_STEP1  = SYSTEM_BASE + 0x02;
    public static final short LOG_IN_STEP2  = SYSTEM_BASE + 0x03;
    public static final short REALM_LIST    = SYSTEM_BASE + 0x04;
    public static final short SESSION_TOKEN = SYSTEM_BASE + 0x05;

    private static final short CHARACTER_BASE           = 0x0100;
    public static final short CHARACTER_LIST            = CHARACTER_BASE + 0x00;
    public static final short CHARACTER_CREATE          = CHARACTER_BASE + 0x01;
    public static final short CHARACTER_DELETE          = CHARACTER_BASE + 0x02;
    public static final short CHARACTER_SELECT          = CHARACTER_BASE + 0x03;
    public static final short CHARACTER_RESTORABLE_LIST = CHARACTER_BASE + 0x04;
    public static final short CHARACTER_RESTORE         = CHARACTER_BASE + 0x05;
    public static final short CHARACTER_RENAME          = CHARACTER_BASE + 0x06;

    private static final short DATA_BASE = 0x0200;
    public static final short RACE_LIST  = DATA_BASE + 0x00;

    private RealmOperations() {
        // Prevent class instantiation
    }
}
