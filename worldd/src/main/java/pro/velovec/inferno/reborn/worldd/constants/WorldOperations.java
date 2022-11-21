package pro.velovec.inferno.reborn.worldd.constants;

public class WorldOperations {

    // SYSTEM
    private static final short SYSTEM_BASE   = 0x0100;
    public static final short AUTHORIZE      = SYSTEM_BASE + 0x00;
    public static final short EXECUTE        = SYSTEM_BASE + 0x01;
    public static final short CHAT_MESSAGE   = SYSTEM_BASE + 0x02;
    public static final short SPELL_CAST     = SYSTEM_BASE + 0x03;
    public static final short TERRAIN_CHECK  = SYSTEM_BASE + 0x60;
    public static final short TERRAIN_LOAD   = SYSTEM_BASE + 0x61;
    public static final short INVITE_RESPOND = SYSTEM_BASE + 0xFC;
    public static final short EVENT          = SYSTEM_BASE + 0xFD;
    public static final short LOG_OUT        = SYSTEM_BASE + 0xFE;
    public static final short HEART_BEAT     = SYSTEM_BASE + 0xFF;

    // MOVEMENT
    private static final short MOVE_BASE               = 0x0200;
    public static final short MOVE_START_FORWARD       = MOVE_BASE + 0x00;
    public static final short MOVE_START_BACKWARD      = MOVE_BASE + 0x01;
    public static final short MOVE_START_STRAFE_LEFT   = MOVE_BASE + 0x02;
    public static final short MOVE_START_STRAFE_RIGHT  = MOVE_BASE + 0x03;
    public static final short MOVE_START_TURN_LEFT     = MOVE_BASE + 0x04;
    public static final short MOVE_START_TURN_RIGHT    = MOVE_BASE + 0x05;
    public static final short MOVE_START_PITCH_UP      = MOVE_BASE + 0x06;
    public static final short MOVE_START_PITCH_DOWN    = MOVE_BASE + 0x07;
    public static final short MOVE_START_SWIM          = MOVE_BASE + 0x08;
    public static final short MOVE_JUMP                = MOVE_BASE + 0x10;
    public static final short MOVE_HEARTBEAT           = MOVE_BASE + 0x11;
    public static final short MOVE_FALL                = MOVE_BASE + 0x12;
    public static final short MOVE_STOP                = MOVE_BASE + 0x20;
    public static final short MOVE_STOP_STRAFE         = MOVE_BASE + 0x21;
    public static final short MOVE_STOP_TURN           = MOVE_BASE + 0x22;
    public static final short MOVE_STOP_PITCH          = MOVE_BASE + 0x23;
    public static final short MOVE_STOP_SWIM           = MOVE_BASE + 0x24;
    public static final short MOVE_SET_RUN_MODE        = MOVE_BASE + 0x30;
    public static final short MOVE_SET_WALK_MODE       = MOVE_BASE + 0x31;

    // CHARACTER INFO
    private static final short CHARACTER_BASE  = 0x0300;
    public static final short STATS_ADD        = CHARACTER_BASE + 0x00;
    public static final short CLASS_ADD        = CHARACTER_BASE + 0x10;
    public static final short CLASS_LIST       = CHARACTER_BASE + 0x11;
    public static final short INVENTORY_LIST   = CHARACTER_BASE + 0x20;
    public static final short INVENTORY_MOVE   = CHARACTER_BASE + 0x21;
    public static final short INVENTORY_ADD    = CHARACTER_BASE + 0x22;
    public static final short INVENTORY_REMOVE = CHARACTER_BASE + 0x23;
    public static final short SPELL_LIST       = CHARACTER_BASE + 0x30;

    // SOCIAL
    private static final short SOCIAL_BASE  = 0x0400;
    public static final short PARTY_CREATE  = SOCIAL_BASE + 0x00;
    public static final short PARTY_INVITE  = SOCIAL_BASE + 0x01;
    public static final short PARTY_LEAVE   = SOCIAL_BASE + 0x02;
    public static final short GUILD_CREATE  = SOCIAL_BASE + 0x10;
    public static final short GUILD_INVITE  = SOCIAL_BASE + 0x11;
    public static final short GUILD_LEAVE   = SOCIAL_BASE + 0x12;
    public static final short GUILD_PROMOTE = SOCIAL_BASE + 0x13;
    public static final short GUILD_INFO    = SOCIAL_BASE + 0x14;

    // ADMIN FUNCTIONS
    private static final short ADMIN_BASE          = 0x7F00;
    public static final short SCRIPT_LANGUAGE_LIST = ADMIN_BASE + 0x00;
    public static final short SCRIPT_LIST          = ADMIN_BASE + 0x10;
    public static final short SCRIPT_GET           = ADMIN_BASE + 0x11;
    public static final short SCRIPT_VALIDATE      = ADMIN_BASE + 0x12;
    public static final short SCRIPT_SAVE          = ADMIN_BASE + 0x13;

    private WorldOperations() {
        // Prevent class instantiation
    }
}
