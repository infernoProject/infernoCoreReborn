package pro.velovec.inferno.reborn.worldd.world;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pro.velovec.inferno.reborn.worldd.dao.time.WorldTime;
import pro.velovec.inferno.reborn.worldd.dao.time.WorldTimeRepository;

import javax.annotation.PreDestroy;
import javax.persistence.Access;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class WorldTimer extends Thread {

    @Autowired
    private WorldTimeRepository worldTimeRepository;

    private Long currentTime = System.currentTimeMillis();

    private Long serverTime = 0L;
    private Integer serverTimeRate = 1000;

    private Integer serverDay = 0;

    private String serverName;
    private boolean isRunning = true;

    private static final Logger logger = LoggerFactory.getLogger(WorldTimer.class);
    private final List<WorldTimerCallBack> callBackList = new ArrayList<>();
    private final List<WorldTimeChangeCallBack> timeChangeCallBackList = new ArrayList<>();

    @Override
    public void run() {
        logger.info("WorldTimer started");

        while (isRunning) {
            Long timeDiff = tick();

            callBackList.parallelStream()
                .forEach(callBack -> callBack.tick(timeDiff));
        }

        logger.info("WorldTimer stopped");
    }

    @PreDestroy
    public void preDestroy() {
        isRunning = false;
    }

    public Long tick() {
        Long time = System.currentTimeMillis();
        Long diff = time - currentTime;

        long newServerTime = (serverTime + diff * serverTimeRate) % (86400 * 1000);
        if (newServerTime < serverTime) {
            serverDay = (serverDay + 1) % 365;
        }
        serverTime = newServerTime;

        currentTime = time;

        return diff;
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

    public String getServerDateTime() {
        int hours = (int) (serverTime / 3600 / 1000);
        int minutes = (int) ((serverTime / 60 / 1000) % 60);

        return String.format(
            "day %d time %02d:%02d", serverDay, hours, minutes
        );
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
