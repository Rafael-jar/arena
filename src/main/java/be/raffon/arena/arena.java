package be.raffon.arena;

import be.raffon.arena.inventories.InventoryManager;
import be.raffon.arena.scoreboards.FastBoard;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


@SuppressWarnings("deprecation")
public class arena extends JavaPlugin implements Listener{
    String host;
    int port;
    String database;
    String username;
    String password;
    private static InventoryManager invs;
    public YamlConfiguration config;
    public static File fileconfig;
    public String text = "["+ChatColor.RED+"Arena"+ChatColor.WHITE+"]";
    private final Map<UUID, FastBoard> boards = new HashMap<>();
    public static Map<UUID, Integer> kills;
    SQLManager sqlManager;

    @Override
    public void onEnable() {
		/*File dir = this.getDataFolder(); // Get the parent directory
		dir.mkdirs();
		js = new File(this.getDataFolder() + "//" + "config.JSON");
		if(!js.exists()) {
			new File(this.getDataFolder() + "//").mkdirs();

			JSONObject o = setJSON();
			try {
				FileWriter file = new FileWriter(this.getDataFolder() + "//" + "config.JSON");
				file.write(o.toJSONString());
				file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		js = new File(this.getDataFolder(), "//" + "config.JSON");*/

        host = "localhost";
        port = 3306;
        database = "sf2021";
        username = "sf2021";
        password = "Lq%n9aajZS7CtU";
        sqlManager = new SQLManager(host, port, database, username, password);
        this.fileconfig = new File(this.getDataFolder().getPath(), "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(fileconfig);
        this.config = config;

        String sql = "CREATE TABLE IF NOT EXISTS arena_kills (\n"
                + "	username VARCHAR(100) PRIMARY KEY,\n"
                + "	kills INTEGER\n"
                + ");";
        sqlManager.getInstance().update(sql);

        AtomicReference<HashMap<UUID, Integer>> hashmap = new AtomicReference<HashMap<UUID, Integer>>();
        SQLManager.getInstance().query("SELECT * FROM arena_kills;", rs -> {
            try {
                HashMap<UUID, Integer> hash = new HashMap<UUID, Integer>();
                while(rs.next()) {
                    String username = rs.getString("username");
                    Integer kills = rs.getInt("kills");
                    hash.put(UUID.fromString(username), kills);
                }
                hashmap.set(hash);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });

        invs = new InventoryManager();

        kills = hashmap.get();

        System.out.println("Arena succeffuly loaded !");

        System.setProperty("file.encoding", "UTF-8");
        getServer().getPluginManager().registerEvents(this, this);

        this.fileconfig = new File(this.getDataFolder().getPath(), "config.yml");
        this.config = YamlConfiguration.loadConfiguration(fileconfig);

        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {

            @Override
            public void run() {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    this.updateBoard(player);
                }
            }
            private void updateBoard(Player player) {

                FastBoard fb = boards.get(player.getUniqueId());
                if(fb == null) {
                    return;
                }

                Integer kill = 0;
                if (kills.get(player.getUniqueId()) == null) {kills.put(player.getUniqueId(), 0);}
                kill = kills.get(player.getUniqueId());

                fb.updateLines(
                        "",
                        ChatColor.YELLOW + "Kills: " + ChatColor.WHITE + kill
                );
            }


        }, 0L, 20L);
    }

    @Override
    public void onDisable() {
        for(Map.Entry<UUID, Integer> entry : kills.entrySet()) {
            UUID key = entry.getKey();
            Integer value = entry.getValue();

            sqlManager.getInstance().update("INSERT INTO arena_kills (username, kills) VALUES ('" + key + "', " + value + ") ON DUPLICATE KEY UPDATE kills="+value+";");
        }
    }


    @EventHandler
    public void onKill(PlayerDeathEvent e)
    {
        Player pl = e.getEntity().getPlayer();
        Player killer = e.getEntity().getKiller();
        if(killer == null) return;

        Integer kill = kills.get(killer.getUniqueId())+1;
        if(kill != null) {
            kills.remove(killer.getUniqueId());
            kills.put(killer.getUniqueId(), kill);
        } else {
            kills.put(killer.getUniqueId(), 1);
        }


        if(kill%10 == 0 && kill != 0) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gmysterybox give "+ killer.getName() + " 1 4");
            killer.sendMessage(this.text + " Well done you received a mystery box level 4 because of your participation :).");
        }




    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if(boards.get(event.getPlayer().getUniqueId()) == null) return;

        ItemStack leave = new ItemStack(Material.DARK_OAK_DOOR);
        ItemMeta leavem = leave.getItemMeta();leavem.setDisplayName(ChatColor.RED + "Leave");leave.setItemMeta(leavem);
        event.getPlayer().getInventory().setItem(8, leave);
        event.setRespawnLocation(getRandomLoc());
    }

    /*
        ArrayList<ConfigurationSection> locs = pitchout.getLocation();
		for(int i=0; i<locs.size(); i++) {
			ConfigurationSection obj = locs.get(i);
			Boolean used = false;
			for(int k=0; k<games.size(); k++) {
				Game game = games.get(k);

				if(game.usedLoc == i) {
					used = true;
				}
			}

			if(!used) {
				Location loc = new Location(obj);
				Game game =  new Game(loc, i);
				games.add(game);
				return game;
			} else {
				Location loc = new Location(obj);


			}
		}
		*/
    public Location getRandomLoc() {
        ArrayList<Location> locs = new ArrayList<Location>();
        /*for(int i=0; i<locs.size(); i++) {
            ConfigurationSection obj = locs.get(i);
            Location loc = new Location(Bukkit.getWorld(obj.getString("world")), obj.getInt("x"), obj.getInt("y"), obj.getInt("z"));
            return loc;
        }

        -194 78 -150
        -243 74 -215
        -299 80 -214
        -318 79 -152
        -250 76 -80
        -244 75 -141

        */
        locs.add(new Location(Bukkit.getWorld("world"), -194, 78, -150));
        locs.add(new Location(Bukkit.getWorld("world"), -243, 74, -215));
        locs.add(new Location(Bukkit.getWorld("world"), -299, 80, -214));
        locs.add(new Location(Bukkit.getWorld("world"), -318, 79, -152));
        locs.add(new Location(Bukkit.getWorld("world"), -250, 76, -80));
        locs.add(new Location(Bukkit.getWorld("world"), -244, 75, -141));

        Integer in = locs.size();
        Random rand = new Random();
        int random = rand.nextInt(in);

        return locs.get(random);
    }


    /*public static ArrayList<ConfigurationSection> getLocation() {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(fileconfig);
        ArrayList<ConfigurationSection> arr = new ArrayList<ConfigurationSection>();

        ConfigurationSection command = yaml.getConfigurationSection("locations");
        for (String key : command.getKeys(false)) {
            ConfigurationSection sect = yaml.getConfigurationSection("locations."+key);
            arr.add(sect);
        }

        return arr;
    }*/

    @EventHandler
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Console can not use this plugin!");
            return true;
        }

        Player pl = (Player) sender;
        if(args[0].equals("join")) {
            if(!pl.hasPermission("arena.join")) {
                pl.sendMessage(text + " You do not have the permission to use this command !");
                return true;
            }

            pl.getInventory().clear();
            FastBoard board = new FastBoard(pl);

            board.updateTitle(ChatColor.GREEN + "Scores");

            pl.setGameMode(GameMode.SURVIVAL);
            boards.put(pl.getUniqueId(), board);

            pl.setHealth(20.0);

            ItemStack arc = new ItemStack(Material.BOW);
            ItemMeta arcm = arc.getItemMeta();arcm.setUnbreakable(true);arc.setItemMeta(arcm);
            pl.getInventory().setItem(1, arc);

            ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
            ItemMeta swordmeta = sword.getItemMeta();swordmeta.setUnbreakable(true);sword.setItemMeta(swordmeta);
            pl.getInventory().setItem(0, sword);

            pl.getInventory().setItem(2, new ItemStack(Material.ARROW, 64));

            ItemStack leave = new ItemStack(Material.DARK_OAK_DOOR);
            ItemMeta leavem = leave.getItemMeta();leavem.setDisplayName(ChatColor.RED + "Leave");leave.setItemMeta(leavem);
            pl.getInventory().setItem(8, leave);

            ItemStack boots = new ItemStack(Material.IRON_BOOTS);
            ItemMeta bootsmeta = boots.getItemMeta();bootsmeta.setUnbreakable(true);boots.setItemMeta(bootsmeta);
            pl.getInventory().setBoots(boots);

            ItemStack helm = new ItemStack(Material.IRON_HELMET);
            ItemMeta helmmeta = helm.getItemMeta();helmmeta.setUnbreakable(true);helm.setItemMeta(helmmeta);
            pl.getInventory().setHelmet(helm);

            ItemStack chest = new ItemStack(Material.IRON_CHESTPLATE);
            ItemMeta chestmeta = chest.getItemMeta();chestmeta.setUnbreakable(true);chest.setItemMeta(chestmeta);
            pl.getInventory().setChestplate(chest);

            ItemStack legs = new ItemStack(Material.IRON_LEGGINGS);
            ItemMeta legsmeta = legs.getItemMeta();legsmeta.setUnbreakable(true);legs.setItemMeta(legsmeta);
            pl.getInventory().setLeggings(legs);

            Location loc = getRandomLoc();
            pl.teleport(getRandomLoc());
            pl.setBedSpawnLocation(loc);
        } else if(args[0].equals("leave")) {
            if(!pl.hasPermission("arena.leave")) {
                pl.sendMessage(text + " You do not have the permission to use this command !");
                return true;
            }
            if(boards.get(pl.getUniqueId()) == null) return true;

            /*pl.getInventory().setLeggings(new ItemStack(Material.AIR));
            pl.getInventory().setChestplate(new ItemStack(Material.AIR));
            pl.getInventory().setHelmet(new ItemStack(Material.AIR));
            pl.getInventory().setBoots(new ItemStack(Material.AIR));
            pl.getInventory().setItem(1, new ItemStack(Material.AIR));
            pl.getInventory().setItem(0, new ItemStack(Material.AIR));
            pl.getInventory().setItem(2, new ItemStack(Material.AIR));*/
            pl.getInventory().clear();

            FastBoard board = boards.get(pl.getUniqueId());
            board.delete();
            boards.remove(pl.getUniqueId());

            pl.teleport(new Location(Bukkit.getWorld("world"), -118, 120, -152));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gmenu menuitem " + pl.getName());
        }
        return true;
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event){
        Player pl = event.getPlayer();
        Location loc = event.getTo();
        System.out.println(loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + " " + (loc.getBlockX() == -118) + " " + (loc.getBlockY() == 120) + " " + (loc.getBlockZ() == -152));
        if(loc.getBlockX() == -118 && loc.getBlockY() == 120 && loc.getBlockZ() == -152) {
            if(boards.get(pl.getUniqueId()) == null) return;

            FastBoard board = boards.get(pl.getUniqueId());
            board.delete();
            boards.remove(pl.getUniqueId());
            pl.getInventory().clear();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gmenu menuitem " + pl.getName());
        }
    }

    @EventHandler
    public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
        ItemStack is = event.getItem();
        System.out.println(is.getItemMeta().getDisplayName() + " " + is.getType());
        if(is.getItemMeta().getDisplayName().equals(ChatColor.RED + "Leave") && is.getType() == Material.DARK_OAK_DOOR) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void OnPlayerInteract(PlayerInteractEvent e) {
        try {
            Player p = e.getPlayer();
            ItemStack is = e.getItem();
            if (is.getItemMeta().getDisplayName().equals(ChatColor.RED + "Leave") && is.getType() == Material.DARK_OAK_DOOR) {
                e.setCancelled(true);
                e.getPlayer().chat("/pvparena leave");
            }
        } catch(NullPointerException e1) {
            return;
        }
    }

}
