package pro.velovec.inferno.reborn.common.realmlist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.velovec.inferno.reborn.common.dao.realmlist.RealmListEntry;
import pro.velovec.inferno.reborn.common.dao.realmlist.RealmListEntryRepository;


import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@Component
public class RealmListManager {

    @Autowired
    private RealmListEntryRepository realmListEntryRepository;

    private static final Logger logger = LoggerFactory.getLogger(RealmListManager.class);

    public List<RealmListEntry> list() throws SQLException {
        return realmListEntryRepository.findAllByOnline(1);
    }

    public RealmListEntry get(String serverName) throws SQLException {
        return realmListEntryRepository.findByName(serverName);
    }

    public RealmListEntry get(int serverId) throws SQLException {
        return realmListEntryRepository.findById(serverId).orElse(null);
    }

    public void online(String serverName, boolean onLine) throws SQLException {
        RealmListEntry entry = get(serverName);

        if (onLine) {
            entry.setOnline(1);
            entry.setLastSeen(new Date());
        } else {
            entry.setOnline(0);
        }

        realmListEntryRepository.save(entry);
    }

    public void check() throws SQLException {
        Date expiration = new Date(System.currentTimeMillis() - 30000);

        realmListEntryRepository.findAllByOnlineAndLastSeenBefore(1, expiration).forEach(
            realmListEntry -> {
                realmListEntry.setOnline(0);
                logger.info("Server '{}' has gone off-line", realmListEntry.getName());
                realmListEntryRepository.save(realmListEntry);
            }
        );
    }

    public boolean exists(String serverName) throws SQLException {
        return get(serverName) != null;
    }
}
