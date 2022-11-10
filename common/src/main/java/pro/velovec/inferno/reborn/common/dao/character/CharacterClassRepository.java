package pro.velovec.inferno.reborn.common.dao.character;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CharacterClassRepository extends CrudRepository<CharacterClass, Integer> {
    List<CharacterClass> findAllByCharacter(CharacterInfo characterInfo);
}
