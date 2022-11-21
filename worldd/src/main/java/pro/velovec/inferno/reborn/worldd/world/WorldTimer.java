package pro.velovec.inferno.reborn.worldd.world;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.velovec.inferno.reborn.worldd.dao.time.WorldTime;
import pro.velovec.inferno.reborn.worldd.dao.time.WorldTimeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class WorldTimer {

    @Autowired
    private WorldTimeRepository worldTimeRepository;

    private Long currentTime = System.currentTimeMillis();

    private Long serverTime = 0L;
    private Integer serverTimeRate = 1000;

    private Integer serverDay = 0;

    private String serverName;
    private boolean loaded = false;

    private final List<WorldTimerCallBack> callBackList = new ArrayList<>();
    private final List<WorldTimeChangeCallBack> timeChangeCallBackList = new ArrayList<>();

    @Scheduled(fixedRate = 100L)
    public void tick() {
        if (!loaded)
            return;

        Long time = System.currentTimeMillis();
        Long diff = time - currentTime;

        long newServerTime = (serverTime + diff * serverTimeRate) % (86400 * 1000);
        if (newServerTime < serverTime) {
            serverDay = (serverDay + 1) % 365;
        }
        serverTime = newServerTime;

        currentTime = time;

        callBackList.parallelStream()
            .forEach(callBack -> callBack.tick(diff));
    }

    public Long getServerTime() {
        return serverTime;
    }

    public int getServerTimeRate() {
        return serverTimeRate;
    }

    public Integer getServerDay() {
        return serverDay;
    }

    public void setServerTime(Integer serverDay, Long serverTime, Integer serverTimeRate) {
        this.serverDay = serverDay;
        this.serverTime = serverTime;
        this.serverTimeRate = serverTimeRate;

        timeChangeCallBackList.parallelStream()
            .forEach(callBack -> callBack.onTimeChange(serverDay, serverTime, serverTimeRate));
        save();
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void registerCallBack(WorldTimerCallBack callback) {
        callBackList.add(callback);
    }

    public void registerCallBack(WorldTimeChangeCallBack callback) {
        timeChangeCallBackList.add(callback);
    }

    public void save() {
        WorldTime worldTime = worldTimeRepository.findByName(serverName);
        if (Objects.isNull(worldTime)) {
            worldTime = new WorldTime();
            worldTime.setName(serverName);
        }

        worldTime.setServerDay(serverDay);
        worldTime.setServerTime(serverTime);
        worldTime.setServerTimeRate(serverTimeRate);

        worldTimeRepository.save(worldTime);
    }

    public void load() {
        WorldTime worldTime = worldTimeRepository.findByName(serverName);
        if (Objects.nonNull(worldTime)) {
            this.serverDay = worldTime.getServerDay();
            this.serverTime = worldTime.getServerTime();
            this.serverTimeRate = worldTime.getServerTimeRate();
        }

        loaded = true;
    }

    @FunctionalInterface
    public interface WorldTimerCallBack {

        void tick(Long timeDiff);

    }

    @FunctionalInterface
    public interface WorldTimeChangeCallBack {

        void onTimeChange(int serverDay, long serverTime, int serverTimeRate);

    }
}
