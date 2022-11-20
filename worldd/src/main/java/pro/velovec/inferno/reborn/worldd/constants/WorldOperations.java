package pro.velovec.inferno.reborn.worldd.constants;

public class WorldOperations {
    public static final short AUTHORIZE = 0x00;
    public static final short EXECUTE = 0x01;

    public static final short STATS_ADD = 0x20;

    public static final short CLASS_LIST = 0x21;
    public static final short CLASS_ADD = 0x22;

    public static final short MOVE_START_FORWARD                          = 0x0B5;
    public static final short MOVE_START_BACKWARD                         = 0x0B6;
    public static final short MOVE_STOP                                   = 0x0B7;
    public static final short MOVE_START_STRAFE_LEFT                      = 0x0B8;
    public static final short MOVE_START_STRAFE_RIGHT                     = 0x0B9;
    public static final short MOVE_STOP_STRAFE                            = 0x0BA;
    public static final short MOVE_JUMP                                   = 0x0BB;
    public static final short MOVE_START_TURN_LEFT                        = 0x0BC;
    public static final short MOVE_START_TURN_RIGHT                       = 0x0BD;
    public static final short MOVE_STOP_TURN                              = 0x0BE;
    public static final short MOVE_START_PITCH_UP                         = 0x0BF;
    public static final short MOVE_START_PITCH_DOWN                       = 0x0C0;
    public static final short MOVE_STOP_PITCH                             = 0x0C1;
    public static final short MOVE_SET_RUN_MODE                           = 0x0C2;
    public static final short MOVE_SET_WALK_MODE                          = 0x0C3;

    public static final short MOVE = 0x30;

    public static final short INVENTORY_LIST = 0x31;
    public static final short INVENTORY_MOVE = 0x32;
    public static final short INVENTORY_ADD = 0x33;
    public static final short INVENTORY_REMOVE = 0x34;

    public static final short SPELL_LIST = 0x40;
    public static final short SPELL_CAST = 0x41;

    public static final short CHAT_MESSAGE = 0x50;

    public static final short GUILD_CREATE = 0x70;
    public static final short GUILD_INVITE = 0x71;
    public static final short GUILD_LEAVE = 0x72;
    public static final short GUILD_PROMOTE = 0x73;
    public static final short GUILD_INFO = 0x74;

    public static final short INVITE_RESPOND = 0x77;

    public static final short SCRIPT_LANGUAGE_LIST = 0x78;
    public static final short SCRIPT_LIST = 0x79;
    public static final short SCRIPT_GET = 0x7A;
    public static final short SCRIPT_VALIDATE = 0x7B;
    public static final short SCRIPT_SAVE = 0x7C;

    public static final short EVENT = 0x7D;
    public static final short LOG_OUT = 0x7E;
    public static final short HEART_BEAT = 0x7F;

    private WorldOperations() {
        // Prevent class instantiation
    }
}
