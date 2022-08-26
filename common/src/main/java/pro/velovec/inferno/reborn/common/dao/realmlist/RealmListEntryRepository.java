package pro.velovec.inferno.reborn.common.dao.realmlist;

import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface RealmListEntryRepository extends CrudRepository<RealmListEntry, Integer> {
    List<RealmListEntry> findAllByOnline(Integer state);

    RealmListEntry findByName(String serverName);

    List<RealmListEntry> findAllByOnlineAndLastSeenBefore(Integer online, Date lastSeen);
}
