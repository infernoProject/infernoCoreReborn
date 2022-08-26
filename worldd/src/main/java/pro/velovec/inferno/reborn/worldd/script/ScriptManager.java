package pro.velovec.inferno.reborn.worldd.script;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import pro.velovec.inferno.reborn.worldd.dao.script.Command;
import pro.velovec.inferno.reborn.worldd.dao.script.CommandRepository;
import pro.velovec.inferno.reborn.worldd.dao.script.Script;
import pro.velovec.inferno.reborn.worldd.dao.script.ScriptRepository;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ScriptManager {

    @Autowired
    private ScriptRepository scriptRepository;

    @Autowired
    private CommandRepository commandRepository;

    private final ScriptEngineManager engineManager = new ScriptEngineManager();

    public ScriptableObject eval(Script script) throws ScriptException {
        ScriptEngine engine = getScriptEngineForLanguage(script.getLanguage());

        engine.eval(script.getScript());

        Object result = engine.get("sObject");
        if ((result == null) || !ScriptableObject.class.isAssignableFrom(result.getClass()))
            throw new ScriptException("Script should provide ScriptableObject with name 'sObject'");

        return (ScriptableObject) result;
    }

    private ScriptEngine getScriptEngineForLanguage(String language) {
        Optional<ScriptEngineFactory> engineFactoryOptional = engineManager.getEngineFactories().stream()
            .filter(factory -> factory.getLanguageName().equals(language))
            .findFirst();

        if (!engineFactoryOptional.isPresent()) {
            throw new IllegalStateException(String.format(
                "No engines available for language: %s", language
            ));
        }

        return engineFactoryOptional.get().getScriptEngine();
    }

    public ScriptValidationResult validateScript(Script script) {
        try {
            ScriptableObject object = eval(script);

            if (object == null)
                return new ScriptValidationResult("Script should define ScriptableObject with name 'sObject'");
        } catch (ScriptException e) {
            return new ScriptValidationResult(e);
        }

        return new ScriptValidationResult();
    }

    public List<String> getAvailableLanguages() {
        return engineManager.getEngineFactories().stream()
            .map(ScriptEngineFactory::getLanguageName)
            .collect(Collectors.toList());
    }

    public List<Script> listScripts() throws SQLException {
        return scriptRepository.findAll();
    }

    public Script getScript(int id) throws SQLException {
        return scriptRepository.findById(id).orElse(null);
    }

    public ScriptValidationResult updateScript(int id, String lang, String script) throws SQLException {
        if (!getAvailableLanguages().contains(lang))
            return new ScriptValidationResult("Script language is not supported");

        Script scriptData = getScript(id);

        if (scriptData != null) {
            scriptData.setLanguage(lang);
            scriptData.setScript(script);

            ScriptValidationResult result = validateScript(scriptData);
            if (result.isValid()) {
                scriptRepository.save(scriptData);
            }

            return result;
        }

        return new ScriptValidationResult("Script doesn't exist");
    }

    public Command getCommand(String command) throws SQLException {
        return commandRepository.findByName(command);
    }
}
