package pro.velovec.inferno.reborn.worldd.world.invite;

import pro.velovec.inferno.reborn.common.utils.ByteArray;
import pro.velovec.inferno.reborn.common.utils.ByteConvertible;
import pro.velovec.inferno.reborn.common.utils.ByteWrapper;
import pro.velovec.inferno.reborn.worldd.world.player.WorldPlayer;

public class Invite implements ByteConvertible {

    private long id;
    private InviteType type;
    private WorldPlayer sender;
    private ByteConvertible data;

    public Invite(long id, InviteType type, WorldPlayer sender, ByteConvertible data) {
        this.id = id;
        this.type = type;
        this.sender = sender;
        this.data = data;
    }

    @Override
    public byte[] toByteArray() {
        return new ByteArray()
            .put(id)
            .put(type.toString().toLowerCase())
            .put(sender.getOID())
            .put(data)
            .toByteArray();
    }

    public WorldPlayer getSender() {
        return sender;
    }

    public InviteType getType() {
        return type;
    }

    public ByteWrapper getData() {
        return new ByteWrapper(data.toByteArray());
    }
}
