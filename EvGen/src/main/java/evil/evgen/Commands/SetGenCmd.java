package evil.evgen.Commands;

import evil.evgen.EvGen;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SetGenCmd extends Command {

    private final EvGen plugin;
    private final Map<String, BukkitTask> tasks = new HashMap<>();

    public SetGenCmd(@NotNull String name, EvGen plugin) {
        super(name);
        this.plugin = plugin;
    }


    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String arg1, @NotNull String[] args) {
        if (sender instanceof Player player) {
            Block block = player.getTargetBlockExact(3); //Zwraca block, na który patrzy gracz, do 3 bloków

            if (args.length > 0) {
                File file = new File(plugin.getDataFolder(), "datagen.yml");
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                if (args[0].equalsIgnoreCase("setloc")) {
                    if (player.hasPermission("darksmc.setgen")) {
                        if (block == null) {
                            player.sendMessage("§x§F§F§3§8§3§8✖ §cMusisz patrzeć na blok, aby użyć tej komendy");
                            return true;
                        }
                        if (args.length >= 5) {
                            String generatorName = args[1];
                            String generateItem = args[2];
                            Integer ItemAmount = Integer.valueOf(args[3]);
                            long delay = Long.parseLong(args[4]);
                            List<String> itemFlags = new ArrayList<>();
                            List<String> itemLore = new ArrayList<>();
                            List<String> itemEnchants = new ArrayList<>();
                            // Sprawdź, czy generator o tej nazwie już istnieje
                            if (config.getConfigurationSection("genloc." + generatorName) != null) {
                                player.sendMessage("§x§F§F§3§8§3§8✖ §cGenerator o nazwie §4" + generatorName + " §cjuż istnieje");
                                return true;
                            }
                            Location loc = block.getLocation();
                            // Zapisz lokalizację jako nową sekcję w "genloc"
                            config.createSection("genloc." + generatorName);
                            config.set("genloc." + generatorName + ".world", loc.getWorld().getName());
                            config.set("genloc." + generatorName + ".x", loc.getX());
                            config.set("genloc." + generatorName + ".y", loc.getY());
                            config.set("genloc." + generatorName + ".z", loc.getZ());
                            config.set("genloc." + generatorName + ".amount", ItemAmount);
                            config.set("genloc." + generatorName + ".item", generateItem);
                            config.set("genloc." + generatorName + ".name", "");
                            config.set("genloc." + generatorName + ".lore", itemLore);
                            config.set("genloc." + generatorName + ".enchants", itemEnchants);
                            config.set("genloc." + generatorName + ".flags", itemFlags);
                            config.set("genloc." + generatorName + ".delay", delay);
                            try {
                                config.save(file); // Zapisz zmiany w pliku
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            plugin.startGenerators(config);

                            player.sendMessage("§aUstawiono Lokalizację Generatora §2" + generatorName);
                        } else {
                            player.sendMessage("§x§F§F§3§8§3§8✖ §cPoprawne użycie: /evgen setloc <nazwa_generatora> <item> <ilość> <delay w tickach>");
                        }
                    } else {
                        player.sendMessage("§x§F§F§3§8§3§8✖ §cNie masz uprawnień do tej komendy");
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("edit")) {
                    if (player.hasPermission("evgen.edit")) {
                        String generatorName = args[1];
                        if (args[2].equalsIgnoreCase("name")) {
                            if (player.hasPermission("evgen.edit") || player.hasPermission("evgen.edit.name")) {
                                String itemName = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                                config.set("genloc." + generatorName + ".name", itemName);
                                try {
                                    config.save(file);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                player.sendMessage("ustawiono");
                            } else {
                                player.sendMessage("§x§F§F§3§8§3§8✖ §cNie masz uprawnień do tej komendy");
                                return true;
                            }
                        } else if (args[2].equalsIgnoreCase("lore")) {
                            if (player.hasPermission("evgen.edit") || player.hasPermission("evgen.edit.lore")) {
                                if (args[3].equalsIgnoreCase("add")) {
                                    String newLoreLine = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
                                    List<String> currentLore = config.getStringList("genloc." + generatorName + ".lore");
                                    currentLore.add(newLoreLine);
                                    config.set("genloc." + generatorName + ".lore", currentLore);
                                    try {
                                        config.save(file);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    player.sendMessage("ustawiono");
                                } else if (args[3].equalsIgnoreCase("remove")) {
                                    int lineToRemove = Integer.parseInt(args[4]);
                                    List<String> currentLore = config.getStringList("genloc." + generatorName + ".lore");

                                    if (lineToRemove >= 0 && lineToRemove < currentLore.size()) {
                                        currentLore.remove(lineToRemove);
                                        config.set("genloc." + generatorName + ".lore", currentLore);
                                        try {
                                            config.save(file);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        player.sendMessage("ustawiono");
                                    } else {
                                        player.sendMessage("§x§F§F§3§8§3§8✖ §cNieprawidłowy numer linii");
                                        return true;
                                    }
                                } else if (args[3].equalsIgnoreCase("clear")) {
                                    config.set("genloc." + generatorName + ".lore", null);
                                    try {
                                        config.save(file);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    player.sendMessage("ustawiono");
                                } else if (args[3].equalsIgnoreCase("set")) {
                                    int lineToSet = Integer.parseInt(args[4]);
                                    StringBuilder newLoreLine = new StringBuilder();
                                    for (int i = 5; i < args.length; i++) {
                                        newLoreLine.append(args[i]);
                                        if (i != args.length - 1) {
                                            newLoreLine.append(" ");
                                        }
                                    }
                                    List<String> currentLore = config.getStringList("genloc." + generatorName + ".lore");
                                    if (lineToSet >= 0 && lineToSet < currentLore.size()) {
                                        currentLore.set(lineToSet, String.valueOf(newLoreLine));
                                        config.set("genloc." + generatorName + ".lore", currentLore);
                                        try {
                                            config.save(file);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        player.sendMessage("ustawiono");
                                    } else {
                                        player.sendMessage("§x§F§F§3§8§3§8✖ §cNieprawidłowy numer linii");
                                        return true;
                                    }
                                }
                            }
                        } else if (args[2].equalsIgnoreCase("amount")) {
                            if (player.hasPermission("evgen.edit.amount") || player.hasPermission("evgen.edit")) {
                                if (args.length >= 4) {
                                    Integer itemAmount = Integer.parseInt(args[3]);
                                    config.set("genloc." + generatorName + ".amount", itemAmount);
                                    try {
                                        config.save(file);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    player.sendMessage("ustawiono");
                                } else {
                                    player.sendMessage("§x§F§F§3§8§3§8✖ §cPoprawne użycie: /evgen edit <nazwa_generatora> amount <liczba>");
                                }
                            } else {
                                player.sendMessage("§x§F§F§3§8§3§8✖ §cNie masz uprawnień do tej komendy");
                                return true;
                            }
                        } else if (args[2].equalsIgnoreCase("delay")) {
                            if (player.hasPermission("evgen.edit.delay") || player.hasPermission("evgen.edit")) {
                                if (args.length >= 4) {
                                    Integer itemDelay = Integer.parseInt(args[3]);
                                    config.set("genloc." + generatorName + ".delay", itemDelay);
                                    try {
                                        config.save(file);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    plugin.startGenerators(config);
                                    player.sendMessage("ustawiono");
                                } else {
                                    player.sendMessage("§x§F§F§3§8§3§8✖ §cPoprawne użycie: /evgen edit <nazwa_generatora> delay <liczba w tickach>");
                                }
                            } else {
                                player.sendMessage("§x§F§F§3§8§3§8✖ §cNie masz uprawnień do tej komendy");
                                return true;
                            }
                        } else if (args[2].equalsIgnoreCase("enchant")) {
                            if (player.hasPermission("evgen.edit.enchants") || player.hasPermission("evgen.edit")) {
                                if (args.length > 3) {
                                    if (args[3].equalsIgnoreCase("add")) {
                                        if (args.length >= 6) {
                                            String enchantName = args[4].toUpperCase();
                                            Integer enchantLevel = Integer.parseInt(args[5]);
                                            List<String> currentEnchants = config.getStringList("genloc." + generatorName + ".enchants");

                                            for (int i = 0; i < currentEnchants.size(); i++) {
                                                if (currentEnchants.get(i).split(":")[0].equalsIgnoreCase(enchantName)) {
                                                    currentEnchants.remove(i);
                                                    break;
                                                }
                                            }
                                            currentEnchants.add(enchantName + ":" + enchantLevel);
                                            config.set("genloc." + generatorName + ".enchants", currentEnchants);
                                            try {
                                                config.save(file);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            player.sendMessage("ustawiono");
                                        } else {
                                            player.sendMessage("§x§F§F§3§8§3§8✖ §cPoprawne użycie: /evgen edit <nazwa_generatora> enchant add <nazwa_enchantu> <poziom_enchantu>");
                                        }
                                    } else if (args[3].equalsIgnoreCase("remove")) {
                                        if (args.length >= 5) {
                                            String enchantToRemove = args[4];
                                            List<String> currentEnchants = config.getStringList("genloc." + generatorName + ".enchants");
                                            for (int i = 0; i < currentEnchants.size(); i++) {
                                                if (currentEnchants.get(i).split(":")[0].equalsIgnoreCase(enchantToRemove)) {
                                                    currentEnchants.remove(i);
                                                    break;
                                                }
                                            }

                                            config.set("genloc." + generatorName + ".enchants", currentEnchants);
                                            try {
                                                config.save(file);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            player.sendMessage("ustawiono");
                                        } else {
                                            player.sendMessage("§x§F§F§3§8§3§8✖ §cPoprawne użycie: /evgen edit <nazwa_generatora> enchant remove <numer_lini>");
                                        }

                                    } else if (args[3].equalsIgnoreCase("clear")) {
                                        config.set("genloc." + generatorName + ".enchants", null);
                                        try {
                                            config.save(file);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        player.sendMessage("ustawiono");
                                    }
                                } else {
                                    player.sendMessage("nie prawidłowo");
                                }
                            }
                        } else if (args[2].equalsIgnoreCase("flags")) {
                            if (player.hasPermission("evgen.edit.flags") || player.hasPermission("evgen.edit")) {
                                if (args[3].equalsIgnoreCase("add")) {
                                    if (args.length >= 5) {
                                        String flagName = args[4];
                                        List<String> currentFlags = config.getStringList("genloc." + generatorName + ".flags");
                                        for (int i = 0; i < currentFlags.size(); i++) {
                                            if (currentFlags.get(i).equalsIgnoreCase(flagName)) {
                                                currentFlags.remove(i);
                                                break;
                                            }
                                        }

                                        currentFlags.add(flagName);
                                        config.set("genloc." + generatorName + ".flags", currentFlags);
                                        try {
                                            config.save(file);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        player.sendMessage("ustawiono");
                                    } else {
                                        player.sendMessage("§x§F§F§3§8§3§8✖ §cPoprawne użycie: /evgen edit <nazwa_generatora> enchant add <nazwa_flagi>");
                                    }
                                } else if (args[3].equalsIgnoreCase("remove")) {
                                    if (args.length >= 5) {
                                        String flagName = args[4];
                                        List<String> currentFlags = config.getStringList("genloc." + generatorName + ".flags");

                                        for (int i = 0; i < currentFlags.size(); i++) {
                                            if (currentFlags.get(i).equalsIgnoreCase(flagName)) {
                                                currentFlags.remove(i);
                                                break;
                                            }
                                        }
                                        config.set("genloc." + generatorName + ".flags", currentFlags);
                                        try {
                                            config.save(file);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        player.sendMessage("ustawiono");
                                    } else {
                                        player.sendMessage("§x§F§F§3§8§3§8✖ §cPoprawne użycie: /evgen edit <nazwa_generatora> enchant remove <nazwa_flagi>");
                                    }
                                } else if (args[3].equalsIgnoreCase("clear")) {
                                    config.set("genloc." + generatorName + ".flags", null);
                                    try {
                                        config.save(file);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    player.sendMessage("ustawiono");
                                }
                            }
                        }
                    } else {
                        player.sendMessage("§x§F§F§3§8§3§8✖ §cNie masz uprawnień do tej komendy");
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("reload")) {
                    if (player.hasPermission("evgen.reload")) {
                        try {
                            config.load(file);
                            config.save(file);
                        } catch (IOException | InvalidConfigurationException e) {
                            throw new RuntimeException(e);
                        }
                        player.sendMessage("§aKonfiguracja została przeładowana!");
                        plugin.startGenerators(config);
                    } else {
                        player.sendMessage("§x§F§F§3§8§3§8✖ §cNie masz uprawnień do tej komendy");
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("remove")) {
                    if (player.hasPermission("evgen.remove")) {
                        if (args.length >= 2) {
                            String generatorName = args[1];
                            if (config.getConfigurationSection("genloc." + generatorName) == null) {
                                player.sendMessage("§x§F§F§3§8§3§8✖ §cGenerator o nazwie §4\" + generatorName + \" §cnie istnieje");
                                return true;
                            }
                            config.set("genloc." + generatorName, null);
                            try {
                                config.save(file);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            BukkitTask existingTask = tasks.get(generatorName);
                            if (existingTask != null) {
                                existingTask.cancel();
                                tasks.remove(generatorName);
                            }

                            player.sendMessage("§aUsunięto Generator §2" + generatorName);
                        } else {
                            player.sendMessage("§x§F§F§3§8§3§8✖ §cPoprawne użycie: /evgen remove <nazwa_generatora>");
                        }
                    } else {
                        player.sendMessage("§x§F§F§3§8§3§8✖ §cNie masz uprawnień do tej komendy");
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("list")) {
                    if (player.hasPermission("evgen.list")) {
                        ConfigurationSection section = config.getConfigurationSection("genloc");
                        if (section != null) {
                            Set<String> generatorNames = section.getKeys(false);
                            String joinedNames = String.join("§a, §r§2", generatorNames);
                            player.sendMessage("§aLista generatorów: §2" + joinedNames);
                        } else {
                            player.sendMessage("§x§F§F§3§8§3§8✖ §cBrak zdefiniowanych generatorów");
                        }
                    } else {
                        player.sendMessage("§x§F§F§3§8§3§8✖ §cNie masz uprawnień do tej komendy");
                    }
                }
            }
        }
        return false;
    }

    @Override
    public @NotNull List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            if ("setloc".startsWith(args[0].toLowerCase())) {
                completions.add("setloc");
            }
            if ("edit".startsWith(args[0].toLowerCase())) {
                completions.add("edit");
            }
            if ("reload".startsWith(args[0].toLowerCase())) {
                completions.add("reload");
            }
            if ("remove".startsWith(args[0].toLowerCase())) {
                completions.add("remove");
            }
            if ("list".startsWith(args[0].toLowerCase())) {
                completions.add("list");
            }

        } else if (args.length == 3 && args[0].equalsIgnoreCase("setloc")) {
            for (Material material : Material.values()) {
                String itemName = material.name();
                if (itemName.toLowerCase().startsWith(args[2].toLowerCase())) {
                    completions.add(itemName);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("edit")) {
            File file = new File(plugin.getDataFolder(), "datagen.yml");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection section = config.getConfigurationSection("genloc");
            if (section != null) {
                Set<String> generatorNames = section.getKeys(false);
                for (String generatorName : generatorNames) {
                    if (generatorName.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(generatorName);
                    }
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("edit")) {
            if ("name".startsWith(args[2].toLowerCase())) {
                completions.add("name");
            }
            if ("lore".startsWith(args[2].toLowerCase())) {
                completions.add("lore");
            }
            if ("amount".startsWith(args[2].toLowerCase())) {
                completions.add("amount");
            }
            if ("delay".startsWith(args[2].toLowerCase())) {
                completions.add("delay");
            }
            if ("enchant".startsWith(args[2].toLowerCase())) {
                completions.add("enchant");
            }
            if ("flags".startsWith(args[2].toLowerCase())) {
                completions.add("flags");
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("edit") && args[2].equalsIgnoreCase("lore")) {
            if ("add".startsWith(args[3].toLowerCase())) {
                completions.add("add");
            }
            if ("set".startsWith(args[3].toLowerCase())) {
                completions.add("set");
            }
            if ("clear".startsWith(args[3].toLowerCase())) {
                completions.add("clear");
            }
            if ("remove".startsWith(args[3].toLowerCase())) {
                completions.add("remove");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            File file = new File(plugin.getDataFolder(), "datagen.yml");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection section = config.getConfigurationSection("genloc");
            if (section != null) {
                Set<String> generatorNames = section.getKeys(false);
                for (String generatorName : generatorNames) {
                    if (generatorName.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(generatorName);
                    }
                }
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("edit") && args[2].equalsIgnoreCase("enchant")) {
            if ("add".startsWith(args[3].toLowerCase())) {
                completions.add("add");
            }
            if ("remove".startsWith(args[3].toLowerCase())) {
                completions.add("remove");
            }
            if ("clear".startsWith(args[3].toLowerCase())) {
                completions.add("clear");
            }
        } else if (args.length == 5 && args[0].equalsIgnoreCase("edit") && args[2].equalsIgnoreCase("enchant") && args[3].equalsIgnoreCase("add")) {
            for (Enchantment enchantment : Enchantment.values()) {
                String enchantmentName = enchantment.getName();
                if (enchantmentName.toLowerCase().startsWith(args[4].toLowerCase())) {
                    completions.add(enchantmentName);
                }
            }
        } else if (args.length == 5 && args[0].equalsIgnoreCase("edit") && args[2].equalsIgnoreCase("enchant") && args[3].equalsIgnoreCase("remove")) {
            File file = new File(plugin.getDataFolder(), "datagen.yml");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            List<String> enchants = config.getStringList("genloc." + args[1] + ".enchants");

            for (String enchant : enchants) {
                String enchantName = enchant.split(":")[0];
                if (enchantName.toLowerCase().startsWith(args[4].toLowerCase())) {
                    completions.add(enchantName);
                }
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("edit") && args[2].equalsIgnoreCase("flags")) {
            if ("add".startsWith(args[3].toLowerCase())) {
                completions.add("add");
            }
            if ("remove".startsWith(args[3].toLowerCase())) {
                completions.add("remove");
            }
            if ("clear".startsWith(args[3].toLowerCase())) {
                completions.add("clear");
            }
        } else if (args.length == 5 && args[0].equalsIgnoreCase("edit") && args[2].equalsIgnoreCase("flags") && args[3].equalsIgnoreCase("add")) {
            for (ItemFlag flag : ItemFlag.values()) {
                String flagName = flag.name();
                if (flagName.toLowerCase().startsWith(args[4].toLowerCase())) {
                    completions.add(flagName);
                }
            }
        } else if (args.length == 5 && args[0].equalsIgnoreCase("edit") && args[2].equalsIgnoreCase("flags") && args[3].equalsIgnoreCase("remove")) {
            File file = new File(plugin.getDataFolder(), "datagen.yml");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            List<String> flags = config.getStringList("genloc." + args[1] + ".flags");

            for (String flag : flags) {
                if (flag.toLowerCase().startsWith(args[4].toLowerCase())) {
                    completions.add(flag);
                }
            }
        }


        return completions;
    }
}



