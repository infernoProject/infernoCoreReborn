package pro.velovec.inferno.reborn.worldd;

import io.netty.channel.ChannelHandlerContext;

import pro.velovec.inferno.reborn.common.dao.auth.Account;
import pro.velovec.inferno.reborn.common.server.ServerSession;
import pro.velovec.inferno.reborn.common.utils.ByteArray;
import pro.velovec.inferno.reborn.common.utils.ByteConvertible;
import pro.velovec.inferno.reborn.worldd.world.WorldNotificationListener;
import pro.velovec.inferno.reborn.worldd.world.player.WorldPlayer;

import java.net.SocketAddress;
import java.util.*;

import static pro.velovec.inferno.reborn.worldd.constants.WorldOperations.EVENT;

public class WorldSession implements ServerSession, WorldNotificationListener {

    private Account account;

    private boolean authorized = false;

    private final ChannelHandlerContext ctx;
    private final SocketAddress remoteAddress;

    private WorldPlayer player;
    private final Queue<ByteConvertible> eventQueue = new LinkedList<>();

    public WorldSession(ChannelHandlerContext ctx, SocketAddress remoteAddress) {
        this.ctx = ctx;
        this.remoteAddress = remoteAddress;
    }

    @Override
    public void setAccount(Account account) {
        this.account = account;
    }

    @Override
    public Account getAccount() {
        return account;
    }

    @Override
    public boolean isAuthorized() {
        return authorized;
    }

    @Override
    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    @Override
    public void write(short opCode, ByteConvertible data) {
        ctx.writeAndFlush(new ByteArray(opCode).put(data));
    }

    @Override
    public SocketAddress address() {
        return remoteAddress;
    }

    @Override
    public ChannelHandlerContext context() {
        return ctx;
    }

    public WorldPlayer getPlayer() {
        return player;
    }

    public void setPlayer(WorldPlayer player) {
        this.player = player;
    }

    @Override
    public void onEvent(short type, ByteConvertible data) {
        eventQueue.add(new ByteArray(type).put(data));
    }

    public void pushEvents() {
        List<ByteConvertible> events = new ArrayList<>();

        while (!eventQueue.isEmpty()) {
            events.add(eventQueue.remove());
        }

        write(EVENT, new ByteArray().put(events));
    }
}
