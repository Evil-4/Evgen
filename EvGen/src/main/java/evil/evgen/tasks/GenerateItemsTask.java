package evil.evgen.tasks;

import evil.evgen.EvGen;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;

public class GenerateItemsTask implements Runnable {

    private final EvGen plugin;
    private final String generatorName;

    public GenerateItemsTask(EvGen plugin, String generatorName) {
        this.plugin = plugin;
        this.generatorName = generatorName;
    }

    @Override
    public void run() {
        File file = new File(plugin.getDataFolder(), "datagen.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (config.getConfigurationSection("genloc." + generatorName) != null) {
            String worldName = config.getString("genloc." + generatorName + ".world");
            double x = config.getDouble("genloc." + generatorName + ".x");
            double y = config.getDouble("genloc." + generatorName + ".y");
            double z = config.getDouble("genloc." + generatorName + ".z");
            Location loc = new Location(Bukkit.getWorld(worldName), x, y, z);
            Block block = loc.getBlock();
            String generateItem = config.getString("genloc." + generatorName + ".item");
            int amount = config.getInt("genloc." + generatorName + ".amount");
            List<String> itemLore = config.getStringList("genloc." + generatorName + ".lore");
            List<String> itemEnchants = config.getStringList("genloc." + generatorName + ".enchants");
            List<String> itemFlags = config.getStringList("genloc." + generatorName + ".flags");
            String itemName = config.getString("genloc." + generatorName + ".name");
            ItemStack itemStack = new ItemStack(Material.valueOf(generateItem), amount);
            ItemMeta meta = itemStack.getItemMeta();
            if (!itemLore.isEmpty()) {
                meta.setLore(itemLore);
            }
            meta.setDisplayName(itemName);
            for (String enchant : itemEnchants) {
                String[] parts = enchant.split(":");
                Enchantment enchantment = Enchantment.getByName(parts[0]);
                int level = Integer.parseInt(parts[1]);
                meta.addEnchant(enchantment, level, true);
            }
            for (String flag : itemFlags) {
                ItemFlag itemFlag = ItemFlag.valueOf(flag);
                meta.addItemFlags(itemFlag);
            }
            itemStack.setItemMeta(meta);
            Collection<Entity> entities = block.getWorld().getNearbyEntities(block.getLocation().add(0.5, 1, 0.5), 0.5, 1, 0.5);
            boolean playerFound = false;
            for (org.bukkit.entity.Entity entity : entities) {
                if (entity instanceof Player) {
                    Player player = (Player) entity;
                    player.getInventory().addItem(itemStack);
                    playerFound = true;
                }
            }
            if (!playerFound) {
                Item item = block.getWorld().dropItem(block.getLocation().add(0.5, 1, 0.5), itemStack);
                item.setVelocity(new Vector(0, 0, 0));
            }
        }
    }
}



