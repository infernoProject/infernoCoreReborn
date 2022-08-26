package pro.velovec.inferno.reborn.worldd.dao.guild;

import pro.velovec.inferno.reborn.common.dao.character.CharacterInfo;
import pro.velovec.inferno.reborn.common.utils.ByteArray;
import pro.velovec.inferno.reborn.common.utils.ByteConvertible;

import javax.persistence.*;

@Entity
@Table(name = "guild_members") // characters
public class GuildMember implements ByteConvertible {

    @Id
    @GeneratedValue
    private int id;

    @ManyToOne(fetch = FetchType.EAGER)
    private Guild guild;

    @ManyToOne(fetch = FetchType.EAGER)
    private CharacterInfo character;

    @Column(name = "level")
    private int level;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Guild getGuild() {
        return guild;
    }

    public void setGuild(Guild guild) {
        this.guild = guild;
    }

    public CharacterInfo getCharacter() {
        return character;
    }

    public void setCharacter(CharacterInfo character) {
        this.character = character;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public byte[] toByteArray() {
        return new ByteArray()
            .put(character)
            .put(level)
            .toByteArray();
    }
}
