package pro.velovec.inferno.reborn.worldd.script;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import pro.velovec.inferno.reborn.worldd.dao.script.Spell;
import pro.velovec.inferno.reborn.worldd.dao.script.SpellRepository;
import pro.velovec.inferno.reborn.worldd.world.player.WorldPlayer;

import java.util.List;

@Component
public class SpellManager {

    @Autowired
    private SpellRepository spellRepository;

    public List<Spell> listSpells(WorldPlayer player) {
        return spellRepository.findAllByRequiredClassAndRequiredLevel(
            player.getCharacterInfo().getClassInfo(),
            player.getCharacterInfo().getLevel()
        );
    }

    public Spell getSpell(int id, WorldPlayer player) {
        return spellRepository.findByIdAndRequiredClassAndRequiredLevel(
            id,
            player.getCharacterInfo().getClassInfo(),
            player.getCharacterInfo().getLevel()
        );
    }
}
