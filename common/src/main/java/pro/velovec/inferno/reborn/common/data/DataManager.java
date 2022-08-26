package pro.velovec.inferno.reborn.common.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.velovec.inferno.reborn.common.dao.data.ClassInfo;
import pro.velovec.inferno.reborn.common.dao.data.ClassInfoRepository;
import pro.velovec.inferno.reborn.common.dao.data.RaceInfo;
import pro.velovec.inferno.reborn.common.dao.data.RaceInfoRepository;

import java.sql.SQLException;
import java.util.List;

@Component
public class DataManager {

    @Autowired
    private RaceInfoRepository raceInfoRepository;

    @Autowired
    private ClassInfoRepository classInfoRepository;

    public List<RaceInfo> raceList() throws SQLException {
        return raceInfoRepository.findAll();
    }

    public RaceInfo raceGetById(int raceId) throws SQLException {
        return raceInfoRepository.findById(raceId).orElse(null);
    }

    public List<ClassInfo> classList() throws SQLException {
        return classInfoRepository.findAll();
    }

    public ClassInfo classGetById(int classId) throws SQLException {
        return classInfoRepository.findById(classId).orElse(null);
    }

    public void update(Long diff) {
        // TODO(velovec): Implement object expiration
    }
}
