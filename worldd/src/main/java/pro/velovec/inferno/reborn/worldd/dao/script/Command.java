package pro.velovec.inferno.reborn.worldd.dao.script;

import org.springframework.context.ConfigurableApplicationContext;

import pro.velovec.inferno.reborn.common.dao.auth.AccountLevel;
import pro.velovec.inferno.reborn.common.dao.auth.Session;
import pro.velovec.inferno.reborn.common.utils.ByteArray;
import pro.velovec.inferno.reborn.worldd.script.ScriptManager;
import pro.velovec.inferno.reborn.worldd.script.impl.CommandBase;

import javax.persistence.*;
import javax.script.ScriptException;

@Entity
@Table(name = "commands") // objects
public class Command {

    @Id
    @GeneratedValue
    private int id;

    private String name;

    private AccountLevel level;

    @ManyToOne(fetch = FetchType.EAGER)
    private Script script;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AccountLevel getLevel() {
        return level;
    }

    public void setLevel(AccountLevel level) {
        this.level = level;
    }

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }

    public ByteArray execute(ConfigurableApplicationContext ctx, Session session, String[] args) throws ScriptException {
        CommandBase commandBase = (CommandBase) ctx.getBean(ScriptManager.class).eval(script);

        return commandBase.execute(ctx, session,  args);
    }
}
