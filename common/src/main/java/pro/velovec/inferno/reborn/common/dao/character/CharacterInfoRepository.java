package pro.velovec.inferno.reborn.common.dao.character;

import org.springframework.data.repository.CrudRepository;
import pro.velovec.inferno.reborn.common.dao.auth.Account;
import pro.velovec.inferno.reborn.common.dao.realmlist.RealmListEntry;

import java.util.Date;
import java.util.List;

public interface CharacterInfoRepository extends CrudRepository<CharacterInfo, Integer> {

    List<CharacterInfo> findAllByAccountAndDeleteFlag(Account account, int deleteFlag);

    CharacterInfo findByRealmAndFirstNameAndLastNameAndDeleteFlag(RealmListEntry realm, String firstName, String lastName, int deleteFlag);

    List<CharacterInfo> findAllByDeleteFlagAndDeleteAfterBefore(int deleteFlag, Date deleteAfter);
}
