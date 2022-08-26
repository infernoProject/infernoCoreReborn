package pro.velovec.inferno.reborn.common.dao.auth;

import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface AccountBanRepository extends CrudRepository<AccountBan, Integer> {
    AccountBan findByAccount(Account account);

    List<AccountBan> findAllByExpiresBefore(Date date);
}
