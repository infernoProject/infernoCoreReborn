package pro.velovec.inferno.reborn.worldd.dao.items;

import pro.velovec.inferno.reborn.common.utils.ByteArray;
import pro.velovec.inferno.reborn.common.utils.ByteConvertible;

import javax.persistence.*;
import java.util.Arrays;

@Entity
@Table(name = "items") // objects
public class Item implements ByteConvertible {

    @Id
    @GeneratedValue
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "sell_price")
    private int sellPrice;
    @Column(name = "vendor_price")
    private int vendorPrice;

    @Column(name = "max_stack")
    private int maxStack;
    @Column(name = "max_owned")
    private int maxOwned;

    @Column(name = "durability")
    private int durability;

    @Column(name = "allowed_slots")
    private String allowedSlots;

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

    public int getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(int sellPrice) {
        this.sellPrice = sellPrice;
    }

    public int getVendorPrice() {
        return vendorPrice;
    }

    public void setVendorPrice(int vendorPrice) {
        this.vendorPrice = vendorPrice;
    }

    public int getMaxStack() {
        return maxStack;
    }

    public void setMaxStack(int maxStack) {
        this.maxStack = maxStack;
    }

    public int getMaxOwned() {
        return maxOwned;
    }

    public void setMaxOwned(int maxOwned) {
        this.maxOwned = maxOwned;
    }

    public int getDurability() {
        return durability;
    }

    public void setDurability(int durability) {
        this.durability = durability;
    }

    public String getAllowedSlots() {
        return allowedSlots;
    }

    public void setAllowedSlots(String allowedSlots) {
        this.allowedSlots = allowedSlots;
    }

    public boolean isEligibleForSlot(int slot) {
        return Arrays.stream(allowedSlots.split(","))
            .filter(slotId -> !slotId.isEmpty())
            .map(Integer::parseInt)
            .anyMatch(slotId -> slotId == slot);
    }

    @Override
    public byte[] toByteArray() {
        return new ByteArray()
            .put(id).put(name)
            .put(sellPrice).put(vendorPrice)
            .put(maxStack).put(maxOwned)
            .put(allowedSlots)
            .toByteArray();
    }
}
