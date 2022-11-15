package pro.velovec.inferno.reborn.worldd.dao.script;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CommandRepository extends CrudRepository<Command, Integer> {

    List<Command> findAll();

    Command findByName(String command);
}
