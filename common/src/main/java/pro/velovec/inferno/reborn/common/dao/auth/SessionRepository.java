package pro.velovec.inferno.reborn.common.dao.auth;

import org.springframework.data.repository.CrudRepository;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public interface SessionRepository extends CrudRepository<Session, Integer> {
    Session findBySessionKey(byte[] key);

    Session findByAccount(Account account);

    Session findByAddress(String toString);

    List<Session> findByLastActivityBefore(Date expiration);
}
