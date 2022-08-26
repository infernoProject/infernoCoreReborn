package pro.velovec.inferno.reborn.worldd.dao.items;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ItemRepository extends CrudRepository<Item, Integer> {
    List<Item> findByName(String name);
}
