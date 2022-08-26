package pro.velovec.inferno.reborn.common.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.velovec.inferno.reborn.common.dao.auth.Account;
import pro.velovec.inferno.reborn.common.dao.auth.Session;
import pro.velovec.inferno.reborn.common.dao.auth.SessionRepository;
import pro.velovec.inferno.reborn.common.properties.InfernoCommonProperties;
import pro.velovec.libs.base.utils.HexBin;

import java.net.SocketAddress;
import java.sql.SQLException;
import java.util.Date;
import java.util.Random;

@Component
public class SessionManager {

    @Autowired
    private InfernoCommonProperties commonProperties;

    @Autowired
    private SessionRepository sessionRepository;

    private static final Random random = new Random();
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);


    public Account authorize(Session givenSession, SocketAddress sessionAddress) throws SQLException {
        Session session = sessionRepository.findBySessionKey(givenSession.getKey());

        if (session != null) {
            session.setAddress(sessionAddress);
            sessionRepository.save(session);

            return session.getAccount();
        }

        return null;
    }

    public Session create(Account account, SocketAddress remoteAddress) throws SQLException {
        kill(account);

        byte[] sessionKey = generateKey();

        Session session = new Session(
            account, sessionKey, remoteAddress
        );

        if (logger.isDebugEnabled()) {
            logger.debug("Session(user={}, session_key={}): created", account.getLogin(), HexBin.encode(sessionKey));
        }

        return sessionRepository.save(session);
    }

    public void cleanup() throws SQLException {
        Date expiration = new Date(System.currentTimeMillis() - commonProperties.getSession().getTtl() * 1000);

        sessionRepository.deleteAll(
            sessionRepository.findByLastActivityBefore(expiration)
        );
    }

    public Session get(byte[] sessionKey) throws SQLException {
        return sessionRepository.findBySessionKey(sessionKey);
    }

    public Session get(Account account) throws SQLException {
        if (account == null)
            return null;

        return sessionRepository.findByAccount(account);
    }

    private byte[] generateKey() {
        byte[] sessionKey = new byte[16];
        random.nextBytes(sessionKey);

        return sessionKey;
    }

    public void kill(Account account) throws SQLException {
        if (account == null)
            return;

        Session session = get(account);

        if (session != null) {
            sessionRepository.delete(session);
        }
    }

    public void kill(Session session) throws SQLException {
        if (session == null)
            return;

        sessionRepository.delete(session);
    }

    public void save(Session session) throws SQLException {
        sessionRepository.save(session);
    }

    public void update(Session session) throws SQLException {
        session.setLastActivity(new Date());
        sessionRepository.save(session);
    }

    public void update(SocketAddress remoteAddress) throws SQLException {
        Session session = sessionRepository.findByAddress(remoteAddress.toString());

        if (session != null) {
            session.setLastActivity(new Date());
            sessionRepository.save(session);
        }
    }
}
