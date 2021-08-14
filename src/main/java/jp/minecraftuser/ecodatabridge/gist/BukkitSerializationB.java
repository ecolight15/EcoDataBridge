
package jp.minecraftuser.ecodatabridge.gist;

import java.util.ArrayList;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BukkitSerializationB {
    public static String[] inventoryToYaml(Inventory inventory) {
        ArrayList<String> ret = new ArrayList<>();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                Bukkit.getServer().getLogger().log(Level.INFO, "getItem[{0}] is null", i);
                ret.add("null");
            } else {
                YamlConfiguration con = new YamlConfiguration();
                con.set("i", inventory.getItem(i));
                ret.add(con.saveToString());
            }
        }
    	return ret.toArray(new String[0]);
    }
    public static String[] itemStacksToYaml(ItemStack[] stack) {
        ArrayList<String> ret = new ArrayList<>();
        for (int i = 0; i < stack.length; i++) {
            if (stack[i] == null) {
                Bukkit.getServer().getLogger().log(Level.INFO, "getItem[{0}] is null", i);
                ret.add("null");
            } else {
                YamlConfiguration con = new YamlConfiguration();
                con.set("i", stack[i]);
                ret.add(con.saveToString());
            }
        }
    	return ret.toArray(new String[0]);
    }
    public static ItemStack[] itemStacksFromYaml(String[] stack) {
        ArrayList<ItemStack> ret = new ArrayList<>();
        for (int i = 0; i < stack.length; i++) {
            if (stack[i] == "null") {
                Bukkit.getServer().getLogger().log(Level.INFO, "cnv[{0}] is null", i);
                ret.add(null);
            } else {
                YamlConfiguration con = new YamlConfiguration();
                try {
                    con.loadFromString(stack[i]);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
                ret.add(con.getItemStack("i", null));
            }
        }
    	return ret.toArray(new ItemStack[0]);
    }
}
