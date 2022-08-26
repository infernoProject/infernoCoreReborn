package pro.velovec.inferno.reborn.common.server;

import io.netty.channel.ChannelHandlerContext;

import pro.velovec.inferno.reborn.common.dao.auth.Account;
import pro.velovec.inferno.reborn.common.utils.ByteConvertible;

import java.net.SocketAddress;

public interface ServerSession {

    void setAccount(Account account);
    Account getAccount();

    SocketAddress address();
    ChannelHandlerContext context();

    void setAuthorized(boolean authorized);
    boolean isAuthorized();

    void write(byte opCode, ByteConvertible data);
}
