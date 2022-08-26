package pro.velovec.inferno.reborn.common.dao.data;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ClassInfoRepository extends CrudRepository<ClassInfo, Integer> {

    List<ClassInfo> findAll();

}
