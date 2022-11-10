package pro.velovec.inferno.reborn.worldd.constants;

public class WorldOperations {
    public static final byte AUTHORIZE = 0x00;
    public static final byte EXECUTE = 0x01;

    public static final byte STATS_ADD = 0x20;

    public static final byte CLASS_LIST = 0x21;
    public static final byte CLASS_ADD = 0x22;

    public static final byte MOVE = 0x30;

    public static final byte INVENTORY_LIST = 0x31;
    public static final byte INVENTORY_MOVE = 0x32;
    public static final byte INVENTORY_ADD = 0x33;
    public static final byte INVENTORY_REMOVE = 0x34;

    public static final byte SPELL_LIST = 0x40;
    public static final byte SPELL_CAST = 0x41;

    public static final byte CHAT_MESSAGE = 0x50;

    public static final byte GUILD_CREATE = 0x70;
    public static final byte GUILD_INVITE = 0x71;
    public static final byte GUILD_LEAVE = 0x72;
    public static final byte GUILD_PROMOTE = 0x73;
    public static final byte GUILD_INFO = 0x74;

    public static final byte INVITE_RESPOND = 0x77;

    public static final byte SCRIPT_LANGUAGE_LIST = 0x78;
    public static final byte SCRIPT_LIST = 0x79;
    public static final byte SCRIPT_GET = 0x7A;
    public static final byte SCRIPT_VALIDATE = 0x7B;
    public static final byte SCRIPT_SAVE = 0x7C;

    public static final byte EVENT = 0x7D;
    public static final byte LOG_OUT = 0x7E;
    public static final byte HEART_BEAT = 0x7F;

    private WorldOperations() {
        // Prevent class instantiation
    }
}
