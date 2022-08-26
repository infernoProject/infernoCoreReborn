package pro.velovec.inferno.reborn.worldd.dao.script;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ScriptRepository extends CrudRepository<Script, Integer> {

    List<Script> findAll();
}
