package pro.velovec.inferno.reborn.worldd.dao.script;

import org.springframework.data.repository.CrudRepository;

public interface CommandRepository extends CrudRepository<Command, Integer> {
    Command findByName(String command);
}
