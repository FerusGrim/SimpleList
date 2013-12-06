// Copyright Nicholas Badger (A.K.A. FerusGrim) //
// Attribution-NonCommercial-ShareAlike 4.0 International (CC BY-NC-SA 4.0) //

package com.caelusrp.ferusgrim.simplelist;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


public class simplelist extends JavaPlugin {

	public static final Logger log = Logger.getLogger("Minecraft");

	static String maindir = "plugins/simplelist/";
	public static YamlConfiguration Settings;
	static File SettingsFile = new File(maindir + "config.yml");
	static ArrayList<String> WhiteListedPlayers = new ArrayList<String>();
    int RefreshWhitelistTaskID = -1;
	static boolean WhitelistON = true;

	public void onDisable() {
		//Clears the whitelist array to clear up memory
		WhiteListedPlayers = new ArrayList<String>();
		//Removes the config to clear up memory
		Settings = null;

		this.getServer().getScheduler().cancelAllTasks();
        RefreshWhitelistTaskID = -1;
		//Shuts the whitelist down cleanly, preventing errors on "stop" command.
		WhitelistON = false;
	}
	@SuppressWarnings("deprecation")
	public void onEnable() {
		new File(maindir).mkdir();
		//Checks if file exists
		if (!SettingsFile.exists()) {
			//Creates the config file if it doesn't exist
			try {
				SettingsFile.createNewFile();
				Settings = config.loadMain(true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			//If it exists load the config
			Settings = config.loadMain(false);
		}
		File WhitelistFile = new File(maindir + simplelist.Settings.getString("simplelist.file.name"));
		if (!WhitelistFile.exists()) {
			try {
				WhitelistFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// Sets the whitelists mode
		WhitelistON = simplelist.Settings.getBoolean("simplelist.enabled");

		PluginManager pm = getServer().getPluginManager();

		//Registers the Listener class
		pm.registerEvents(new listener(), this);
		ConsoleCommandSender console = getServer().getConsoleSender();
		console.sendMessage(ChatColor.YELLOW + "[SL]" + ChatColor.DARK_AQUA + " SimpleList: " + (WhitelistON == true ? ChatColor.GREEN + "ACTIVE" : ChatColor.RED + "DEACTIVE") + ChatColor.DARK_AQUA + "!");
		DebugPrint("§8You're seeing debugging messages! To deactivate these, set \"debug\" to 'false'.");
		RefreshWhitelist(true);
        if(RefreshWhitelistTaskID < 0){
                RefreshWhitelistTaskID = getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
                        public void run() {
                                RefreshWhitelist(false);
                        }
                }, 0, Settings.getInt("simplelist.file.update-interval") * 20);
        }
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
		String commandLabel, String[] args) {
		String commandName = command.getName().toLowerCase();
		String[] trimmedArgs = args;

		if (commandName.equals("whitelist")) {
			return CommandHandler(sender, trimmedArgs);
		}
		return false;
	}

	public static void SendMessage(CommandSender Player, String MSG){
		Player.sendMessage("§e[SL] §3" + MSG);
	}

	public boolean CommandHandler(CommandSender sender, String[] trimmedArgs) {
		if (trimmedArgs.length > 0) {
			String[] args = RearangeString(1, trimmedArgs);
			String CommandName = trimmedArgs[0];
			if (CommandName.equals("add")) {
				return AddPlayerToWhitelist(sender, args);
			}
			if (CommandName.equals("remove")) {
				return RemovePlayerFromWhitelist(sender, args);
			}
			if (CommandName.equals("reload")) {
				return ReloadPlugin(sender, args);
			}
			if (CommandName.equals("refresh")) {
				return RefreshWhitelist(sender, args);
			}
			if (CommandName.equals("on")) {
				return WhitelistOn(sender, args);
			}
			if (CommandName.equals("off")) {
				return WhitelistOff(sender, args);
			}
		}
		PrintHelper(sender);
		return true;
	}

	private boolean WhitelistOn(CommandSender sender, String[] args){
		boolean auth = false;
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
			if (player.hasPermission("simplelist.toggle"))
				auth = true;
		} else {
			auth = true;
		}
		if (auth) {
			WhitelistON = true;
			ConsoleCommandSender console = getServer().getConsoleSender();
			console.sendMessage(ChatColor.YELLOW + "[SL] "
					+ ChatColor.DARK_AQUA + (player == null ? "A console administrator" : player.getName())
					+ " changed SimpleList status: " + ChatColor.GREEN + "ACTIVE" + ChatColor.DARK_AQUA + "!");
						sender.sendMessage("§e[SL] §3SimpleList: §aACTIVE§3!");
			return true;
		}
		sender.sendMessage("§e[SL] §cInsufficient privelages!");
		return true;
	}

	private boolean WhitelistOff(CommandSender sender, String[] args){
		boolean auth = false;
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
			if (player.hasPermission("simplelist.toggle"))
				auth = true;
		} else {
			auth = true;
		}
		if (auth) {
			WhitelistON = false;
			ConsoleCommandSender console = getServer().getConsoleSender();
			console.sendMessage(ChatColor.YELLOW + "[SL] "
					+ ChatColor.DARK_AQUA + (player == null ? "A console administrator" : player.getName())
					+ " changed SimpleList status: " + ChatColor.RED + "DEACTIVE" + ChatColor.DARK_AQUA + "!");
			sender.sendMessage("§e[SL] §3SimpleList: §cDEACTIVE§3!");
			return true;
		}
		sender.sendMessage("§e[SL] §cInsufficient privelages!");
		return true;
	}

	private boolean RefreshWhitelist(CommandSender sender, String[] args){
		boolean auth = false;
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
			if (player.hasPermission("simplelist.refresh"))
				auth = true;
		} else {
			auth = true;
		}
		if (auth) {
			ConsoleCommandSender console = getServer().getConsoleSender();
			console.sendMessage(ChatColor.YELLOW + "[SL] "
					+ ChatColor.DARK_AQUA + (player == null ? "A console administrator" : player.getName())
					+ " refreshed SimpleList!");
			sender.sendMessage("§e[SL] §3SimpleList: REFRESHED!");
			(new UpdateWhitelist(false)).start();
			return true;
		}
		sender.sendMessage("§e[SL] §cInsufficient privelages!");
		return true;
	}

	private void PrintHelper(CommandSender sender){
		sender.sendMessage("§7== §8simplelist");
		sender.sendMessage("§7== > §a/whitelist §c[on/off]");
		sender.sendMessage("§7== > §a/whitelist §badd §c[player]");
		sender.sendMessage("§7== > §a/whitelist §bremove §c[player]");
		sender.sendMessage("§7== > §a/whitelist §brefresh");
		sender.sendMessage("§7== > §a/whitelist §breload");
	}

	private boolean ReloadPlugin(CommandSender sender, String[] args){
		boolean auth = false;
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
			if (player.hasPermission("simplelist.reload"))
				auth = true;
		} else {
			auth = true;
		}
		if (auth) {
			onDisable();
			onEnable();
			ConsoleCommandSender console = getServer().getConsoleSender();
			console.sendMessage(ChatColor.YELLOW + "[SL] "
					+ ChatColor.DARK_AQUA + (player == null ? "A console administrator" : player.getName())
					+ " reloaded SimpleList configuration!");
			sender.sendMessage("§e[SL] §3SimpleList: RELOADED!");
			return true;
		}
		sender.sendMessage("§e[SL] §cInsufficient privelages!");
		return true;
	}

	private ArrayList<String> GetWhitelist(String Type){
		ArrayList<String> tmpArray = new ArrayList<String>();
		if(Type.equals("file")){
			FileInputStream in;
			try {
				in = new FileInputStream(maindir + simplelist.Settings.getString("simplelist.file.name"));

				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String strLine;
				while ((strLine = br.readLine()) != null) {
					tmpArray.add(strLine.toLowerCase());
				}
				DebugPrint("Whitelist (type:" + Type +" count: " + tmpArray.toArray().length + ")");
				in.close();
				return tmpArray;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}else if(Type.equals("mysql")){
				Connection conn = connector.getSQLConnection();
				if (conn == null) {
					ConsoleCommandSender console = getServer().getConsoleSender();
					console.sendMessage(ChatColor.YELLOW + "[SL-" + ChatColor.RED + "001" + ChatColor.YELLOW + "] "
							+ ChatColor.RED + "SimpleList could not establish a SQL connection!");
					return null;
				} else {

					PreparedStatement ps = null;
					ResultSet rs = null;
					try {
						String Query = simplelist.Settings.getString("simplelist.mysql.query");
						Query = Query.replace("{table}",
								simplelist.Settings.getString("simplelist.mysql.table"));
						Query = Query.replace("{field}",
								simplelist.Settings.getString("simplelist.mysql.field"));
						Query = Query.replace("{time}", "" + GetTime());
						ps = conn.prepareStatement(Query);
						rs = ps.executeQuery();
						while (rs.next()) {
							tmpArray.add(rs.getString(
									simplelist.Settings.getString("simplelist.mysql.field")).toLowerCase());
						}
						DebugPrint("Whitelist (type:" + Type +" count: " + tmpArray.toArray().length + ")");
						return tmpArray;
					} catch (SQLException ex) {
						ConsoleCommandSender console = getServer().getConsoleSender();
						console.sendMessage(ChatColor.YELLOW + "[SL-" + ChatColor.RED + "002" + ChatColor.YELLOW + "] "
								+ ChatColor.RED + "SQL statement could not be executed!");
						log.log(Level.SEVERE,
								"STATEMENT:",
								ex);
					} finally {
						try {
							if (ps != null)
								ps.close();
							if (conn != null)
								conn.close();
						} catch (SQLException ex) {
							ConsoleCommandSender console = getServer().getConsoleSender();
							console.sendMessage(ChatColor.YELLOW + "[SL-" + ChatColor.RED + "003" + ChatColor.YELLOW + "] "
									+ ChatColor.RED + "Failed to close connection to database!");
							log.log(Level.SEVERE,
									"STATEMENT:",
									ex);
						}
					}

					try {
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
		}else{
			ConsoleCommandSender console = getServer().getConsoleSender();
			console.sendMessage(ChatColor.YELLOW + "[SL-" + ChatColor.RED + "004" + ChatColor.YELLOW + "] " + ChatColor.DARK_AQUA + "The connection type you've chosen, \"" + ChatColor.YELLOW + simplelist.Settings.getString("simplelist.connection") + ChatColor.DARK_AQUA + "\" doesn't exist!");
		}
		return null;
	}

	private boolean AddPlayerToWhitelist(CommandSender sender, String[] args) {
		boolean auth = false;
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
			if (player.hasPermission("simplelist.add"))
				auth = true;
		} else {
			auth = true;
		}
		if (auth) {
			if (args.length > 0) {
				String p = args[0];

				if (simplelist.WhiteListedPlayers.contains(p
						.toLowerCase())) {
					sender.sendMessage("§e[SL] §3" + p + ":§c ALREADY WHITELISTED§3!");
					return true;
				}

				simplelist.WhiteListedPlayers.add(p.toLowerCase());
				String ConType = simplelist.Settings.getString("simplelist.connection");
				if(ConType.equals("file")){
						try{
							BufferedWriter fW = new BufferedWriter(new FileWriter(maindir + simplelist.Settings.getString("simplelist.file.name")));
							for(int i = 0; i< simplelist.WhiteListedPlayers.size(); i = i + 1){
								fW.write(simplelist.WhiteListedPlayers.get(i));
								fW.newLine();
							}
						    fW.close();
						}catch (Exception e){
							e.printStackTrace();
						}
						ConsoleCommandSender console = getServer().getConsoleSender();
						console.sendMessage(ChatColor.YELLOW + "[SL] "
								+ ChatColor.DARK_AQUA + (player == null ? "A console administrator" : player.getName())
								+ " whitelisted " + ChatColor.YELLOW + p + ChatColor.DARK_AQUA + "!");
						sender.sendMessage("§e[SL] §3" + p + ":§a WHITELISTED§3!");
						return true;
				}else if(ConType.equals("mysql")){
						Connection conn = null;
						PreparedStatement ps = null;
						try {
							conn = connector.getSQLConnection();
							ps = conn.prepareStatement("INSERT INTO "
									+ Settings.getString("simplelist.mysql.table") + " (" + Settings.getString("simplelist.mysql.field") + ") VALUES(?)");
							ps.setString(1, p);
							ps.executeUpdate();
						} catch (SQLException ex) {
							ConsoleCommandSender console = getServer().getConsoleSender();
							console.sendMessage(ChatColor.YELLOW + "[SL-" + ChatColor.RED + "005" + ChatColor.YELLOW + "] "
									+ ChatColor.RED + "SQL statement couldn't be executed!");
							log.log(Level.SEVERE,
									"STATEMENT:",
									ex);
						} finally {
							try {
								if (ps != null)
									ps.close();
								if (conn != null)
									conn.close();
							} catch (SQLException ex) {
								ConsoleCommandSender console = getServer().getConsoleSender();
								console.sendMessage(ChatColor.YELLOW + "[SL-" + ChatColor.RED + "006" + ChatColor.YELLOW + "] "
										+ ChatColor.RED + "Failed to close connection to database!");
								log.log(Level.SEVERE,
										"STATEMENT:",
										ex);
							}
						}

						ConsoleCommandSender console = getServer().getConsoleSender();
						console.sendMessage(ChatColor.YELLOW + "[SL] "
								+ ChatColor.DARK_AQUA + (player == null ? "A console administrator" : player.getName())
								+ " whitelisted " + ChatColor.YELLOW + p + ChatColor.DARK_AQUA + "!");
						sender.sendMessage("§e[SL] §3" + p + ":§a WHITELISTED§3!");
						return true;
				}else{
					ConsoleCommandSender console = getServer().getConsoleSender();
					console.sendMessage(ChatColor.YELLOW + "[SL-" + ChatColor.RED + "007" + ChatColor.YELLOW + "] " + ChatColor.DARK_AQUA + "The connection type you've chosen, \"" + ChatColor.YELLOW + simplelist.Settings.getString("simplelist.connection") + ChatColor.DARK_AQUA + "\" doesn't exist!");
					
				}
			}
		}
		sender.sendMessage("§e[SL] §cInsufficient privelages!");
		return true;
	}

	private boolean RemovePlayerFromWhitelist(CommandSender sender, String[] args) {
		boolean auth = false;
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
			if (player.hasPermission("simplelist.remove"))
				auth = true;
		} else {
			auth = true;
		}
		if (auth) {
			if (args.length > 0) {
				String p = args[0];
				Player victim = this.getServer().getPlayer(p);

				if (!simplelist.WhiteListedPlayers.contains(p
						.toLowerCase())) {
					sender.sendMessage("§e[SL] §3" + p + ": §cISN'T WHITELISTED§3!");
					return true;
				}

				if (victim != null) {
					p = victim.getName();
				}

				simplelist.WhiteListedPlayers.remove(p
						.toLowerCase());
				String ConType = simplelist.Settings.getString("simplelist.connection");
				if(ConType.equals("file")){
					try{
						BufferedWriter fW = new BufferedWriter(new FileWriter(maindir + simplelist.Settings.getString("simplelist.file.name")));
						for(int i = 0; i< simplelist.WhiteListedPlayers.size(); i = i + 1){
							fW.write(simplelist.WhiteListedPlayers.get(i));
							fW.newLine();
						}
					    fW.close();
					}catch (Exception e){
						e.printStackTrace();
					}
				}else if(ConType.equals("mysql")){
					Connection conn = null;
					PreparedStatement ps = null;
					try {
						conn = connector.getSQLConnection();
						ps = conn.prepareStatement("DELETE FROM "
								+ Settings.getString("simplelist.mysql.table") + " WHERE " + Settings.getString("simplelist.mysql.field") + " = ?");
						ps.setString(1, p);
						ps.executeUpdate();
					} catch (SQLException ex) {
						ConsoleCommandSender console = getServer().getConsoleSender();
						console.sendMessage(ChatColor.YELLOW + "[SL-" + ChatColor.RED + "008" + ChatColor.YELLOW + "] "
								+ ChatColor.RED + "SQL statement couldn't be executed!");
						log.log(Level.SEVERE,
								"STATEMENT:",
								ex);
					} finally {
						try {
							if (ps != null)
								ps.close();
							if (conn != null)
								conn.close();
						} catch (SQLException ex) {
							ConsoleCommandSender console = getServer().getConsoleSender();
							console.sendMessage(ChatColor.YELLOW + "[SL-" + ChatColor.RED + "009" + ChatColor.YELLOW + "] "
									+ ChatColor.RED + "Failed to close connection to database!");
							log.log(Level.SEVERE,
									"STATEMENT:",
									ex);
						}
					}
				}else{
					ConsoleCommandSender console = getServer().getConsoleSender();
					console.sendMessage(ChatColor.YELLOW + "[SL-" + ChatColor.RED + "010" + ChatColor.YELLOW + "] " + ChatColor.DARK_AQUA + "The connection type you've chosen, \"" + ChatColor.YELLOW + simplelist.Settings.getString("simplelist.connection") + ChatColor.DARK_AQUA + "\" doesn't exist!");
					return true;
				}
				ConsoleCommandSender console = getServer().getConsoleSender();
				console.sendMessage(ChatColor.YELLOW + "[SL] "
						+ ChatColor.DARK_AQUA + (player == null ? "A console administrator" : player.getName())
						+ " de-whitelisted " + ChatColor.YELLOW + p + ChatColor.DARK_AQUA + "!");
				sender.sendMessage("§e[SL] §3" + p + ":§c DE-WHITELISTED§3!");
				return true;
			}
		}
		sender.sendMessage("§e[SL] §cInsufficient privelages!");
		return true;
	}

	public static void DebugPrint(String MSG) {
		if (Settings.getBoolean("simplelist.debug")) {
			log.log(Level.INFO, "[SL-debug] " + MSG);
		}
	}

	public static String[] RearangeString(int startIndex, String[] string) {
		String TMPString = "";
		for (int i = startIndex; i < string.length; i++) {
			String Add = " ";
			if (i == startIndex) {
				Add = "";
			}
			TMPString += Add + string[i];
		}
		return TMPString.split("\\ ");
	}

	public void RefreshWhitelist(Boolean First) {
		new UpdateWhitelist(First).run();
	}

	public long GetTime() {
		return System.currentTimeMillis() / 1000L;
	}

	class UpdateWhitelist extends Thread {

		private Boolean First;

		public UpdateWhitelist(Boolean First) {
			this.First = First;
		}

		public void run() {
			String ConType = simplelist.Settings.getString("simplelist.connection");
			if(ConType.equals("file") || ConType.equals("mysql")){
				ArrayList<String> TmpArray = new ArrayList<String>();
				TmpArray = GetWhitelist(ConType);
				if(!TmpArray.equals(null)){
					if(First){
						ConsoleCommandSender console = getServer().getConsoleSender();
						console.sendMessage(ChatColor.YELLOW + "[SL] " + ChatColor.DARK_AQUA + "SimpleList: " + ChatColor.GREEN + "LOADED " + ChatColor.DARK_AQUA + "!");
					}
					simplelist.WhiteListedPlayers = TmpArray;
				}
				TmpArray = null;
				return;
			}
			ConsoleCommandSender console = getServer().getConsoleSender();
			console.sendMessage(ChatColor.YELLOW + "[SL-" + ChatColor.RED + "010" + ChatColor.YELLOW + "] " + ChatColor.DARK_AQUA + "The connection type you've chosen, \"" + ChatColor.YELLOW + simplelist.Settings.getString("simplelist.connection") + ChatColor.DARK_AQUA + "\" doesn't exist!");
		}
	}
}