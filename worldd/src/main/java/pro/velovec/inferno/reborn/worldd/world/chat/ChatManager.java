package pro.velovec.inferno.reborn.worldd.world.chat;

import org.springframework.stereotype.Component;
import pro.velovec.inferno.reborn.common.utils.ByteArray;
import pro.velovec.inferno.reborn.worldd.constants.WorldEventType;
import pro.velovec.inferno.reborn.worldd.map.WorldCell;
import pro.velovec.inferno.reborn.worldd.map.WorldMap;
import pro.velovec.inferno.reborn.worldd.map.WorldMapManager;
import pro.velovec.inferno.reborn.worldd.world.object.WorldObject;
import pro.velovec.inferno.reborn.worldd.world.player.WorldPlayer;

@Component
public class ChatManager {

    private final WorldMapManager worldMapManager;

    public ChatManager(WorldMapManager worldMapManager) {
        this.worldMapManager = worldMapManager;
    }

    public void sendLocalMessage(WorldPlayer sender, String message) {
        WorldMap map = worldMapManager.getMap(sender.getPosition());
        WorldCell cell = map.getCellByPosition(sender.getPosition());

        cell.onEvent(sender, WorldEventType.CHAT_MESSAGE, new ByteArray()
            .put(ChatMessageType.LOCAL)
            .put(sender.getOID())
            .put(sender.getName())
            .put(message)
        );
    }

    public void sendBroadcastMessage(WorldPlayer sender, String message) {
        WorldMap map = worldMapManager.getMap(sender.getPosition());

        map.onEvent(sender, WorldEventType.CHAT_MESSAGE, new ByteArray()
            .put(ChatMessageType.BROADCAST)
            .put(sender.getOID())
            .put(sender.getName())
            .put(message)
        );
    }

    public void sendPrivateMessage(WorldPlayer sender, WorldPlayer target, String message) {
        WorldMap map = worldMapManager.getMap(target.getPosition());
        WorldCell cell = map.getCellByPosition(target.getPosition());

        ByteArray chatMessage = new ByteArray()
            .put(ChatMessageType.PRIVATE)
            .put(sender.getOID())
            .put(sender.getName())
            .put(message);

        target.onEvent(cell, WorldEventType.CHAT_MESSAGE, new ByteArray()
            .put(sender.getAttributes())
            .put(chatMessage)
        );
    }

    public void sendAnnounce(String message) {
        WorldObject sender = WorldObject.WORLD;

        worldMapManager.getMaps()
            .forEach(map -> map.onEvent(sender, WorldEventType.CHAT_MESSAGE, new ByteArray()
                .put(ChatMessageType.ANNOUNCE)
                .put(sender.getOID())
                .put(sender.getName())
                .put(message)
            ));

    }

    public void sendGuildMessage(WorldPlayer sender, WorldPlayer target, String message) {
        WorldMap map = worldMapManager.getMap(target.getPosition());
        WorldCell cell = map.getCellByPosition(target.getPosition());

        ByteArray chatMessage = new ByteArray()
            .put(ChatMessageType.GUILD)
            .put(sender.getOID())
            .put(sender.getName())
            .put(message);

        target.onEvent(cell, WorldEventType.CHAT_MESSAGE, new ByteArray()
            .put(sender.getAttributes())
            .put(chatMessage)
        );
    }
}
