package pro.velovec.inferno.reborn.common.dao.character;

import pro.velovec.inferno.reborn.common.dao.auth.Account;
import pro.velovec.inferno.reborn.common.dao.data.ClassInfo;
import pro.velovec.inferno.reborn.common.dao.data.GenderInfo;
import pro.velovec.inferno.reborn.common.dao.data.RaceInfo;
import pro.velovec.inferno.reborn.common.dao.realmlist.RealmListEntry;
import pro.velovec.inferno.reborn.common.utils.ByteArray;
import pro.velovec.inferno.reborn.common.utils.ByteConvertible;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "characters") // characters
public class CharacterInfo implements ByteConvertible {

    @Id
    @GeneratedValue
    private int id;

    @ManyToOne(fetch = FetchType.EAGER)
    private Account account;

    @ManyToOne(fetch = FetchType.EAGER)
    private RealmListEntry realm;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @ManyToOne(fetch = FetchType.EAGER)
    private RaceInfo raceInfo;

    @Column(name = "gender")
    private GenderInfo gender;

    @ManyToOne(fetch = FetchType.EAGER)
    private ClassInfo classInfo;

    @Column(name = "level")
    private int level = 0;

    @Column(name = "exp")
    private long exp = 0;

    @Column(name = "currency")
    private long currency = 0;


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

    @Column(name = "body")
    private byte[] body;

    @Column(name = "delete_flag")
    private int deleteFlag;

    @Column(name = "delete_after")
    private Date deleteAfter;

    public CharacterInfo() {
        // Default constructor for SQLObjectWrapper
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public RealmListEntry getRealm() {
        return realm;
    }

    public void setRealm(RealmListEntry realm) {
        this.realm = realm;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public RaceInfo getRaceInfo() {
        return raceInfo;
    }

    public void setRaceInfo(RaceInfo raceInfo) {
        this.raceInfo = raceInfo;
    }

    public GenderInfo getGender() {
        return gender;
    }

    public void setGender(GenderInfo gender) {
        this.gender = gender;
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

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public int getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(int deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    public Date getDeleteAfter() {
        return deleteAfter;
    }

    public void setDeleteAfter(Date deleteAfter) {
        this.deleteAfter = deleteAfter;
    }

    @Override
    public byte[] toByteArray() {
        return new ByteArray()
            .put(id).put(realm.getId()).put(location)
            .put(firstName).put(lastName)
            .put(raceInfo.getId()).put(gender.toString().toLowerCase()).put(classInfo.getId())
            .put(level).put(exp).put(currency).put(body)
            .put(positionX).put(positionY).put(positionZ)
            .toByteArray();
    }

    @Override
    public String toString() {
        return String.format(
            "CharacterInfo(ID=%d):LVL(%d):R(%s):C(%s): %s : %s %s",
            id, level, raceInfo, classInfo, gender, firstName, lastName
        );
    }
}
