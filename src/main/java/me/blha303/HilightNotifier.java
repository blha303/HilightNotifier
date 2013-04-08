package me.blha303;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
                getConfig().addDefault("hilights." + p.getName(), list);
            }
        }
        List<String> example = new ArrayList<String>();
        example.add("blha303");
        example.add("blha");
        example.add("steven");
        getConfig().addDefault("hilights.blha303", example);
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    @Override
    public void onDisable() {
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        String msg = event.getMessage();
        Player[] playerlist;
        List<String> hilightslist = new ArrayList<String>();
        boolean alreadymatched = false;
        Future<Player[]> onlinePlayersFuture = getServer().getScheduler().callSyncMethod(this, new Callable<Player[]>() {

            public Player[] call() throws Exception {
                return getServer().getOnlinePlayers();
            }

        });
        try {
            playerlist = onlinePlayersFuture.get();
        } catch (Exception e) {
            return;
        }

        if (playerlist == null)
            return;
        for (final Player player : playerlist) {
            Future<List<String>> hilightslistfuture = getServer().getScheduler().callSyncMethod(this, new Callable<List<String>>() {

                public List<String> call() throws Exception {
                    return getConfig().getStringList("hilights." + player.getName());
                }

            });
            try {
                hilightslist = hilightslistfuture.get();
            } catch (Exception e) {
                return;
            }
            for (String hilight : hilightslist) {
                if (contains(msg, hilight)) {
                    if (!player.getName().equals(sender.getName()) && !alreadymatched) {
                        event.getRecipients().remove(player);
                        String newmessage = msg.replaceAll(hilight, ChatColor.YELLOW + hilight + ChatColor.RESET);
                        getServer().getScheduler().callSyncMethod(this, new Callable<Object>() {

                            public Object call() throws Exception {
                                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 10.0F, 1.0F);
                                return null;
                            }

                        });
                        player.sendMessage(String.format(event.getFormat(), sender.getDisplayName(), newmessage));
                        alreadymatched = true;
                    }
                }
            }
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            List<String> list = getConfig().getStringList("hilights." + sender.getName());
            if (args.length == 0)
                return false;
            for (int i = 0; i < args.length;) {
                if (list.contains(args[i])) {
                    sender.sendMessage("You already have that string set to ping you.");
                    return true;
                }
                list.add(args[i]);
                i++;
            }
            getConfig().set("hilights." + sender.getName(), list);
            sender.sendMessage("Hilight list: " + list.toArray().toString());
            return true;
        } else {
            if (args.length == 0) {
                sender.sendMessage("Usage from console: /hladd <playername> <string>...");
                return true;
            } else if (args.length == 1) {
                sender.sendMessage("Usage from console: /hladd <playername> <string>...");
                return true;
            } else {
                String name = getServer().getPlayer(args[0]).getName();
                List<String> list = getConfig().getStringList("hilights." + name);
                for (int i = 1; i < args.length; i++) {
                    if (list.contains(args[i])) {
                        sender.sendMessage("That player already has that string set to ping them.");
                        return true;
                    }
                    list.add(args[i]);
                }
                getConfig().set("hilights." + name, list.toArray());
                String delim = "";
                String listout = "";
                for (String i : list) {
                    listout.concat(delim).concat(i);
                    delim = ",";
                }
                sender.sendMessage(String.format("Hilight list for %s: %s", name, list.toArray().toString()));
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
