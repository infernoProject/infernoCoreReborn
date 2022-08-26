package pro.velovec.inferno.reborn.worldd.world.guild;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.velovec.inferno.reborn.common.dao.character.CharacterInfo;
import pro.velovec.inferno.reborn.worldd.dao.guild.Guild;
import pro.velovec.inferno.reborn.worldd.dao.guild.GuildMember;
import pro.velovec.inferno.reborn.worldd.dao.guild.GuildMemberRepository;
import pro.velovec.inferno.reborn.worldd.dao.guild.GuildRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Component
public class GuildManager {

    @Autowired
    private GuildRepository guildRepository;

    @Autowired
    private GuildMemberRepository guildMemberRepository;

    public Guild getPlayerGuild(int id) throws SQLException {
        GuildMember guildMember = guildMemberRepository.findByCharacterId(id);

        return Objects.nonNull(guildMember) ? guildMember.getGuild() : null;
    }

    public Guild getGuild(int id) throws SQLException {
        return guildRepository.findById(id).orElse(null);
    }

    public Guild getGuildByTitle(int realm, String title) throws SQLException {
        return guildRepository.findByRealmAndTitle(realm, title);
    }

    public List<GuildMember> getGuildPlayers(int id) throws SQLException {
        return guildMemberRepository.findAllByGuildId(id);
    }

    public CharacterInfo getGuildMaster(int id) throws SQLException {
        GuildMember guildMember = guildMemberRepository.findByGuildIdAndLevel(id, 1);

        return Objects.nonNull(guildMember) ? guildMember.getCharacter() : null;
    }

    public Guild createGuild(String title, String tag, String description, CharacterInfo owner) throws SQLException {
        Guild guildInfo = guildRepository.findByRealmAndTitleOrTag(owner.getRealm().getId(), title, tag);

        if (Objects.nonNull(guildInfo)) {
            return null;
        }

        guildInfo = new Guild();

        guildInfo.setTag(tag);
        guildInfo.setTitle(title);
        guildInfo.setDescription(description);
        guildInfo.setRealm(owner.getRealm().getId());

        guildInfo = guildRepository.save(guildInfo);
        addGuildMember(guildInfo, owner, 1);

        return guildInfo;
    }

    public void addGuildMember(Guild guild, CharacterInfo player, int level) throws SQLException {
        GuildMember guildMember = new GuildMember();

        guildMember.setGuild(guild);
        guildMember.setCharacter(player);
        guildMember.setLevel(level);

        guildMemberRepository.save(guildMember);
    }

    public void removeGuildMember(CharacterInfo player) throws SQLException {
        guildMemberRepository.delete(guildMemberRepository.findByCharacterId(player.getId()));
    }

    public void removeGuild(int id) throws SQLException {
        guildMemberRepository.deleteAll(guildMemberRepository.findAllByGuildId(id));
        guildRepository.delete(guildRepository.findById(id).get());
    }

    public int getPlayerLevel(Guild guild, CharacterInfo characterInfo) throws SQLException {
        GuildMember guildMember = guildMemberRepository.findByCharacterId(characterInfo.getId());

        return Objects.nonNull(guildMember) ? guildMember.getLevel() : 0;
    }

    public void setPlayerLevel(Guild guild, CharacterInfo player, int level) throws SQLException {
        GuildMember guildMember = guildMemberRepository.findByCharacterId(player.getId());

        guildMember.setLevel(level);

        guildMemberRepository.save(guildMember);
    }
}
