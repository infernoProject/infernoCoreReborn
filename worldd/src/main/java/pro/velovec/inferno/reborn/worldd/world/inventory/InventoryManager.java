package pro.velovec.inferno.reborn.worldd.world.inventory;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.velovec.inferno.reborn.worldd.dao.inventory.CharacterInventoryItem;
import pro.velovec.inferno.reborn.worldd.dao.inventory.CharacterInventoryItemRepository;

import java.sql.SQLException;
import java.util.List;

@Component
public class InventoryManager {

    @Autowired
    private CharacterInventoryItemRepository itemRepository;


    public List<CharacterInventoryItem> getCharacterInventory(int characterId) throws SQLException {
        return itemRepository.findAllByCharacterId(characterId);
    }
}
