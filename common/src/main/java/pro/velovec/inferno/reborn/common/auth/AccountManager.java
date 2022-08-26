package pro.velovec.inferno.reborn.common.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import pro.velovec.inferno.reborn.common.dao.auth.*;
import pro.velovec.inferno.reborn.common.properties.InfernoCommonProperties;
import pro.velovec.libs.base.utils.HexBin;

import java.net.SocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;

@Component
public class AccountManager {

    @Autowired
    private InfernoCommonProperties commonProperties;
    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountBanRepository accountBanRepository;

    private static final Logger logger = LoggerFactory.getLogger(AccountManager.class);


    public byte[] serverSalt() {
        return HexBin.decode(commonProperties.getCrypto().getSalt());
    }

    public Account create(String login, String email, byte[] salt, byte[] verifier) throws SQLException {
        Account account = get(login);

        if (account == null) {
            account = new Account();

            account.setLogin(login);
            account.setAccessLevel(AccountLevel.USER);
            account.setEmail(email);
            account.setSalt(salt);
            account.setVerifier(verifier);

            return accountRepository.save(account);
        }

        return null;
    }

    public Account get(String login) throws SQLException {
        return accountRepository.findByLogin(login);
    }

    public Session logInStep1(SocketAddress remoteAddress, String login) throws SQLException {
        Account account = get(login);

        if (account != null) {
            return sessionManager.create(account, remoteAddress);
        }

        return null;
    }

    public boolean logInStep2(Session session, byte[] challenge) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");

        messageDigest.update(session.getVector());
        messageDigest.update(session.getAccount().getVerifier());
        messageDigest.update(serverSalt());

        byte[] digest = messageDigest.digest();

        if (logger.isDebugEnabled()) {
            logger.debug("S_SALT: {}", HexBin.encode(serverSalt()));
            logger.debug("VERIFIER: {}", HexBin.encode(session.getAccount().getVerifier()));
            logger.debug("VECTOR: {}", HexBin.encode(session.getVector()));
            logger.debug("CHLG: {} <==> {}", HexBin.encode(digest), HexBin.encode(challenge));
        }

        return Arrays.equals(digest, challenge);
    }

    public AccountBan checkBan(Account account) throws SQLException {
        return accountBanRepository.findByAccount(account);
    }

    public void cleanup() throws SQLException {
        accountBanRepository.deleteAll(
            accountBanRepository.findAllByExpiresBefore(new Date())
        );
    }
}
