package pro.velovec.inferno.reborn.worldd.dao.guild;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GuildMemberRepository extends CrudRepository<GuildMember, Integer> {
    List<GuildMember> findAllByGuildId(int id);

    GuildMember findByCharacterId(int id);

    GuildMember findByGuildIdAndLevel(int id, int level);
}
