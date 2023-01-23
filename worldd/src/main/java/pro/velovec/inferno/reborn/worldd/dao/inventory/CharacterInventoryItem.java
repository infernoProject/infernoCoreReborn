package pro.velovec.inferno.reborn.worldd.dao.inventory;

import pro.velovec.inferno.reborn.common.dao.character.CharacterInfo;
import pro.velovec.inferno.reborn.common.utils.ByteArray;
import pro.velovec.inferno.reborn.common.utils.ByteConvertible;
import pro.velovec.inferno.reborn.worldd.dao.items.Item;

import jakarta.persistence.*;

@Entity
@Table(name = "character_inventory") // characters
public class CharacterInventoryItem implements ByteConvertible {

    @Id
    @GeneratedValue
    private int id;

    @ManyToOne(fetch = FetchType.EAGER)
    private CharacterInfo character;

    @ManyToOne(fetch = FetchType.EAGER)
    private Item item;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "durability")
    private int durability;

    @Column(name = "inventory_type")
    private int inventoryType;

    @Column(name = "inventory_id")
    private int inventoryId;

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

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getDurability() {
        return durability;
    }

    public void setDurability(int durability) {
        this.durability = durability;
    }

    public int getInventoryType() {
        return inventoryType;
    }

    public void setInventoryType(int inventoryType) {
        this.inventoryType = inventoryType;
    }

    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }

    @Override
    public byte[] toByteArray() {
        return new ByteArray()
            .put(inventoryType).put(inventoryId)
            .put(quantity).put(durability)
            .put(item)
            .toByteArray();
    }
}
