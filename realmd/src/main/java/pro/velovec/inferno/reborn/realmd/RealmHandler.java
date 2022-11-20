package pro.velovec.inferno.reborn.realmd;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.context.ConfigurableApplicationContext;
import pro.velovec.inferno.reborn.common.character.CharacterManager;
import pro.velovec.inferno.reborn.common.dao.auth.Account;
import pro.velovec.inferno.reborn.common.dao.auth.AccountBan;
import pro.velovec.inferno.reborn.common.dao.auth.Session;
import pro.velovec.inferno.reborn.common.dao.character.CharacterData;
import pro.velovec.inferno.reborn.common.dao.character.CharacterInfo;
import pro.velovec.inferno.reborn.common.dao.data.GenderInfo;
import pro.velovec.inferno.reborn.common.dao.data.RaceInfo;
import pro.velovec.inferno.reborn.common.dao.map.Location;
import pro.velovec.inferno.reborn.common.dao.map.LocationRepository;
import pro.velovec.inferno.reborn.common.dao.realmlist.RealmListEntry;
import pro.velovec.inferno.reborn.common.server.ServerAction;
import pro.velovec.inferno.reborn.common.server.ServerHandler;
import pro.velovec.inferno.reborn.common.server.ServerSession;
import pro.velovec.inferno.reborn.common.utils.ByteArray;
import pro.velovec.inferno.reborn.common.utils.ByteWrapper;


import java.net.SocketAddress;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static pro.velovec.inferno.reborn.common.constants.CommonErrorCodes.*;
import static pro.velovec.inferno.reborn.realmd.constants.RealmErrorCodes.*;
import static pro.velovec.inferno.reborn.realmd.constants.RealmOperations.*;


@ChannelHandler.Sharable
public class RealmHandler extends ServerHandler {

    private final LocationRepository locationRepository;

    public RealmHandler(ConfigurableApplicationContext ctx) {
        super(ctx);

        locationRepository = ctx.getBean(LocationRepository.class);
    }

    @ServerAction(opCode = CRYPTO_CONFIG)
    public ByteArray cryptoConfigGet(ByteWrapper request, ServerSession session) throws Exception {
        return new ByteArray(SUCCESS).put(accountManager.serverSalt());
    }

    @ServerAction(opCode = SIGN_UP)
    public ByteArray signUp(ByteWrapper request, ServerSession session) throws Exception {
        String login = request.getString();
        String email = request.getString();

        byte[] salt = request.getBytes();
        byte[] verifier = request.getBytes();
        
        Account account = accountManager.create(login, email, salt, verifier);

        if (account != null) {
            session.setAccount(account);

            return new ByteArray(SUCCESS);
        } else {
            return new ByteArray(ALREADY_EXISTS);
        }
    }

    @ServerAction(opCode = LOG_IN_STEP1)
    public ByteArray logInStep1(ByteWrapper request, ServerSession serverSession) throws Exception {
        String login = request.getString();

        Session session = accountManager.logInStep1(serverSession.address(), login);

        if (session != null) {
            return new ByteArray(SUCCESS)
                .put(session.getKey())
                .put(session.getAccount().getSalt())
                .put(session.getVector());
        } else {
            return new ByteArray(AUTH_ERROR);
        }
    }

    @ServerAction(opCode = LOG_IN_STEP2)
    public ByteArray logInStep2(ByteWrapper request, ServerSession serverSession) throws Exception {
        try {
            Session session = sessionManager.get(
                request.getBytes()
            );

            AccountBan ban = accountManager.checkBan(session.getAccount());
            if (ban != null) {
                return new ByteArray(USER_BANNED)
                    .put(ban.getReason())
                    .put(ban.getExpires());
            }

            if (accountManager.logInStep2(session, request.getBytes())) {
                serverSession.setAuthorized(true);
                serverSession.setAccount(session.getAccount());

                return new ByteArray(SUCCESS);
            } else {
                return new ByteArray(AUTH_INVALID);
            }
        } catch (NoSuchAlgorithmException e) {
            return new ByteArray(AUTH_ERROR);
        }
    }

    @ServerAction(opCode = SESSION_TOKEN, authRequired = true)
    public ByteArray sessionTokenGet(ByteWrapper request, ServerSession serverSession) throws Exception {
        Session session = sessionManager.get(serverSession.getAccount());

        return new ByteArray(SUCCESS).put(session.getKey());
    }

    @ServerAction(opCode = REALM_LIST, authRequired = true)
    public ByteArray realmListGet(ByteWrapper request, ServerSession session) throws Exception {
        List<RealmListEntry> realmServerList = realmList.list();

        return new ByteArray(SUCCESS).put(realmServerList);
    }

    @ServerAction(opCode = RACE_LIST, authRequired = true)
    public ByteArray raceListGet(ByteWrapper request, ServerSession session) throws Exception {
        List<RaceInfo> raceList = dataManager.raceList();

        return new ByteArray(SUCCESS).put(raceList);
    }

    @ServerAction(opCode = CHARACTER_LIST, authRequired = true)
    public ByteArray characterListGet(ByteWrapper request, ServerSession session) throws Exception {
        List<ByteArray> charactersData = new ArrayList<>();
        characterManager.list(session.getAccount()).forEach(characterInfo -> {
            CharacterData characterData = characterManager.getCharacterData(characterInfo);
            Location characterLocation = locationRepository.findById(characterData.getLocation()).orElse(null);

            ByteArray characterDataArray = new ByteArray();

            characterDataArray.put(characterInfo);
            characterDataArray.put(characterData);
            characterDataArray.put(characterLocation);

            charactersData.add(characterDataArray);
        });

        return new ByteArray(SUCCESS).put(charactersData);
    }

    @ServerAction(opCode = CHARACTER_CREATE, authRequired = true)
    public ByteArray characterCreate(ByteWrapper request, ServerSession session) throws Exception {
        CharacterInfo characterInfo = new CharacterInfo();
        characterInfo.setRealm(realmList.get(request.getInt()));
        characterInfo.setAccount(session.getAccount());

        characterInfo.setFirstName(request.getString());
        characterInfo.setLastName(request.getString());

        characterInfo.setGender(request.getEnum(GenderInfo.class));
        characterInfo.setRaceInfo(dataManager.raceGetById(request.getInt()));

        characterInfo.setBody(request.getBytes());

        ByteWrapper characterStats = request.getWrapper();

        CharacterData characterData = CharacterData.fromStats(characterStats);
        if (!CharacterManager.validateInitialStats(characterData)) {
            return new ByteArray(SERVER_ERROR);
        }

        int characterId = characterManager.create(characterInfo, characterData);
        if (characterId > 0) {
            return new ByteArray(SUCCESS).put(characterId);
        } else {
            return new ByteArray(CHARACTER_EXISTS);
        }
    }

    @ServerAction(opCode = CHARACTER_SELECT, authRequired = true)
    public ByteArray characterSelect(ByteWrapper request, ServerSession session) throws Exception {
        CharacterInfo characterInfo = characterManager.get(request.getInt());

        if ((characterInfo == null) || (characterInfo.getAccount().getId() != session.getAccount().getId()))
            return new ByteArray(CHARACTER_NOT_FOUND);

        Session playerSession = sessionManager.get(session.getAccount());

        playerSession.setCharacterInfo(characterInfo);
        sessionManager.save(playerSession);

        return new ByteArray(SUCCESS);
    }

    @ServerAction(opCode = CHARACTER_DELETE, authRequired = true)
    public ByteArray characterDelete(ByteWrapper request, ServerSession session) throws Exception {
        CharacterInfo characterInfo = characterManager.get(request.getInt());

        if ((characterInfo == null) || (characterInfo.getAccount().getId() != session.getAccount().getId()))
            return new ByteArray(CHARACTER_NOT_FOUND);

        if (characterManager.delete(characterInfo)) {
            return new ByteArray(SUCCESS);
        } else {
            return new ByteArray(CHARACTER_DELETED);
        }
    }

    @ServerAction(opCode = CHARACTER_RESTORABLE_LIST, authRequired = true)
    public ByteArray characterGetRestorableList(ByteWrapper request, ServerSession session) throws Exception {
        List<CharacterInfo> characterList = characterManager.list_deleted(session.getAccount());

        return new ByteArray(SUCCESS).put(characterList);
    }

    @ServerAction(opCode = CHARACTER_RESTORE, authRequired = true)
    public ByteArray characterRestore(ByteWrapper request, ServerSession session) throws Exception {
        CharacterInfo characterInfo = characterManager.get(request.getInt());

        if ((characterInfo == null) || (characterInfo.getAccount().getId() != session.getAccount().getId()))
            return new ByteArray(CHARACTER_NOT_FOUND);

        if (characterManager.restore(characterInfo)) {
            return new ByteArray(SUCCESS);
        } else {
            return new ByteArray(CHARACTER_EXISTS);
        }
    }

    @Override
    protected ServerSession onSessionInit(ChannelHandlerContext ctx, SocketAddress remoteAddress) {
        return new RealmSession(ctx, remoteAddress);
    }

    @Override
    protected void onSessionClose(SocketAddress remoteAddress) {
        // Custom session termination is not required
    }

    @Override
    protected void onShutdown() {
        // Custom shutdown handling is not required
    }
}
