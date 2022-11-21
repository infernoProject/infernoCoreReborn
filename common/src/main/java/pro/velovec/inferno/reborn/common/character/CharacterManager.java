package pro.velovec.inferno.reborn.common.character;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.velovec.inferno.reborn.common.constants.CommonConstants;
import pro.velovec.inferno.reborn.common.dao.auth.Account;
import pro.velovec.inferno.reborn.common.dao.character.*;
import pro.velovec.inferno.reborn.common.dao.data.ClassInfo;
import pro.velovec.inferno.reborn.common.dao.data.ClassInfoRepository;
import pro.velovec.inferno.reborn.common.dao.realmlist.RealmListEntry;
import pro.velovec.inferno.reborn.common.properties.InfernoCommonProperties;
import pro.velovec.libs.base.utils.TimeUtils;


import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@Component
public class CharacterManager {

    @Autowired
    private InfernoCommonProperties commonProperties;
    @Autowired
    private CharacterInfoRepository characterInfoRepository;

    @Autowired
    private CharacterDataRepository characterDataRepository;

    @Autowired
    private CharacterClassRepository characterClassRepository;

    @Autowired
    private ClassInfoRepository classInfoRepository;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(CharacterManager.class);

    public List<CharacterInfo> list(Account account) throws SQLException {
        return characterInfoRepository.findAllByAccountAndDeleteFlag(account, 0);
    }

    public List<CharacterInfo> list_deleted(Account account) throws SQLException {
        return characterInfoRepository.findAllByAccountAndDeleteFlag(account, 1);
    }

    public int create(CharacterInfo characterInfo, CharacterData characterData) throws SQLException {
        if (exists(characterInfo))
            return -1;

        characterInfo = characterInfoRepository.save(characterInfo);

        characterData.setCharacter(characterInfo);
        characterDataRepository.save(characterData);

        return characterInfo.getId();
    }

    public boolean delete(CharacterInfo characterInfo) throws SQLException {
        if (exists(characterInfo)) {
            characterInfo.setDeleteFlag(1);
            characterInfo.setDeleteAfter(TimeUtils.add(
                new Date(), Calendar.DAY_OF_MONTH, commonProperties.getCharacters().getDeleteAfter()
            ));

            characterInfoRepository.save(characterInfo);

            return true;
        }

        return false;
    }

    public boolean restore(CharacterInfo characterInfo) throws SQLException {
        if (!exists(characterInfo)) {
            characterInfo.setDeleteFlag(0);
            characterInfo.setDeleteAfter(null);

            characterInfoRepository.save(characterInfo);

            return true;
        }

        return false;
    }

    public boolean exists(CharacterInfo characterInfo) throws SQLException {
        return characterInfoRepository.findByRealmAndFirstNameAndLastNameAndDeleteFlag(
            characterInfo.getRealm(), characterInfo.getFirstName(), characterInfo.getLastName(), 0
        ) != null;
    }

    public CharacterInfo get(RealmListEntry realm, String firstName, String lastName) throws SQLException {
        return characterInfoRepository.findByRealmAndFirstNameAndLastNameAndDeleteFlag(
            realm, firstName, lastName, 0
        );
    }

    public CharacterInfo get(int characterId) throws SQLException {
        return characterInfoRepository.findById(characterId).orElse(null);
    }

    public void cleanup() throws SQLException {
        characterInfoRepository.deleteAll(
            characterInfoRepository.findAllByDeleteFlagAndDeleteAfterBefore(1, new Date())
        );
    }

    public void update(CharacterData characterData) throws SQLException {
        characterDataRepository.save(characterData);
    }

    public static boolean validateInitialStats(CharacterData characterData) {
        Integer[] stats = new Integer[] {
            characterData.getVitality(),
            characterData.getStrength(),
            characterData.getIntelligence(),
            characterData.getControl(),
            characterData.getAgility()
        };

        int statsSum = Arrays.stream(stats).mapToInt((x) -> x).sum();
        int statsMax = Arrays.stream(stats).mapToInt((x) -> x).max().getAsInt();
        int statsMin = Arrays.stream(stats).mapToInt((x) -> x).min().getAsInt();

        return statsSum <= CommonConstants.INITIAL_STATS_SUM &&
            statsMin >= CommonConstants.INITIAL_STATS_MIN &&
            statsMax <= CommonConstants.INITIAL_STATS_MAX;
    }

    public CharacterData getCharacterData(CharacterInfo characterInfo) {
        return characterDataRepository.findByCharacter(characterInfo);
    }

    public boolean hasClass(ClassInfo classInfo, CharacterInfo characterInfo) {
        return listClasses(characterInfo).stream()
            .anyMatch(characterClassInfo -> characterClassInfo.getClassInfo().getId() == classInfo.getId());
    }

    public CharacterClass getClass(ClassInfo classInfo, CharacterInfo characterInfo) {
        return listClasses(characterInfo).stream()
            .filter(characterClassInfo -> characterClassInfo.getClassInfo().getId() == classInfo.getId())
            .findFirst().orElse(null);
    }

    public CharacterClass addClass(ClassInfo classInfo, CharacterInfo characterInfo) {
        if (!hasClass(classInfo, characterInfo)) {
            CharacterClass characterClass = new CharacterClass();

            characterClass.setClassInfo(classInfo);
            characterClass.setCharacter(characterInfo);
            characterClass.setLevel(1);

            return characterClassRepository.save(characterClass);
        }

        return null;
    }

    public boolean increaseClassLevel(ClassInfo classInfo, int level, CharacterInfo characterInfo) {
        CharacterClass characterClass = getClass(classInfo, characterInfo);
        if (Objects.nonNull(characterClass)) {
            if (characterClass.getLevel() + level <= classInfo.getMaxLevel()) {
                characterClass.setLevel(level);
                characterClassRepository.save(characterClass);

                return true;
            }
        }

        return false;
    }

    public List<CharacterClass> listClasses(CharacterInfo characterInfo) {
        return characterClassRepository.findAllByCharacter(characterInfo);
    }
    public List<ClassInfo> listAvailableClasses(CharacterInfo characterInfo) throws IOException {
        List<ClassInfo> availableClasses = new ArrayList<>(classInfoRepository.findAllByHidden(false));

        List<CharacterClass> characterClasses = listClasses(characterInfo);
        CharacterData characterData = getCharacterData(characterInfo);
        for (ClassInfo classInfo: classInfoRepository.findAllByHidden(true)) {
            if (isEligibleForClass(classInfo, characterData, characterClasses)) {
                classInfo.setRequirements(null);
                availableClasses.add(classInfo);
            }
        }

        return availableClasses;
    }

    public boolean isEligibleForClass(ClassInfo classInfo, CharacterInfo characterInfo) throws IOException {
        return isEligibleForClass(
            classInfo, getCharacterData(characterInfo), listClasses(characterInfo)
        );
    }

    @SuppressWarnings("unchecked")
    private static boolean isEligibleForClass(ClassInfo classInfo, CharacterData characterData, List<CharacterClass> characterClasses) throws IOException {
        Map<String, Object> requirements = OBJECT_MAPPER.readValue(classInfo.getRequirements(), LinkedHashMap.class);

        boolean eligible = true;
        for (String key: requirements.keySet()) {
            switch (key) {
                case "race" -> {
                    List<String> requiredRace = (List<String>) requirements.get("race");
                    eligible = eligible && requiredRace.contains(characterData.getCharacter().getRaceInfo().getName());
                }
                case "no_race" -> {
                    List<String> restrictedRace = (List<String>) requirements.get("no_race");
                    eligible = eligible && !restrictedRace.contains(characterData.getCharacter().getRaceInfo().getName());
                }
                case "level" -> {
                    Integer requiredLevel = (Integer) requirements.get("level");
                    eligible = eligible && (requiredLevel <= characterData.getLevel());
                }
                case "has_class" -> {
                    List<Map<String, Integer>> requiredClasses = (List<Map<String, Integer>>) requirements.get("has_class");
                    for (Map<String, Integer> requiredClass : requiredClasses) {
                        eligible = eligible && characterClasses.stream()
                            .filter(characterClass -> characterClass.getClassInfo().getId() == requiredClass.get("id"))
                            .anyMatch(characterClass -> characterClass.getLevel() >= requiredClass.getOrDefault("level", 1));
                    }
                }
                case "has_no_class" -> {
                    List<Map<String, Integer>> restrictedClasses = (List<Map<String, Integer>>) requirements.get("has_no_class");
                    for (Map<String, Integer> restrictedClass : restrictedClasses) {
                        eligible = eligible && characterClasses.stream()
                            .noneMatch(characterClass -> characterClass.getClassInfo().getId() == restrictedClass.get("id"));
                    }
                }
                case "stats" -> {
                    Map<String, Integer> requiredStats = (Map<String, Integer>) requirements.get("stats");
                    for (String requiredStat : requiredStats.keySet()) {
                        eligible = switch (requiredStat) {
                            case "VIT" -> eligible && (characterData.getVitality() >= requiredStats.get(requiredStat));
                            case "STR" -> eligible && (characterData.getStrength() >= requiredStats.get(requiredStat));
                            case "INT" -> eligible && (characterData.getIntelligence() >= requiredStats.get(requiredStat));
                            case "CTR" -> eligible && (characterData.getControl() >= requiredStats.get(requiredStat));
                            case "AGI" -> eligible && (characterData.getAgility() >= requiredStats.get(requiredStat));
                            default -> eligible;
                        };
                    }
                }
                default -> eligible = false;
            }

            if (!eligible) {
                break;
            }
        }

        return eligible;
    }
}
