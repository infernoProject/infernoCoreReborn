package pro.velovec.inferno.reborn.worldd.world.invite;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import pro.velovec.inferno.reborn.common.utils.ByteArray;
import pro.velovec.inferno.reborn.common.utils.ByteConvertible;
import pro.velovec.inferno.reborn.common.utils.ByteWrapper;
import pro.velovec.inferno.reborn.worldd.constants.WorldEventType;
import pro.velovec.inferno.reborn.worldd.dao.guild.Guild;
import pro.velovec.inferno.reborn.worldd.map.WorldCell;
import pro.velovec.inferno.reborn.worldd.map.WorldMap;
import pro.velovec.inferno.reborn.worldd.map.WorldMapManager;
import pro.velovec.inferno.reborn.worldd.world.guild.GuildManager;
import pro.velovec.inferno.reborn.worldd.world.player.WorldPlayer;

import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InviteManager {

    private volatile long inviteId;
    private final Map<Long, Invite> inviteMap;

    @Autowired
    private WorldMapManager worldMapManager;
    @Autowired
    private GuildManager guildManager;

    public InviteManager() {
        this.inviteId = 1L;
        this.inviteMap = new ConcurrentHashMap<>();
    }

    public synchronized void sendInvite(InviteType type, WorldPlayer sender, WorldPlayer target, ByteConvertible data) {
        WorldMap map = worldMapManager.getMap(target.getPosition());
        WorldCell cell = map.getCellByPosition(target.getPosition());

        Invite invite = new Invite(inviteId, type, sender, data);

        inviteMap.put(inviteId, invite);
        inviteId++;

        target.onEvent(cell, WorldEventType.INVITE, new ByteArray()
            .put(sender.getAttributes())
            .put(invite)
        );
    }

    public boolean respondToInvite(long id, boolean accepted, WorldPlayer respondent) throws SQLException {
        Invite invite = inviteMap.get(id);

        if (Objects.isNull(invite))
            return false;

        if (accepted) {
            switch (invite.getType()) {
                case GUILD -> acceptGuildInvite(invite, respondent);
            }
        }

        WorldPlayer sender = invite.getSender();
        WorldMap map = worldMapManager.getMap(sender.getPosition());
        WorldCell cell = map.getCellByPosition(sender.getPosition());

        sender.onEvent(cell, WorldEventType.INVITE_RESPONSE, new ByteArray()
            .put(respondent.getAttributes())
            .put(new ByteArray().put(accepted))
        );

        inviteMap.remove(id);
        return true;
    }

    private void acceptGuildInvite(Invite invite, WorldPlayer respondent) throws SQLException {
        ByteWrapper guildInfo = invite.getData();
        int guildId = guildInfo.getInt();
        Guild guild = guildManager.getGuild(guildId);

        guildManager.addGuildMember(guild, respondent.getCharacterInfo(), -1);
    }
}
