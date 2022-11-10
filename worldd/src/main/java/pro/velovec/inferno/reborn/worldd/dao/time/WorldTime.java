package pro.velovec.inferno.reborn.worldd.dao.time;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "world_time")
public class WorldTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    private Integer serverDay;

    private Long serverTime;

    private Integer serverTimeRate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getServerDay() {
        return serverDay;
    }

    public void setServerDay(Integer serverDay) {
        this.serverDay = serverDay;
    }

    public Long getServerTime() {
        return serverTime;
    }

    public void setServerTime(Long serverTime) {
        this.serverTime = serverTime;
    }

    public Integer getServerTimeRate() {
        return serverTimeRate;
    }

    public void setServerTimeRate(Integer serverTimeRate) {
        this.serverTimeRate = serverTimeRate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldTime worldTime = (WorldTime) o;
        return Objects.equals(id, worldTime.id) && name.equals(worldTime.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
