package pro.velovec.inferno.reborn.worldd;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import org.springframework.context.ConfigurableApplicationContext;

import pro.velovec.inferno.reborn.common.dao.auth.Account;
import pro.velovec.inferno.reborn.common.dao.auth.AccountLevel;
import pro.velovec.inferno.reborn.common.dao.auth.Session;
import pro.velovec.inferno.reborn.common.dao.character.CharacterClass;
import pro.velovec.inferno.reborn.common.dao.character.CharacterData;
import pro.velovec.inferno.reborn.common.dao.character.CharacterInfo;
import pro.velovec.inferno.reborn.common.dao.data.ClassInfo;
import pro.velovec.inferno.reborn.common.server.ServerAction;
import pro.velovec.inferno.reborn.common.server.ServerHandler;
import pro.velovec.inferno.reborn.common.server.ServerSession;
import pro.velovec.inferno.reborn.common.utils.ByteArray;
import pro.velovec.inferno.reborn.common.utils.ByteWrapper;
import pro.velovec.inferno.reborn.worldd.constants.WorldEventType;
import pro.velovec.inferno.reborn.worldd.dao.guild.Guild;
import pro.velovec.inferno.reborn.worldd.dao.guild.GuildMember;
import pro.velovec.inferno.reborn.worldd.dao.inventory.CharacterInventoryItem;
import pro.velovec.inferno.reborn.worldd.dao.script.*;
import pro.velovec.inferno.reborn.worldd.map.WorldMap;
import pro.velovec.inferno.reborn.worldd.map.WorldMapManager;
import pro.velovec.inferno.reborn.worldd.properties.WorldServerProperties;
import pro.velovec.inferno.reborn.worldd.script.ScriptManager;
import pro.velovec.inferno.reborn.worldd.script.ScriptValidationResult;
import pro.velovec.inferno.reborn.worldd.script.SpellManager;
import pro.velovec.inferno.reborn.worldd.utils.MathUtils;
import pro.velovec.inferno.reborn.worldd.world.InternalCommand;
import pro.velovec.inferno.reborn.worldd.world.WorldTimer;
import pro.velovec.inferno.reborn.worldd.world.chat.ChatManager;
import pro.velovec.inferno.reborn.worldd.world.chat.ChatMessageType;
import pro.velovec.inferno.reborn.worldd.world.creature.WorldCreatureStats;
import pro.velovec.inferno.reborn.worldd.world.guild.GuildManager;
import pro.velovec.inferno.reborn.worldd.world.inventory.InventoryManager;
import pro.velovec.inferno.reborn.worldd.world.invite.InviteManager;
import pro.velovec.inferno.reborn.worldd.world.invite.InviteType;
import pro.velovec.inferno.reborn.worldd.world.items.ItemManager;
import pro.velovec.inferno.reborn.worldd.world.movement.WorldPosition;
import pro.velovec.inferno.reborn.worldd.world.object.WorldObject;
import pro.velovec.inferno.reborn.worldd.world.player.WorldPlayer;

import javax.script.ScriptException;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static pro.velovec.inferno.reborn.common.constants.CommonErrorCodes.*;
import static pro.velovec.inferno.reborn.worldd.constants.WorldErrorCodes.*;
import static pro.velovec.inferno.reborn.worldd.constants.WorldOperations.*;

@ChannelHandler.Sharable
public class WorldHandler extends ServerHandler {

    private final WorldMapManager worldMapManager;
    private final ScriptManager scriptManager;
    private final ChatManager chatManager;
    private final GuildManager guildManager;
    private final InviteManager inviteManager;
    private final ItemManager itemManager;
    private final InventoryManager inventoryManager;
    private final SpellManager spellManager;

    private final WorldServerProperties properties;

    private final Map<String, Method> internalCommands;

    private final WorldTimer worldTimer;

    private final String serverName;

    public WorldHandler(ConfigurableApplicationContext ctx) {
        super(ctx);

        worldMapManager = ctx.getBean(WorldMapManager.class);
        scriptManager = ctx.getBean(ScriptManager.class);
        chatManager = ctx.getBean(ChatManager.class);
        guildManager = ctx.getBean(GuildManager.class);
        inviteManager = ctx.getBean(InviteManager.class);
        itemManager = ctx.getBean(ItemManager.class);
        inventoryManager = ctx.getBean(InventoryManager.class);
        spellManager = ctx.getBean(SpellManager.class);
        properties = ctx.getBean(WorldServerProperties.class);
        worldTimer = ctx.getBean(WorldTimer.class);

        internalCommands = registerInternalCommands();

        serverName = properties.getName();

        if (Objects.isNull(serverName)) {
            logger.error("Server name not specified");
            System.exit(1);
        }

        try {
            if (!realmList.exists(serverName)) {
                logger.error("Server with name '{}' is not registered", serverName);
                System.exit(1);
            }
        } catch (SQLException e) {
            logger.error("SQLError[{}]: {}", e.getSQLState(), e.getMessage());
            System.exit(1);
        }

        try {
            worldMapManager.readMapData(new File(properties.getMap().getDataPath()));
        } catch (SQLException e) {
            logger.error("SQLError[{}]: {}", e.getSQLState(), e.getMessage());
            System.exit(1);
        }

        worldTimer.registerCallBack(this::update);
        worldTimer.registerCallBack(this::onServerTimeChange);
        worldTimer.setServerName(serverName);

        worldTimer.load();

        schedule(() -> realmList.online(serverName, true), 10, 15);
        schedule(worldTimer::save, 10, 15);
    }

    // Internal commands

    private Map<String, Method> registerInternalCommands() {
        logger.info("Looking for InternalCommands");
        Map<String, Method> internalCommands = new HashMap<>();

        for (Method method: getClass().getDeclaredMethods()) {
            if (!validateInternalCommand(method)) {
                continue;
            }

            InternalCommand internalCommand = method.getAnnotation(InternalCommand.class);
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Command('%s'): %s", internalCommand.command(), method.getName()));
            }
            internalCommands.put(internalCommand.command(), method);
        }
        logger.info("InternalCommands registered: {}", internalCommands.size());

        return internalCommands;
    }

    private boolean validateInternalCommand(Method action) {
        return action.isAnnotationPresent(InternalCommand.class) &&
            action.getReturnType().equals(ByteArray.class) &&
            action.getParameterCount() == 2 &&
            action.getParameterTypes()[0].equals(String[].class) &&
            action.getParameterTypes()[1].equals(ServerSession.class);
    }

    // Session handling

    @Override
    protected ServerSession onSessionInit(ChannelHandlerContext ctx, SocketAddress remoteAddress) {
        return new WorldSession(ctx, remoteAddress);
    }

    @Override
    protected void onSessionClose(SocketAddress remoteAddress) {
        try {
            ServerSession session = sessionGet(remoteAddress);
            WorldPlayer player = ((WorldSession) session).getPlayer();
            if (Objects.nonNull(player)) {
                CharacterData characterData = player.getCharacterData();
                WorldPosition position = player.getPosition();

                characterData.setLocation(position.getLocation());

                characterData.setPositionX(position.getX());
                characterData.setPositionY(position.getY());
                characterData.setPositionZ(position.getZ());

                characterData.setOrientation(position.getOrientation());

                characterManager.update(characterData);

                player.destroy();
            }

            ((WorldSession) session).setPlayer(null);

            sessionManager.kill(session.getAccount());
        } catch (SQLException e) {
            logger.error("SQLError[{}]: {}", e.getSQLState(), e.getMessage());
        }
    }

    // Client Authorization

    @ServerAction(opCode = AUTHORIZE)
    public ByteArray authorize(ByteWrapper request, ServerSession serverSession) throws Exception {
        Session session = sessionManager.get(request.getBytes());
        if (Objects.isNull(session)) {
            return new ByteArray(AUTH_ERROR);
        }

        Account account = sessionManager.authorize(session, serverSession.address());
        if (Objects.isNull(account)) {
            return new ByteArray(AUTH_ERROR);
        }

        if (Objects.isNull(session.getCharacterInfo())) {
            return new ByteArray(AUTH_ERROR);
        }

        if (session.getCharacterInfo().getRealm().getId() != realmList.get(serverName).getId()) {
            return new ByteArray(AUTH_ERROR);
        }

        serverSession.setAuthorized(true);
        serverSession.setAccount(account);

        WorldPlayer player = new WorldPlayer(
            (WorldSession) serverSession, session.getCharacterInfo(),
            characterManager.getCharacterData(session.getCharacterInfo()),
            characterManager.listClasses(session.getCharacterInfo())
        );

        player.updatePosition(MOVE_STOP, player.getPosition(), worldMapManager.getMap(player.getPosition()));

        ((WorldSession) serverSession).setPlayer(player);

        return new ByteArray(SUCCESS)
            .put(player.getOID())
            .put(worldMapManager.getMapById(player.getCharacterData().getLocation()).getLocation())
            .put(session.getCharacterInfo())
            .put(player.getState())
            .put(worldTimer.getServerDay())
            .put(worldTimer.getServerTime())
            .put(worldTimer.getServerTimeRate());
    }

    // Command Execution

    @ServerAction(opCode = EXECUTE, authRequired = true)
    public ByteArray executeCommand(ByteWrapper request, ServerSession session) throws Exception {
        String cmd = request.getString();
        Command command = scriptManager.getCommand(cmd);

        if (Objects.isNull(command)) {
            return executeInternalCommand(cmd, request.getStrings(), session);
        }

        if (AccountLevel.hasAccess(session.getAccount().getAccessLevel(), command.getLevel())) {
            return new ByteArray(SUCCESS).put(
                command.execute(ctx, sessionManager.get(session.getAccount()), request.getStrings())
            );
        } else {
            return new ByteArray(AUTH_ERROR);
        }
    }

    private ByteArray executeInternalCommand(String command, String[] args, ServerSession session) {
        logger.info("User {} is executing internal command '{}' with arguments: {}", session.getAccount().getLogin(), command, args);

        if (internalCommands.containsKey(command)) {
            Method internalCommand = internalCommands.get(command);
            AccountLevel level = internalCommand.getAnnotation(InternalCommand.class).level();

            if (!AccountLevel.hasAccess(session.getAccount().getAccessLevel(), level)) {
                return new ByteArray(AUTH_ERROR);
            }

            internalCommand.setAccessible(true);

            try {
                return (ByteArray) internalCommand.invoke(this, args, session);
            } catch (IllegalAccessException e) {
                logger.error("Unable to execute internal command '{}': {}", command, e);

                return new ByteArray(SERVER_ERROR);
            } catch (InvocationTargetException e) {
                logger.error("Unable to execute internal command '{}': {}", command, e.getTargetException());

                return new ByteArray(SERVER_ERROR);
            }
        }

        return new ByteArray(INVALID_REQUEST);
    }

    // Characters Stats

    @ServerAction(opCode = STATS_ADD, authRequired = true)
    public ByteArray statsAdd(ByteWrapper request, ServerSession session) throws Exception {
        WorldPlayer player = ((WorldSession) session).getPlayer();

        if (player.getAvailableStatPoints() > 0) {
            CharacterData characterData = player.getCharacterData();

            switch (request.getInt()) {
                case 0:
                    characterData.setVitality(characterData.getVitality() + 1);
                    break;
                case 1:
                    characterData.setStrength(characterData.getStrength() + 1);
                    break;
                case 2:
                    characterData.setIntelligence(characterData.getIntelligence() + 1);
                    break;
                case 3:
                    characterData.setControl(characterData.getControl() + 1);
                    break;
                case 4:
                    characterData.setAgility(characterData.getAgility() + 1);
                    break;
                default:
                    return new ByteArray(INVALID_REQUEST);
            }

            characterManager.update(characterData);
            player.processStatsChange(WorldCreatureStats.fromCharacterData(characterData));

            return new ByteArray(SUCCESS)
                .put(player.getState());
        } else {
            return new ByteArray(INSUFFICIENT_RESOURCES);
        }
    }

    // Character class management

    @ServerAction(opCode = CLASS_LIST, authRequired = true)
    public ByteArray classList(ByteWrapper request, ServerSession session) throws Exception {
        WorldPlayer player = ((WorldSession) session).getPlayer();

        return new ByteArray(SUCCESS).put(
            characterManager.listAvailableClasses(player.getCharacterInfo())
        );
    }

    @ServerAction(opCode = CLASS_ADD, authRequired = true)
    public ByteArray classAdd(ByteWrapper request, ServerSession session) throws Exception {
        WorldPlayer player = ((WorldSession) session).getPlayer();

        if (player.getAvailableClassPoints() > 0) {
            ClassInfo classInfo = dataManager.classGetById(request.getInt());
            if (Objects.isNull(classInfo)) {
                return new ByteArray(NOT_EXISTS);
            }

            if (!characterManager.isEligibleForClass(classInfo, player.getCharacterInfo())) {
                return new ByteArray(NOT_EXISTS);
            }

            if (characterManager.hasClass(classInfo, player.getCharacterInfo())) {
                if (!characterManager.increaseClassLevel(classInfo, 1, player.getCharacterInfo())) {
                    return new ByteArray(OUT_OF_RANGE);
                }
            } else {
                CharacterClass characterClass = characterManager.addClass(classInfo, player.getCharacterInfo());
                if (Objects.isNull(characterClass))  {
                    return new ByteArray(SERVER_ERROR);
                }
            }

            player.processClassListChange(characterManager.listClasses(player.getCharacterInfo()));

            return new ByteArray(SUCCESS)
                .put(player.getState());
        } else {
            return new ByteArray(INSUFFICIENT_RESOURCES);
        }
    }

    // Movement handling

    @ServerAction(opCode = {
        MOVE_START_FORWARD, MOVE_START_BACKWARD,
        MOVE_START_STRAFE_LEFT, MOVE_START_STRAFE_RIGHT,
        MOVE_START_TURN_LEFT, MOVE_START_TURN_RIGHT,
        MOVE_START_PITCH_UP, MOVE_START_PITCH_DOWN,
        MOVE_START_SWIM,
        MOVE_JUMP, MOVE_HEARTBEAT, MOVE_FALL,
        MOVE_STOP, MOVE_STOP_STRAFE, MOVE_STOP_TURN, MOVE_STOP_PITCH, MOVE_STOP_SWIM,
        MOVE_SET_RUN_MODE, MOVE_SET_WALK_MODE
    }, authRequired = true)
    public ByteArray move(ByteWrapper request, ServerSession session) {
        WorldPlayer player = ((WorldSession) session).getPlayer();
        WorldMap map = worldMapManager.getMap(player.getPosition());

        request.rewind();
        short opCode = request.getShort();

        try {
            WorldPosition position = new WorldPosition(
                map.getLocation().getId(),
                request.getFloat(),
                request.getFloat(),
                request.getFloat(),
                request.getFloat()
            );

            if (map.isLegalMove(player, position)) {
                player.updatePosition(opCode, position, map);
                return new ByteArray(SUCCESS).put(position);
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Illegal move: {}", e.getMessage());
        }

        return new ByteArray(ILLEGAL_MOVE).put(player.getPosition());
    }

    // Terrain data validation

    @ServerAction(opCode = TERRAIN_CHECK, authRequired = true)
    public ByteArray terrainCheck(ByteWrapper request, ServerSession session) {
        WorldMap map = worldMapManager.getMapById(request.getInt());
        if (Objects.nonNull(map)) {
            // TODO: Implement Terrain data validation
            return new ByteArray(SUCCESS)
                .put(false)
                .put(map.getLocation().getId());
        }

        return new ByteArray(NOT_EXISTS);
    }

    @ServerAction(opCode = TERRAIN_LOAD, authRequired = true)
    public ByteArray terrainLoad(ByteWrapper request, ServerSession session) {
        WorldMap map = worldMapManager.getMapById(request.getInt());
        if (Objects.nonNull(map)) {
            return new ByteArray(SUCCESS)
                .put(map);
        }

        return new ByteArray(NOT_EXISTS);
    }

    // Character inventory

    @ServerAction(opCode = INVENTORY_LIST, authRequired = true)
    public ByteArray inventoryList(ByteWrapper request, ServerSession session) throws Exception {
        WorldPlayer player = ((WorldSession) session).getPlayer();

        List<CharacterInventoryItem> inventoryItems = inventoryManager.getCharacterInventory(player.getCharacterInfo().getId());
        double inventoryWeight = inventoryManager.getInventoryWeight(player.getCharacterInfo().getId());

        return new ByteArray(SUCCESS)
            .put(inventoryItems)
            .put(inventoryWeight);
    }

    @ServerAction(opCode = INVENTORY_ADD, authRequired = true)
    public ByteArray inventoryAdd(ByteWrapper request, ServerSession session) throws Exception {
        WorldPlayer player = ((WorldSession) session).getPlayer();

        // TODO: Implement item add processing

        List<CharacterInventoryItem> inventoryItems = inventoryManager.getCharacterInventory(player.getCharacterInfo().getId());
        double inventoryWeight = inventoryManager.getInventoryWeight(player.getCharacterInfo().getId());

        return new ByteArray(SUCCESS)
            .put(inventoryItems)
            .put(inventoryWeight);
    }

    @ServerAction(opCode = INVENTORY_REMOVE, authRequired = true)
    public ByteArray inventoryRemove(ByteWrapper request, ServerSession session) throws Exception {
        WorldPlayer player = ((WorldSession) session).getPlayer();

        // TODO: Implement item remove processing

        List<CharacterInventoryItem> inventoryItems = inventoryManager.getCharacterInventory(player.getCharacterInfo().getId());
        double inventoryWeight = inventoryManager.getInventoryWeight(player.getCharacterInfo().getId());

        return new ByteArray(SUCCESS)
            .put(inventoryItems)
            .put(inventoryWeight);
    }

    @ServerAction(opCode = INVENTORY_MOVE, authRequired = true)
    public ByteArray inventoryMove(ByteWrapper request, ServerSession session) throws Exception {
        WorldPlayer player = ((WorldSession) session).getPlayer();

        // TODO: Implement item move processing

        List<CharacterInventoryItem> inventoryItems = inventoryManager.getCharacterInventory(player.getCharacterInfo().getId());
        double inventoryWeight = inventoryManager.getInventoryWeight(player.getCharacterInfo().getId());

        return new ByteArray(SUCCESS)
            .put(inventoryItems)
            .put(inventoryWeight);
    }

    // Character spells

    @ServerAction(opCode = SPELL_LIST, authRequired = true)
    public ByteArray spellList(ByteWrapper request, ServerSession session) {
        WorldPlayer player = ((WorldSession) session).getPlayer();
        List<Spell> spellList = spellManager.listSpells(player);

        return new ByteArray(SUCCESS).put(spellList);
    }

    @ServerAction(opCode = SPELL_CAST, authRequired = true)
    public ByteArray spellCast(ByteWrapper request, ServerSession session) throws Exception {
        WorldPlayer player = ((WorldSession) session).getPlayer();
        WorldMap map = worldMapManager.getMap(player.getPosition());

        Spell spell = spellManager.getSpell(request.getInt(), player);

        if (Objects.nonNull(spell)) {
            if (player.hasCoolDown(spell.getId())) {
                return new ByteArray(COOLDOWN).put(player.getCoolDown(spell.getId()));
            }

            return switch (spell.getType()) {
                case SELF -> spellCastSelf(spell, player);
                case SINGLE_TARGET -> spellCastSingleTarget(map, spell, player, request);
                case AREA_OF_EFFECT -> spellCastAreaOfEffect(map, spell, player, request);
            };
        }

        return new ByteArray(NOT_EXISTS);
    }

    private ByteArray spellCastSelf(Spell spell, WorldObject player) throws ScriptException {
        spell.cast(ctx, player, Collections.singletonList(player));

        return new ByteArray(SUCCESS);
    }

    private ByteArray spellCastSingleTarget(WorldMap map, Spell spell, WorldPlayer player, ByteWrapper target) throws ScriptException {
        WorldObject targetObject = map.findObjectById(target.getOID());

        float spellDistance = (float) player.processEffects(CastDirection.OFFENSE, CastAttribute.RANGE, spell.getDistance(), spell.getDamageType());

        if (Objects.nonNull(targetObject)&&(MathUtils.calculateDistance(player.getPosition(), targetObject.getPosition()) <= spellDistance)) {
            spell.cast(ctx, player, Collections.singletonList(targetObject));

            return new ByteArray(SUCCESS);
        }

        return new ByteArray(OUT_OF_RANGE);
    }

    private ByteArray spellCastAreaOfEffect(WorldMap map, Spell spell, WorldPlayer player, ByteWrapper target) throws ScriptException {
        WorldPosition targetPosition = new WorldPosition(
            map.getLocation().getId(),
            target.getFloat(),
            target.getFloat(),
            target.getFloat(),
            0f
        );

        float spellDistance = (float) player.processEffects(CastDirection.OFFENSE, CastAttribute.RANGE, spell.getDistance(), spell.getDamageType());
        float spellRadius = (float) player.processEffects(CastDirection.OFFENSE, CastAttribute.RADIUS, spell.getRadius(), spell.getDamageType());

        if (MathUtils.calculateDistance(player.getPosition(), targetPosition) <= spellDistance) {
            List<WorldObject> targetList = map.findObjectsInArea(targetPosition, spellRadius);

            spell.cast(ctx, player, targetList);
            return new ByteArray(SUCCESS);
        }

        return new ByteArray(OUT_OF_RANGE);
    }

    // Chat

    @ServerAction(opCode = CHAT_MESSAGE, authRequired = true)
    public ByteArray chatMessageSend(ByteWrapper request, ServerSession session) throws Exception {
        ChatMessageType messageType = ChatMessageType.valueOf(request.getString().toUpperCase());

        String targetName = request.getString();
        String message = request.getString();

        WorldPlayer player = ((WorldSession) session).getPlayer();

        switch (messageType) {
            case LOCAL:
                chatManager.sendLocalMessage(player, message);

                return new ByteArray(SUCCESS).put(messageType).put(message);
            case BROADCAST:
                chatManager.sendBroadcastMessage(player, message);

                return new ByteArray(SUCCESS).put(messageType).put(message);
            case PRIVATE:
                WorldPlayer target = sessionList().stream()
                    .map(worldSession -> ((WorldSession) worldSession).getPlayer())
                    .filter(worldPlayer -> Objects.nonNull(worldPlayer) && worldPlayer.getName().equals(targetName))
                    .findFirst().orElse(null);

                if (Objects.isNull(target)) {
                    return new ByteArray(NOT_EXISTS);
                }

                chatManager.sendPrivateMessage(player, target, message);

                return new ByteArray(SUCCESS).put(messageType).put(message);
            case PARTY:
                return new ByteArray(NOT_EXISTS);
            case GUILD:
                Guild guild = guildManager.getPlayerGuild(player.getCharacterInfo().getId());

                if (Objects.isNull(guild)) {
                    return new ByteArray(NOT_EXISTS);
                }

                for (GuildMember guildMember: guildManager.getGuildPlayers(guild.getId())) {
                    CharacterInfo guildPlayer = guildMember.getCharacter();

                    WorldPlayer targetMember = sessionList().stream()
                        .map(worldSession -> ((WorldSession) worldSession).getPlayer())
                        .filter(worldPlayer -> Objects.nonNull(worldPlayer) && worldPlayer.getName().equals(String.format("%s %s", guildPlayer.getFirstName(), guildPlayer.getLastName())))
                        .findFirst().orElse(null);

                    if (Objects.nonNull(targetMember)) {
                        chatManager.sendGuildMessage(player, targetMember, message);
                    }
                }

                return new ByteArray(SUCCESS).put(messageType).put(message);
            case ANNOUNCE:
                if (AccountLevel.isGameMaster(session.getAccount().getAccessLevel())) {
                    chatManager.sendAnnounce(message);
                    return new ByteArray(SUCCESS).put(messageType).put(message);
                } else {
                    return new ByteArray(AUTH_ERROR);
                }
        }

        return new ByteArray(INVALID_REQUEST);
    }

    // Guild management

    @ServerAction(opCode = GUILD_CREATE, authRequired = true)
    public ByteArray guildCreate(ByteWrapper request, ServerSession session) throws Exception {
        CharacterInfo player = ((WorldSession) session).getPlayer().getCharacterInfo();
        Guild playerGuild = guildManager.getPlayerGuild(player.getId());

        if (Objects.nonNull(playerGuild)) {
            return new ByteArray(COOLDOWN);
        }

        String title = request.getString();
        String tag = request.getString();
        String description = request.getString();

        playerGuild = guildManager.createGuild(title, tag, description, player);

        if (Objects.nonNull(playerGuild)) {
            return new ByteArray(SUCCESS).put(playerGuild.getId());
        } else {
            return new ByteArray(INVALID_REQUEST);
        }
    }

    @ServerAction(opCode = GUILD_INVITE, authRequired = true)
    public ByteArray guildInvite(ByteWrapper request, ServerSession session) throws Exception {
        String targetName = request.getString();

        WorldPlayer player = ((WorldSession) session).getPlayer();
        Guild guild = guildManager.getPlayerGuild(player.getCharacterInfo().getId());

        if (Objects.isNull(guild)) {
            return new ByteArray(INVALID_REQUEST);
        }

        WorldPlayer target = sessionList().stream()
            .map(worldSession -> ((WorldSession) worldSession).getPlayer())
            .filter(worldPlayer -> Objects.nonNull(worldPlayer) && worldPlayer.getName().equals(targetName))
            .findFirst().orElse(null);

        if (Objects.isNull(target)) {
            return new ByteArray(NOT_EXISTS);
        }

        if (Objects.nonNull(guildManager.getPlayerGuild(target.getCharacterInfo().getId()))) {
            return new ByteArray(COOLDOWN);
        }

        inviteManager.sendInvite(InviteType.GUILD, player, target, new ByteArray().put(guild.getId()).put(guild.getTitle()));

        return new ByteArray(SUCCESS);
    }

    @ServerAction(opCode = GUILD_LEAVE, authRequired = true)
    public ByteArray guildLeave(ByteWrapper request, ServerSession session) throws Exception {
        WorldPlayer player = ((WorldSession) session).getPlayer();
        Guild guild = guildManager.getPlayerGuild(player.getCharacterInfo().getId());

        if (Objects.isNull(guild)) {
            return new ByteArray(INVALID_REQUEST);
        }

        if (guildManager.getGuildMaster(guild.getId()).getId() == player.getCharacterInfo().getId()) {
            if (guildManager.getGuildPlayers(guild.getId()).size() == 1) {
                guildManager.removeGuildMember(player.getCharacterInfo());
                guildManager.removeGuild(guild.getId());

                return new ByteArray(SUCCESS);
            } else {
                return new ByteArray(COOLDOWN);
            }
        } else {
            guildManager.removeGuildMember(player.getCharacterInfo());

            return new ByteArray(SUCCESS);
        }
    }

    @ServerAction(opCode = GUILD_PROMOTE, authRequired = true)
    public ByteArray guildPromote(ByteWrapper request, ServerSession session) throws Exception {
        WorldPlayer player = ((WorldSession) session).getPlayer();
        Guild guild = guildManager.getPlayerGuild(player.getCharacterInfo().getId());

        if (Objects.isNull(guild)) {
            return new ByteArray(INVALID_REQUEST);
        }

        int playerLevel = guildManager.getPlayerLevel(guild, player.getCharacterInfo());

        int targetPlayer = request.getInt();
        int targetLevel = request.getInt();

        CharacterInfo targetPlayerCharacter = characterManager.get(targetPlayer);

        if (playerLevel == -1) {
            return new ByteArray(AUTH_ERROR);
        }

        if (targetPlayer == player.getCharacterInfo().getId()) {
            return new ByteArray(INVALID_REQUEST);
        }

        if ((targetLevel == 0) || (targetLevel < -1)) {
            return new ByteArray(INVALID_REQUEST);
        }

        if ((playerLevel < targetLevel) || (targetLevel == -1)) {
            guildManager.setPlayerLevel(guild, targetPlayerCharacter, targetLevel);

            return new ByteArray(SUCCESS);
        } else if (playerLevel == 1) {
            guildManager.setPlayerLevel(guild, targetPlayerCharacter, 1);
            guildManager.setPlayerLevel(guild, player.getCharacterInfo(), 2);

            return new ByteArray(SUCCESS);
        } else {
            return new ByteArray(AUTH_ERROR);
        }
    }

    @ServerAction(opCode = GUILD_INFO, authRequired = true)
    public ByteArray guildInfo(ByteWrapper request, ServerSession session) throws Exception {
        int guildId = request.getInt();

        Guild guild;
        if (guildId == -1) {
            WorldPlayer player = ((WorldSession) session).getPlayer();
            guild = guildManager.getPlayerGuild(player.getCharacterInfo().getId());
        } else {
            guild = guildManager.getGuild(guildId);
        }

        if (Objects.isNull(guild)) {
            return new ByteArray(NOT_EXISTS);
        }

        List<GuildMember> guildMembers = guildManager.getGuildPlayers(guild.getId());

        return new ByteArray(SUCCESS)
            .put(guild)
            .put(guildMembers);
    }

    // Invite system

    @ServerAction(opCode = INVITE_RESPOND, authRequired = true)
    public ByteArray inviteRespond(ByteWrapper request, ServerSession session) throws Exception {
        long inviteId = request.getLong();
        boolean inviteAccepted = request.getBoolean();

        boolean result = inviteManager.respondToInvite(inviteId, inviteAccepted, ((WorldSession) session).getPlayer());

        return new ByteArray(result ? SUCCESS : NOT_EXISTS);
    }

    // Admin functions

    @ServerAction(opCode = SCRIPT_LIST, authRequired = true, minLevel = AccountLevel.GAME_MASTER)
    public ByteArray scriptList(ByteWrapper request, ServerSession session) throws Exception {
        List<Script> scripts = scriptManager.listScripts();

        return new ByteArray(SUCCESS).put(
            scripts.stream()
                .map(script -> new ByteArray().put(script.getId()).put(script.getName()).put(script.getLanguage()))
                .collect(Collectors.toList())
        );
    }

    @ServerAction(opCode = SCRIPT_LANGUAGE_LIST, authRequired = true, minLevel = AccountLevel.GAME_MASTER)
    public ByteArray scriptLanguageList(ByteWrapper request, ServerSession session) {
        List<String> languages = scriptManager.getAvailableLanguages();

        return new ByteArray(SUCCESS).put(
            languages.stream().map(language -> new ByteArray().put(language)).collect(Collectors.toList())
        );
    }

    @ServerAction(opCode = SCRIPT_GET, authRequired = true, minLevel = AccountLevel.GAME_MASTER)
    public ByteArray scriptGet(ByteWrapper request, ServerSession session) throws Exception {
        Script script = scriptManager.getScript(request.getInt());

        if (Objects.nonNull(script)) {
            return new ByteArray(SUCCESS)
                .put(script);
        } else {
            return new ByteArray(NOT_EXISTS);
        }
    }

    @ServerAction(opCode = SCRIPT_VALIDATE, authRequired = true, minLevel = AccountLevel.GAME_MASTER)
    public ByteArray scriptValidate(ByteWrapper request, ServerSession session) {
        Script script = new Script();
        script.setLanguage(request.getString());
        script.setScript(request.getString());

        if (!scriptManager.getAvailableLanguages().contains(script.getLanguage())) {
            return new ByteArray(NOT_EXISTS);
        }

        ScriptValidationResult result = scriptManager.validateScript(script);
        if (result.isValid()) {
            return new ByteArray(SUCCESS);
        } else {
            return new ByteArray(INVALID_SCRIPT)
                .put(result.getLine())
                .put(result.getColumn())
                .put(result.getMessage());
        }
    }

    @ServerAction(opCode = SCRIPT_SAVE, authRequired = true, minLevel = AccountLevel.GAME_MASTER)
    public ByteArray scriptSave(ByteWrapper request, ServerSession session) throws Exception {
        ScriptValidationResult result = scriptManager.updateScript(request.getInt(), request.getString(), request.getString());
        if (result.isValid()) {
            return new ByteArray(SUCCESS);
        } else {
            return new ByteArray(INVALID_SCRIPT)
                .put(result.getLine())
                .put(result.getColumn())
                .put(result.getMessage());
        }
    }

    // Log Out

    @ServerAction(opCode = LOG_OUT, authRequired = true)
    public ByteArray logOut(ByteWrapper request, ServerSession session) throws Exception {
        sessionManager.kill(session.getAccount());

        return new ByteArray(SUCCESS);
    }

    // Heartbeat

    @ServerAction(opCode = HEART_BEAT)
    public ByteArray heartBeat(ByteWrapper request, ServerSession session) {
        return new ByteArray(SUCCESS).put(request.getLong());
    }

    // Internal commands

    @InternalCommand(command = "help", description = "Show available command list", level = AccountLevel.USER)
    public ByteArray help(String[] args, ServerSession session) throws SQLException {
        StringBuilder result = new StringBuilder("Internal commands: \n");

        for (String command: internalCommands.keySet()) {
            InternalCommand internalCommandInfo = internalCommands.get(command).getAnnotation(InternalCommand.class);

            if (AccountLevel.hasAccess(session.getAccount().getAccessLevel(), internalCommandInfo.level())) {
                result.append(String.format(
                    "%s: %s (%s)\n", command, internalCommandInfo.description(), internalCommandInfo.level()
                ));
            }
        }

        result.append("\n External commands:\n");

        for (Command command: scriptManager.listCommands()) {
            if (AccountLevel.hasAccess(session.getAccount().getAccessLevel(), command.getLevel())) {
                result.append(String.format(
                    "%s: %s (%s)\n", command.getName(), command.getDescription(), command.getLevel()
                ));
            }
        }

        return new ByteArray(SUCCESS).put(result.toString());
    }

    @InternalCommand(command = "setServerTime", description = "Set server time to given value (HH:MM[:SS])", level = AccountLevel.ADMIN)
    public ByteArray setServerTime(String[] args, ServerSession session) {
        if (args.length < 1) {
            return new ByteArray(SERVER_ERROR);
        }

        String[] timeParts = args[0].split(":");

        if ((timeParts.length < 2) || (timeParts.length > 3)) {
            return new ByteArray(SERVER_ERROR);
        }

        long hours = Long.parseLong(timeParts[0]) * 3600 * 1000;
        long minutes = Long.parseLong(timeParts[1]) * 60 * 1000;
        long seconds = (timeParts.length == 3 ? Long.parseLong(timeParts[2]) : 0) * 1000;

        long newServerTime = hours + minutes + seconds;

        worldTimer.setServerTime(worldTimer.getServerDay(), newServerTime, worldTimer.getServerTimeRate());

        return new ByteArray(SUCCESS);
    }

    @InternalCommand(command = "setServerDay", description = "Set server day to given value", level = AccountLevel.ADMIN)
    public ByteArray setServerDay(String[] args, ServerSession session) {
        if (args.length < 1) {
            return new ByteArray(SERVER_ERROR);
        }

        int newServerDay = Integer.parseInt(args[0]);

        worldTimer.setServerTime(newServerDay, worldTimer.getServerTime(), worldTimer.getServerTimeRate());

        return new ByteArray(SUCCESS);
    }

    @InternalCommand(command = "setServerTimeRate", description = "Set server time rate to given value", level = AccountLevel.ADMIN)
    public ByteArray setServerTimeRate(String[] args, ServerSession session) {
        if (args.length < 1) {
            return new ByteArray(SERVER_ERROR);
        }

        int newServerTimeRate = Integer.parseInt(args[0]);

        worldTimer.setServerTime(worldTimer.getServerDay(), worldTimer.getServerTime(), newServerTimeRate);

        return new ByteArray(SUCCESS);
    }

    public void onServerTimeChange(int serverDay, long serverTime, int serverTimeRate) {
        ByteArray timeChangeEvent = new ByteArray()
            .put(serverDay)
            .put(serverTime)
            .put(serverTimeRate);

        sessionList().parallelStream()
            .map(worldSession -> (WorldSession) worldSession)
            .forEach(
                worldSession -> worldSession.onEvent(
                    WorldEventType.TIME_CHANGE, new ByteArray()
                        .put(WorldObject.WORLD.getAttributes())
                        .put(timeChangeEvent)
                )
            );
    }

    // Internal methods

    public void update(Long diff) {
        worldMapManager.update(diff);
        sessionList().parallelStream()
            .forEach(session -> ((WorldSession) session).pushEvents());
    }

    @Override
    protected void onShutdown() {
        // Custom shutdown handling is not required
    }
}
