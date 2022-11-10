package pro.velovec.inferno.reborn.worldd.dao.time;

import org.springframework.data.repository.CrudRepository;

public interface WorldTimeRepository extends CrudRepository<WorldTime, Long> {

    WorldTime findByName(String serverName);
}
