package pro.velovec.inferno.reborn.worldd.world;

import pro.velovec.inferno.reborn.common.utils.ByteConvertible;

public interface WorldNotificationListener {

    void onEvent(short type, ByteConvertible data);

}
