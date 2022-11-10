package pro.velovec.inferno.reborn.common.dao.character;

import org.springframework.data.repository.CrudRepository;

public interface CharacterDataRepository extends CrudRepository<CharacterData, Integer> {
    CharacterData findByCharacter(CharacterInfo characterInfo);
}
