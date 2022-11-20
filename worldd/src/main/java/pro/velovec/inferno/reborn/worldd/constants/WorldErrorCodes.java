package pro.velovec.inferno.reborn.worldd.constants;

public class WorldErrorCodes {

    public static final short NOT_EXISTS = 0x01;
    public static final short OUT_OF_RANGE = 0x02;
    public static final short COOLDOWN = 0x03;
    public static final short INSUFFICIENT_RESOURCES = 0x04;

    public static final short INVALID_SCRIPT = 0x7A;
    public static final short INVALID_REQUEST = 0x7B;
    public static final short ILLEGAL_MOVE = 0x7C;

    private WorldErrorCodes() {
        // Prevent class instantiation
    }
}
