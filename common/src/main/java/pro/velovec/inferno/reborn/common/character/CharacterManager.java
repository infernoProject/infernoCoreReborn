package pro.velovec.inferno.reborn.common.character;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.velovec.inferno.reborn.common.dao.auth.Account;
import pro.velovec.inferno.reborn.common.dao.character.CharacterInfo;
import pro.velovec.inferno.reborn.common.dao.character.CharacterInfoRepository;
import pro.velovec.inferno.reborn.common.dao.realmlist.RealmListEntry;
import pro.velovec.inferno.reborn.common.properties.InfernoCommonProperties;
import pro.velovec.libs.base.utils.TimeUtils;


import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class CharacterManager {

    @Autowired
    private InfernoCommonProperties commonProperties;
    @Autowired
    private CharacterInfoRepository characterInfoRepository;

    private static final Logger logger = LoggerFactory.getLogger(CharacterManager.class);

    public List<CharacterInfo> list(Account account) throws SQLException {
        return characterInfoRepository.findAllByAccountAndDeleteFlag(account, 0);
    }

    public List<CharacterInfo> list_deleted(Account account) throws SQLException {
        return characterInfoRepository.findAllByAccountAndDeleteFlag(account, 1);
    }

    public int create(CharacterInfo characterInfo) throws SQLException {
        if (exists(characterInfo))
            return -1;

        return characterInfoRepository.save(characterInfo).getId();
    }

    public boolean delete(CharacterInfo characterInfo) throws SQLException {
        if (exists(characterInfo)) {
            characterInfo.setDeleteFlag(1);
            characterInfo.setDeleteAfter(TimeUtils.add(
                new Date(), Calendar.DAY_OF_MONTH, commonProperties.getCharacters().getDeleteAfter()
            ));

            characterInfoRepository.save(characterInfo);

            return true;
        }

        return false;
    }

    public boolean restore(CharacterInfo characterInfo) throws SQLException {
        if (!exists(characterInfo)) {
            characterInfo.setDeleteFlag(0);
            characterInfo.setDeleteAfter(null);

            characterInfoRepository.save(characterInfo);

            return true;
        }

        return false;
    }

    public boolean exists(CharacterInfo characterInfo) throws SQLException {
        return characterInfoRepository.findByRealmAndFirstNameAndLastNameAndDeleteFlag(
            characterInfo.getRealm(), characterInfo.getFirstName(), characterInfo.getLastName(), 0
        ) != null;
    }

    public CharacterInfo get(RealmListEntry realm, String firstName, String lastName) throws SQLException {
        return characterInfoRepository.findByRealmAndFirstNameAndLastNameAndDeleteFlag(
            realm, firstName, lastName, 0
        );
    }

    public CharacterInfo get(int characterId) throws SQLException {
        return characterInfoRepository.findById(characterId).orElse(null);
    }

    public void cleanup() throws SQLException {
        characterInfoRepository.deleteAll(
            characterInfoRepository.findAllByDeleteFlagAndDeleteAfterBefore(1, new Date())
        );
    }

    public void update(CharacterInfo characterInfo) throws SQLException {
        characterInfoRepository.save(characterInfo);
    }
}
