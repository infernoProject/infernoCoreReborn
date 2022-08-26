package pro.velovec.inferno.reborn.worldd.script.impl;


import org.springframework.context.ConfigurableApplicationContext;

import pro.velovec.inferno.reborn.common.dao.auth.Session;
import pro.velovec.inferno.reborn.common.utils.ByteArray;
import pro.velovec.inferno.reborn.worldd.script.ScriptableObject;

@FunctionalInterface
public interface CommandBase extends ScriptableObject {

    ByteArray execute(ConfigurableApplicationContext ctx, Session session, String[] args);
}
