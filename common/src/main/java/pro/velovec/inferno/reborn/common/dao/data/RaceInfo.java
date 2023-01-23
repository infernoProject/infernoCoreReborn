package pro.velovec.inferno.reborn.common.dao.data;


import pro.velovec.inferno.reborn.common.utils.ByteArray;
import pro.velovec.inferno.reborn.common.utils.ByteConvertible;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "races_info") // objects
public class RaceInfo implements ByteConvertible {

    @Id
    @GeneratedValue
    private int id;

    private String name;

    private String resource;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    @Override
    public byte[] toByteArray() {
        return new ByteArray()
            .put(id).put(name).put(resource)
            .toByteArray();
    }

    @Override
    public String toString() {
        return name;
    }
}
