package pro.velovec.inferno.reborn.common.dao.auth;

import org.springframework.data.repository.CrudRepository;

public interface AccountRepository extends CrudRepository<Account, Integer> {
    Account findByLogin(String login);
}
