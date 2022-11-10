package pro.velovec.inferno.reborn.common.dao.character;

import pro.velovec.inferno.reborn.common.dao.data.ClassInfo;
import pro.velovec.inferno.reborn.common.utils.ByteArray;
import pro.velovec.inferno.reborn.common.utils.ByteConvertible;

import javax.persistence.*;

@Entity
@Table(name = "character_classes") // characters
public class CharacterClass implements ByteConvertible {

    @Id
    @GeneratedValue
    private int id;

    @OneToOne(fetch = FetchType.EAGER)
    private CharacterInfo character;

    @ManyToOne(fetch = FetchType.EAGER)
    private ClassInfo classInfo;

    private int level;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CharacterInfo getCharacter() {
        return character;
    }

    public void setCharacter(CharacterInfo character) {
        this.character = character;
    }

    public ClassInfo getClassInfo() {
        return classInfo;
    }

    public void setClassInfo(ClassInfo classInfo) {
        this.classInfo = classInfo;
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
            .put(classInfo.getName())
            .put(classInfo.getRequirements())
            .put(classInfo.getMaxLevel())
            .put(level)
            .toByteArray();
    }
}
