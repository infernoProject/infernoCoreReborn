package pro.velovec.inferno.reborn.worldd.constants;

public class WorldEventType {
    public static final short ENTER = 0x01;
    public static final short LEAVE = 0x02;

    public static final short SUBSCRIBE = 0x03;

    public static final short MOVE = 0x04;

    public static final short ATTRIBUTE_CHANGE = 0x05;

//    public static final short STATUS_CHANGE = 0x05;
//    public static final short HP_CHANGE = 0x06;

    public static final short CHAT_MESSAGE = 0x07;

    public static final short INVITE = 0x08;
    public static final short INVITE_RESPONSE = 0x09;

    public static final short TIME_CHANGE = 0x0A;
    public static final short WEATHER_CHANGE = 0x0B;

    public static final short EFFECT_ADD = 0x0C;
    public static final short EFFECT_UPDATE = 0x0D;
    public static final short EFFECT_REMOVE = 0x0E;

    public static final short DOT_ADD = 0x0F;
    public static final short DOT_UPDATE = 0x10;
    public static final short DOT_REMOVE = 0x11;

    private WorldEventType() {
        // Prevent class instantiation
    }
}
