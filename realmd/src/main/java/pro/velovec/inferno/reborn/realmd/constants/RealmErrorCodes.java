package pro.velovec.inferno.reborn.realmd.constants;

public class RealmErrorCodes {

    public static final short ALREADY_EXISTS = 0x01;
    public static final short CHARACTER_EXISTS = 0x01;

    public static final short AUTH_INVALID = 0x02;
    public static final short CHARACTER_NOT_FOUND = 0x02;

    public static final short USER_BANNED = 0x03;
    public static final short CHARACTER_DELETED = 0x03;

    private RealmErrorCodes() {
        // Prevent class instantiation
    }
}
