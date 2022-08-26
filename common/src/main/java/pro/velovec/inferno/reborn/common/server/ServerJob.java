package pro.velovec.inferno.reborn.common.server;

import java.sql.SQLException;

public interface ServerJob {

    void run() throws SQLException;
}
