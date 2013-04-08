package me.blha303;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class HilightNotifier extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        for (Player p : getServer().getOnlinePlayers()) {
        	if (!getConfig().contains("hilights." + p.getName())) {
        		List<String> list = new ArrayList<String>();
                list.add(p.getName());
                getConfig().set("hilights." + p.getName(), list);
        	}
        }
        List<String> example = new ArrayList<String>();
        example.add("blha303"); example.add("blha"); example.add("steven");
        getConfig().addDefault("hilights.blha303", example);
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!getConfig().contains("hilights." + event.getPlayer().getName())) {
            List<String> list = new ArrayList<String>();
            list.add(event.getPlayer().getName());
            getConfig().set("hilights." + event.getPlayer().getName(), list);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        String msg = ChatColor.stripColor(event.getMessage());

        for (Player player : getServer().getOnlinePlayers()) {
        	for (String hilight : getConfig().getStringList("hilights." + player.getName())) {
        		if (contains(msg, hilight)) {
            		if (!player.getName().equals(sender.getName())) {
            			String newmessage = msg.replaceAll(player.getName(), ChatColor.YELLOW + player.getName());
            			player.playSound(player.getLocation(), Sound.ORB_PICKUP, 10.0F, 1.0F);
            			player.sendMessage(String.format(event.getFormat(), sender.getDisplayName(), newmessage));
            			event.getRecipients().remove(player);
            		}
            	}
        	}
        }
    }
    
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			List<String> list = getConfig().getStringList("hilights." + sender.getName());	
			if (args.length == 0) return false;
			for (int i = 0; i<=args.length; i++) {
				list.add(args[i]);
			}
			getConfig().set("hilights." + sender.getName(), list);
			String delim = "";
			String listout = "";
			for (String i : list) {
				listout.concat(delim).concat(i);
				delim = ",";
			}
			sender.sendMessage("Hilight list: " + listout);
			return true;
		} else {
			if (args.length == 0) {
				sender.sendMessage("Usage from console: /hladd <playername> <string>...");
				return true;
			} else
			if (args.length == 1) {
				sender.sendMessage("Usage from console: /hladd <playername> <string>...");
				return true;
			} else {
				String name = getServer().getPlayer(args[0]).getName();
				List<String> list = getConfig().getStringList("hilights." + name);	
				for (int i = 1; i<=args.length; i++) {
					list.add(args[i]);
				}
				getConfig().set("hilights." + name, list);
				String delim = "";
				String listout = "";
				for (String i : list) {
					listout.concat(delim).concat(i);
					delim = ",";
				}
				sender.sendMessage(String.format("Hilight list for %s: %s", name, listout));
				return true;
			}
			
		}
	}
    
	// http://stackoverflow.com/a/2275030
	public boolean contains(String haystack, String needle) {
		haystack = haystack == null ? "" : haystack;
		needle = needle == null ? "" : needle;
		return haystack.toLowerCase().contains(needle.toLowerCase());
	}

}
