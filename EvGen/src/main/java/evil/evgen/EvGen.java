package evil.evgen;

import evil.evgen.Commands.SetGenCmd;
import evil.evgen.tasks.GenerateItemsTask;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EvGen extends JavaPlugin {

    private CommandMap commandMap;
    private final Map<String, BukkitTask> tasks = new HashMap<>();
    private EvGen main;
    @Override
    public void onEnable() {
        main = this;
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        File genlocdb = new File(getDataFolder(), "datagen.yml");
        File config = new File(getDataFolder(), "config.yml");
        if (!genlocdb.exists()) {
            getLogger().info("Plik datagen.yml nie znaleziony, tworzenie!");
            saveResource("datagen.yml", false);
        } else {
            getLogger().info("Plik datagen.yml znaleziony, ładowanie!");
        }
        if (!config.exists()) {
            getLogger().info("Plik config.yml nie został znaleziony, tworzenie...");
            saveResource("config.yml", false);
        } else {
            getLogger().info("Plik config.yml znaleziony, ładowanie...");
        }

        File file = new File(main.getDataFolder(), "config.yml");
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        List<String> aliases = configuration.getStringList("commands.evgen.aliases");
        YamlConfiguration yamlGenLocDB = YamlConfiguration.loadConfiguration(genlocdb);
        startGenerators(yamlGenLocDB);
        for (String alias : aliases) {
            SetGenCmd setGenCmd = new SetGenCmd(alias, this);
            getCommandMap().register(alias, setGenCmd);
        }
        SetGenCmd setGenCmd = new SetGenCmd("evgen", this);
        getCommandMap().register("evgen", setGenCmd);
        getServer().getLogger().info("Pomyślnie załadowano plugin");
    }


    private CommandMap getCommandMap() {
        if (commandMap == null) {
            try {
                Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                field.setAccessible(true);
                commandMap = (CommandMap) field.get(Bukkit.getServer());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return commandMap;
    }

    public void startGenerators(YamlConfiguration config) {
        for (BukkitTask task : tasks.values()) {
            task.cancel();
        }
        tasks.clear();

        ConfigurationSection genloc = config.getConfigurationSection("genloc");
        if (genloc != null) {
            for (String generatorName : genloc.getKeys(false)) {
                long delay = config.getLong("genloc." + generatorName + ".delay");
                BukkitTask task = Bukkit.getScheduler().runTaskTimer(main, new GenerateItemsTask(main, generatorName), delay, delay);
                tasks.put(generatorName, task);
            }
        }
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
