package me.blha303;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Logger;

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
    
    private boolean debug = false;
    private Logger log;

    @Override
    public void onEnable() {
        log = this.getLogger();
        getServer().getPluginManager().registerEvents(this, this);
        getConfig().addDefault("addPlaceholderHilightOnJoin", true);
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
        getConfig().addDefault("hilights.blha303", example);
        getConfig().addDefault("debug", false);
        getConfig().options().copyDefaults(true);
        saveConfig();
        debug = getConfig().getBoolean("debug");
    }

    @Override
    public void onDisable() {
        saveConfig();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!getConfig().contains("hilights." + event.getPlayer().getName()) && getConfig().getBoolean("addPlaceholderHilightOnJoin")) {
            if (debug) log.info("Player joined! Writing defaults");
            List<String> list = new ArrayList<String>();
            list.add(event.getPlayer().getName());
            getConfig().set("hilights." + event.getPlayer().getName(), list);
            saveConfig();
            if (debug) log.info("Defaults written for " + event.getPlayer().getName());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        String msg = event.getMessage();
        Player[] playerlist;
        List<String> hilightslist = new ArrayList<String>();
        boolean alreadymatched = false;
        if (debug) log.info("About to try to get onlinePlayersFuture");
        Future<Player[]> onlinePlayersFuture = getServer().getScheduler().callSyncMethod(this, new Callable<Player[]>() {

            public Player[] call() throws Exception {
                return (Player[]) getServer().getOnlinePlayers().toArray();
            }

        });
        try {
            playerlist = onlinePlayersFuture.get();
            if (debug) log.info("We got onlinePlayersFuture!");
        } catch (Exception e) {
            if (debug) e.printStackTrace();
            if (debug) log.info("We didn't get onlinePlayersFuture...");
            return;
        }

        if (playerlist == null) {
            if (debug) log.info("playerlist was null...");
            return;
        }
        for (final Player player : playerlist) {
            if (debug) log.info("Trying to get hilightslistfuture...");
            Future<List<String>> hilightslistfuture = getServer().getScheduler().callSyncMethod(this, new Callable<List<String>>() {

                public List<String> call() throws Exception {
                    return getConfig().getStringList("hilights." + player.getName());
                }

            });
            try {
                hilightslist = hilightslistfuture.get();
                if (debug) log.info("We got hilightslistfuture!");
            } catch (Exception e) {
                if (debug) log.info("We didn't get hilightslistfuture...");
                return;
            }
            for (String hilight : hilightslist) {
                if (debug) log.info("Iterating hilightslist now...");
                if (contains(msg, hilight)) {
                    if (!player.getName().equals(sender.getName()) && !alreadymatched) {
                        event.getRecipients().remove(player);
                        String newmessage = msg.replaceAll(hilight, ChatColor.YELLOW + hilight + ChatColor.RESET);
                        if (debug) log.info("Trying to play the sound...");
                        getServer().getScheduler().callSyncMethod(this, new Callable<Object>() {

                            public Object call() throws Exception {
                                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10.0F, 1.0F);
                                return null;
                            }

                        });
                        player.sendMessage(String.format(event.getFormat(), sender.getDisplayName(), newmessage));
                        alreadymatched = true;
                    }
                }
            }
            if (debug) log.info("Finished iterating hilightslist");
        }
        if (debug) log.info("Finished iterating playerlist. Job complete.");
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getLabel().equalsIgnoreCase("notify")) {
            if (args.length == 0)
                return false;
            if (args.length >= 1) {
                for (String name : args) {
                    String player = getServer().getPlayer(name).getName();
                    if (player != null) {
                        getServer().getPlayer(player).sendMessage(ChatColor.YELLOW + sender.getName() + " pinged you!");
                        getServer().getPlayer(player).playSound(getServer().getPlayer(player).getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10.0F, 1.0F);
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.RED + "Could not match a player with " + name);
                    }
                }
                return true;
            }
        } else if (command.getLabel().equalsIgnoreCase("hladd")) {
            if (sender instanceof Player) {
                List<String> list = getConfig().getStringList("hilights." + sender.getName());
                if (args.length == 0)
                    return false;
                for (String i : args) {
                    if (list.contains(i)) {
                        sender.sendMessage(String.format("You already have %s set to ping you.", ChatColor.YELLOW + i + ChatColor.RESET));
                        return true;
                    }
                    list.add(i);
                }
                getConfig().set("hilights." + sender.getName(), list);
                saveConfig();
                int i = 0;
                String strlist = "";
                for (String s : list) {
                    if (i == 0) {
                        strlist = s;
                    } else {
                        strlist = strlist + ", " + s;
                    }
                    i++;
                }
                sender.sendMessage("Hilight list: " + strlist);
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
                    saveConfig();
                    int i = 0;
                    String strlist = "";
                    for (String s : list) {
                        if (i == 0) {
                            strlist = s;
                        } else {
                            strlist = strlist + ", " + s;
                        }
                        i++;
                    }
                    sender.sendMessage(String.format("Hilight list for %s: %s", name, strlist));
                    return true;
                }

            }
        } else if (command.getLabel().equalsIgnoreCase("hldel")) {
            if (sender instanceof Player) {
                if (args.length == 0) return false;
                List<String> list = getConfig().getStringList("hilights." + sender.getName());
                for (String i : args) {
                    boolean removed = list.remove(i);
                    if (removed) sender.sendMessage("Removed " + i + " from the list of hilights.");
                }
                getConfig().set("hilights." + sender.getName(), list);
                saveConfig();
                return true;
            } else {
                if (args.length == 0) {
                    sender.sendMessage("Usage from console: /hldel <playername> <string>...");
                    return true;
                } else if (args.length == 1) {
                    sender.sendMessage("Usage from console: /hldel <playername> <string>...");
                    return true;
                } else {
                    String name = getServer().getPlayer(args[0]).getName();
                    List<String> list = getConfig().getStringList("hilights." + name);
                    for (int i = 1; i < args.length;) {
                        boolean removed = list.remove(args[i]);
                        if (removed) sender.sendMessage("Removed " + args[i] + " from " + name + "'s list of hilights.");
                    }
                    getConfig().set("hilights." + name, list);
                    saveConfig();
                    return true;
                }
            }
        }
        return false;
    }

    // http://stackoverflow.com/a/2275030
    public boolean contains(String haystack, String needle) {
        haystack = haystack == null ? "" : haystack;
        needle = needle == null ? "" : needle;
        return haystack.toLowerCase().contains(needle.toLowerCase());
    }

}
