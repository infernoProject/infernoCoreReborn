package pro.velovec.inferno.reborn.worldd.dao.guild;

import org.springframework.data.repository.CrudRepository;

public interface GuildRepository extends CrudRepository<Guild, Integer> {

    Guild findByRealmAndTitle(int realm, String title);

    Guild findByRealmAndTitleOrTag(int id, String title, String tag);
}
