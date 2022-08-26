package pro.velovec.inferno.reborn.common.dao.data;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RaceInfoRepository extends CrudRepository<RaceInfo, Integer> {

    List<RaceInfo> findAll();

}
