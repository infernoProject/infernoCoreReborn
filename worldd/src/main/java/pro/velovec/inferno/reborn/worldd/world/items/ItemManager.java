package pro.velovec.inferno.reborn.worldd.world.items;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.velovec.inferno.reborn.worldd.dao.items.Item;
import pro.velovec.inferno.reborn.worldd.dao.items.ItemRepository;

import java.sql.SQLException;
import java.util.List;

@Component
public class ItemManager {

    @Autowired
    private ItemRepository itemRepository;

    public List<Item> findItemsByName(String name) throws SQLException {
        return itemRepository.findByName(name);
    }

    public Item getItemById(int id) throws SQLException {
        return itemRepository.findById(id).orElse(null);
    }
}
