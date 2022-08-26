package pro.velovec.inferno.reborn.worldd.dao.script;

import org.springframework.data.repository.CrudRepository;

import pro.velovec.inferno.reborn.common.dao.data.ClassInfo;

import java.util.List;

public interface SpellRepository extends CrudRepository<Spell, Integer> {
    List<Spell> findAllByRequiredClassAndRequiredLevel(ClassInfo classInfo, long level);

    Spell findByIdAndRequiredClassAndRequiredLevel(int id, ClassInfo classInfo, long level);
}
