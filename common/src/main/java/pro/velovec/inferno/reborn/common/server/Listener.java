package pro.velovec.inferno.reborn.common.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

@Component
@Scope("prototype")
public class Listener extends Thread {

    private ListenerProperties listenerProperties;

    private final ChannelHandler channelHandler;

    private final EventLoopGroup master = new NioEventLoopGroup();
    private final EventLoopGroup worker = new NioEventLoopGroup();

    private static final Logger LOGGER = LoggerFactory.getLogger(Listener.class);

    public Listener() {
        channelHandler = new ChannelHandler();
    }

    public void setListenerConfig(ListenerProperties listenerConfig) {
        this.listenerProperties = listenerConfig;
    }

    @Override
    public void run() {
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(master, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(channelHandler)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(
                listenerProperties.getHost(),
                listenerProperties.getPort()
            ).sync();
            LOGGER.info(
                "Listener(host='{}',port={}): started",
                listenerProperties.getHost(),
                listenerProperties.getPort()
            );

            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @PreDestroy
    public void preDestroy() {
        LOGGER.info(
            "Listener(host='{}',port={}): shutdown in progress",
            listenerProperties.getHost(),
            listenerProperties.getPort()
        );

        worker.shutdownGracefully();
        master.shutdownGracefully();

        LOGGER.info(
            "Listener(host='{}',port={}): shutdown completed",
            listenerProperties.getHost(),
            listenerProperties.getPort()
        );
    }

    public ChannelHandler channelHandler() {
        return channelHandler;
    }

    public void addHandler(Class<? extends io.netty.channel.ChannelHandler> handler) {
        channelHandler.addHandler(handler);
    }

    public void addHandler(io.netty.channel.ChannelHandler handler) {
        channelHandler.addHandler(handler);
    }

}
