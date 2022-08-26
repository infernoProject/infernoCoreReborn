package pro.velovec.inferno.reborn.common.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ConfigurableApplicationContext;

import pro.velovec.inferno.reborn.common.auth.AccountManager;
import pro.velovec.inferno.reborn.common.auth.SessionManager;
import pro.velovec.inferno.reborn.common.character.CharacterManager;
import pro.velovec.inferno.reborn.common.dao.auth.AccountLevel;
import pro.velovec.inferno.reborn.common.data.DataManager;
import pro.velovec.inferno.reborn.common.realmlist.RealmListManager;
import pro.velovec.inferno.reborn.common.utils.ByteArray;
import pro.velovec.inferno.reborn.common.utils.ByteWrapper;
import pro.velovec.inferno.reborn.common.utils.ErrorUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static pro.velovec.inferno.reborn.common.constants.CommonErrorCodes.*;

@ChannelHandler.Sharable
public abstract class ServerHandler extends ChannelInboundHandlerAdapter {

    protected final ConfigurableApplicationContext ctx;

    protected final RealmListManager realmList;

    protected final SessionManager sessionManager;
    protected final AccountManager accountManager;
    protected final CharacterManager characterManager;
    protected final DataManager dataManager;

    private final Map<SocketAddress, ServerSession> sessions;
    private final Map<Byte, Method> actions;


    protected static Logger logger;

    protected final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(
        Runtime.getRuntime().availableProcessors() * 10
    );

    public ServerHandler(ConfigurableApplicationContext ctx) {
        logger = LoggerFactory.getLogger(getClass());

        this.realmList = ctx.getBean(RealmListManager.class);

        this.sessionManager = ctx.getBean(SessionManager.class);
        this.accountManager = ctx.getBean(AccountManager.class);
        this.characterManager = ctx.getBean(CharacterManager.class);
        this.dataManager = ctx.getBean(DataManager.class);

        this.sessions = new ConcurrentHashMap<>();
        this.actions = new HashMap<>();
        this.ctx = ctx;

        registerActions();

        schedule(realmList::check, 10, 30);

        schedule(accountManager::cleanup, 10, 60);
        schedule(sessionManager::cleanup, 10, 60);
        schedule(characterManager::cleanup, 10, 60);
    }

    protected void schedule(ServerJob job, int delay, int period) {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                job.run();
            } catch (SQLException e) {
                logger.error("SQLError[{}]: {}", e.getSQLState(), e.getMessage());
            } catch (Exception e) {
                logger.error("Error:", e);
            }
        }, delay, period, TimeUnit.SECONDS);
    }

    private boolean validateAction(Method action) {
        return action.isAnnotationPresent(ServerAction.class) &&
            action.getReturnType().equals(ByteArray.class) &&
            action.getParameterCount() == 2 &&
            action.getParameterTypes()[0].equals(ByteWrapper.class) &&
            ServerSession.class.isAssignableFrom(action.getParameterTypes()[1]);
    }

    private void registerActions() {
        logger.info("Looking for ServerActions");
        for (Method method: getClass().getDeclaredMethods()) {
            if (!validateAction(method))
                continue;

            ServerAction serverAction = method.getAnnotation(ServerAction.class);
            for (byte opCode: serverAction.opCode()) {
                if (logger.isDebugEnabled())
                    logger.debug(String.format("Action(0x%02X): %s", opCode, method.getName()));
                actions.put(opCode, method);
            }
        }
        logger.info("ServerActions registered: {}", actions.size());
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        SocketAddress remoteAddress = ctx.channel().remoteAddress();
        ServerSession serverSession = onSessionInit(ctx, remoteAddress);

        sessions.put(remoteAddress, serverSession);

        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        SocketAddress remoteAddress = ctx.channel().remoteAddress();

        onSessionClose(remoteAddress);
        sessions.remove(remoteAddress);

        super.channelUnregistered(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteWrapper request = (ByteWrapper) msg;
        ByteArray response;

        if (logger.isDebugEnabled())
            logger.debug("IN: {}", request.toString());

        ServerSession serverSession = sessionGet(ctx.channel().remoteAddress());
        byte opCode = request.getByte();

        if (actions.containsKey(opCode)) {
            Method actionMethod = actions.get(opCode);
            ServerAction serverAction = actionMethod.getAnnotation(ServerAction.class);

            if (!serverAction.authRequired()||(serverSession.isAuthorized()&& AccountLevel.hasAccess(serverSession.getAccount().getAccessLevel(), serverAction.minLevel()))) {
                try {
                    response = (ByteArray) actionMethod.invoke(this, request, serverSession);
                } catch (InvocationTargetException e) {
                    ErrorUtils.logger(logger).error("Unable to process request", e);

                    response = new ByteArray(SERVER_ERROR);
                }
            } else {
                response = new ByteArray(AUTH_REQUIRED);
            }
        } else {
            response = new ByteArray(UNKNOWN_OPCODE);
        }

        if (logger.isDebugEnabled())
            logger.debug("OUT: {}", response.toString());

        sessionManager.update(serverSession.address());
        ctx.write(new ByteArray(opCode).put(response));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ErrorUtils.logger(logger).error("Unable to process request", cause);
        ctx.close();
    }

    public ServerSession sessionGet(SocketAddress remoteAddress) {
        return sessions.getOrDefault(remoteAddress, null);
    }

    public List<ServerSession> sessionList() {
        return new ArrayList<>(sessions.values());
    }

    protected abstract ServerSession onSessionInit(ChannelHandlerContext ctx, SocketAddress remoteAddress);
    protected abstract void onSessionClose(SocketAddress remoteAddress);

    protected abstract void onShutdown();

    public void shutdown() {
        onShutdown();

        scheduler.shutdown();
    }
}
