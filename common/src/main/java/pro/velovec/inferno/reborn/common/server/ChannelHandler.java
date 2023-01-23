package pro.velovec.inferno.reborn.common.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChannelHandler extends ChannelInitializer<SocketChannel> {

    private final List<Object> handlers = new CopyOnWriteArrayList<>();

    @Override
    @SuppressWarnings("unchecked")
    public void initChannel(SocketChannel channel) throws Exception {
        for (Object channelHandler: handlers) {
            if (channelHandler instanceof io.netty.channel.ChannelHandler) {
                channel.pipeline().addLast((io.netty.channel.ChannelHandler) channelHandler);
            } else if (channelHandler instanceof Class) {
                channel.pipeline().addLast(
                    ((Class<? extends io.netty.channel.ChannelHandler>) channelHandler).getConstructor().newInstance()
                );
            }
        }
    }

    public void addHandler(Class<? extends io.netty.channel.ChannelHandler> handler) {
        handlers.add(handler);
    }

    public void addHandler(io.netty.channel.ChannelHandler handler) {
        handlers.add(handler);
    }

    public void resetHandlers() {
        handlers.clear();
    }
}
