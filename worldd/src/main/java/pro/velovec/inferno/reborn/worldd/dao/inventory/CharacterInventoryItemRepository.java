package pro.velovec.inferno.reborn.worldd.dao.inventory;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CharacterInventoryItemRepository extends CrudRepository<CharacterInventoryItem, Integer> {
    List<CharacterInventoryItem> findAllByCharacterId(int characterId);
}
