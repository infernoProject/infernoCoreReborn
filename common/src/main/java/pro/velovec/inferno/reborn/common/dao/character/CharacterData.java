package pro.velovec.inferno.reborn.common.dao.character;

import pro.velovec.inferno.reborn.common.utils.ByteArray;
import pro.velovec.inferno.reborn.common.utils.ByteConvertible;
import pro.velovec.inferno.reborn.common.utils.ByteWrapper;

import javax.persistence.*;

@Entity
@Table(name = "character_stats") // characters
public class CharacterData implements ByteConvertible {

    @Id
    @GeneratedValue
    private int id;

    @OneToOne(fetch = FetchType.EAGER)
    private CharacterInfo character;

    // Character Level
    @Column(name = "level")
    private int level = 1;

    @Column(name = "exp")
    private long exp = 0;

    // Character Location

    @Column(name = "location")
    private int location = 1;

    @Column(name = "position_x")
    private float positionX = .0f;

    @Column(name = "position_y")
    private float positionY = .0f;

    @Column(name = "position_z")
    private float positionZ = .0f;

    @Column(name = "orientation")
    private float orientation = .0f;

    // Character Stats

    @Column(name = "vitality")
    private int vitality = 0;

    @Column(name = "strength")
    private int strength = 0;

    @Column(name = "intelligence")
    private int intelligence = 0;

    @Column(name = "control")
    private int control = 0;

    @Column(name = "agility")
    private int agility = 0;


    // Accessors
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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getExp() {
        return exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public float getPositionX() {
        return positionX;
    }

    public void setPositionX(float positionX) {
        this.positionX = positionX;
    }

    public float getPositionY() {
        return positionY;
    }

    public void setPositionY(float positionY) {
        this.positionY = positionY;
    }

    public float getPositionZ() {
        return positionZ;
    }

    public void setPositionZ(float positionZ) {
        this.positionZ = positionZ;
    }

    public float getOrientation() {
        return orientation;
    }

    public void setOrientation(float orientation) {
        this.orientation = orientation;
    }

    public int getVitality() {
        return vitality;
    }

    public void setVitality(int vitality) {
        this.vitality = vitality;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getIntelligence() {
        return intelligence;
    }

    public void setIntelligence(int intelligence) {
        this.intelligence = intelligence;
    }

    public int getControl() {
        return control;
    }

    public void setControl(int control) {
        this.control = control;
    }

    public int getAgility() {
        return agility;
    }

    public void setAgility(int agility) {
        this.agility = agility;
    }

    @Override
    public byte[] toByteArray() {
        return new ByteArray()
            .put(level).put(exp)
            .put(location).put(positionX).put(positionY).put(positionZ).put(orientation)
            .put(vitality).put(strength).put(intelligence).put(control).put(agility)
            .toByteArray();
    }

    public static CharacterData fromStats(ByteWrapper characterStats) {
        CharacterData characterData = new CharacterData();

        characterData.setLevel(1);
        characterData.setExp(0);

        characterData.setVitality(characterStats.getInt());
        characterData.setStrength(characterStats.getInt());
        characterData.setIntelligence(characterStats.getInt());
        characterData.setControl(characterStats.getInt());
        characterData.setAgility(characterStats.getInt());

        return characterData;
    }
}
