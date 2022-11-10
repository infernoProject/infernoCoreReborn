package pro.velovec.inferno.reborn.worldd.script;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import pro.velovec.inferno.reborn.common.dao.character.CharacterClass;
import pro.velovec.inferno.reborn.worldd.dao.script.Spell;
import pro.velovec.inferno.reborn.worldd.dao.script.SpellRepository;
import pro.velovec.inferno.reborn.worldd.world.player.WorldPlayer;

import java.util.ArrayList;
import java.util.List;

@Component
public class SpellManager {

    @Autowired
    private SpellRepository spellRepository;

    public List<Spell> listSpells(WorldPlayer player) {
        List<Spell> spellList = new ArrayList<>();

        for (CharacterClass characterClass: player.getClassList()) {
            spellList.addAll(
                spellRepository.findAllByRequiredClassAndRequiredLevel(characterClass.getClassInfo(), characterClass.getLevel())
            );
        }

        return spellList;
    }

    public Spell getSpell(int id, WorldPlayer player) {
        return listSpells(player).stream()
            .filter(spell -> spell.getId() == id)
            .findFirst().orElse(null);
    }
}
