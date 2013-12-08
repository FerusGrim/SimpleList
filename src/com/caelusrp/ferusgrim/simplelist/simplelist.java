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
	public static YamlConfiguration Phrases;
	public static YamlConfiguration CPhrases;
	static File SettingsFile = new File(maindir + "config.yml");
	static File PhrasesFile = new File(maindir + "phrases.yml");
	static File CPhrasesFile = new File(maindir + "console_phrases.yml");
	static ArrayList<String> WhiteListedPlayers = new ArrayList<String>();
    int RefreshWhitelistTaskID = -1;
	static boolean WhitelistON = true;

	public void onDisable() {
		//Clears the whitelist array to clear up memory
		WhiteListedPlayers = new ArrayList<String>();
		//Clears configurations to clear up memory
		Settings = null;
		Phrases = null;
		CPhrases = null;

		this.getServer().getScheduler().cancelAllTasks();
        RefreshWhitelistTaskID = -1;
		//Shuts the whitelist down cleanly, preventing errors on "stop" command.
        //Only fixes the error when using 'file' connection type.
        //TODO fix onDisable errors for mySQL.
        //LOOK INTO - forcing connection close, onDisable?
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
		if (!PhrasesFile.exists()) {
			//Will create the Phrases file if it doesn't exist
			try {
				PhrasesFile.createNewFile();
				Phrases = phrase.loadMain(true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			Phrases = phrase.loadMain(false);
		}
		if (!CPhrasesFile.exists()) {
			//Will create the CPhrases file if it doesn't exist
			try {
				CPhrasesFile.createNewFile();
				CPhrases = consolePhrase.loadMain(true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			CPhrases = consolePhrase.loadMain(false);
		}
		File WhitelistFile = new File(maindir + simplelist.Settings.getString("SimpleList.File.Name"));
		if (!WhitelistFile.exists()) {
			//Will create the Whitelist file if it doesn't exist
			try {
				WhitelistFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// Sets the whitelists mode
		WhitelistON = simplelist.Settings.getBoolean("SimpleList.Enabled");

		PluginManager pm = getServer().getPluginManager();

		//Registers the Listener class
		pm.registerEvents(new listener(), this);
		ConsoleCommandSender console = getServer().getConsoleSender();
		console.sendMessage((WhitelistON == true ? simplelist.CPhrases.getString("ConsolePhrases.Whitelist.Active") : simplelist.CPhrases.getString("ConsolePhrases.Whitelist.NotActive")));
		DebugPrint("§8You're seeing debugging messages! To deactivate these, set \"debug\" to 'false'.");
		RefreshWhitelist(true);
        if(RefreshWhitelistTaskID < 0){
                RefreshWhitelistTaskID = getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
                        public void run() {
                                RefreshWhitelist(false);
                        }
                }, 0, Settings.getInt("SimpleList.File.Update-Interval") * 20);
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
		Player.sendMessage(MSG);
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
			console.sendMessage(ChatColor.YELLOW + "[SimpleList] "
					+ (player == null ?  simplelist.CPhrases.getString("ConsolePhrases.Toggle.WhitelistOn.IfConsole") : simplelist.CPhrases.getString("ConsolePhrases.Toggle.WhitelistOn.IfPlayer")
					.replace("{player}", player.getName())));
			sender.sendMessage(simplelist.Phrases.getString("Phrases.Toggle.WhitelistOn"));
			return true;
		}
		sender.sendMessage(simplelist.Phrases.getString("Phrases.NoPermissions"));
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
			console.sendMessage(ChatColor.YELLOW + "[SimpleList] "
					+ (player == null ?  simplelist.CPhrases.getString("ConsolePhrases.Toggle.WhitelistOff.IfConsole") : simplelist.CPhrases.getString("ConsolePhrases.Toggle.WhitelistOff.IfPlayer")
					.replace("{player}", player.getName())));
			sender.sendMessage(simplelist.Phrases.getString("Phrases.Toggle.WhitelistOff"));
			return true;
		}
		sender.sendMessage(simplelist.Phrases.getString("Phrases.NoPermissions"));
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
			console.sendMessage(ChatColor.YELLOW + "[SimpleList] "
					+ (player == null ?  simplelist.CPhrases.getString("ConsolePhrases.Refresh.IfConsole") : simplelist.CPhrases.getString("ConsolePhrases.Refresh.IfPlayer")
					.replace("{player}", player.getName())));
			sender.sendMessage(simplelist.Phrases.getString("Phrases.Refresh"));
			(new UpdateWhitelist(false)).start();
			return true;
		}
		sender.sendMessage(simplelist.Phrases.getString("Phrases.NoPermissions"));
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
			console.sendMessage(ChatColor.YELLOW + "[SimpleList] "
					+ (player == null ?  simplelist.CPhrases.getString("ConsolePhrases.Reload.IfConsole") : simplelist.CPhrases.getString("ConsolePhrases.Reload.IfPlayer")
					.replace("{player}", player.getName())));
			sender.sendMessage(simplelist.Phrases.getString("Phrases.Reload"));
			return true;
		}
		sender.sendMessage(simplelist.Phrases.getString("Phrases.NoPermissions"));
		return true;
	}

	private ArrayList<String> GetWhitelist(String Type){
		ArrayList<String> tmpArray = new ArrayList<String>();
		if(Type.equals("file")){
			FileInputStream in;
			try {
				in = new FileInputStream(maindir + simplelist.Settings.getString("SimpleList.File.Name"));

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
					log.log(Level.SEVERE,
							"[SIMPLELIST] ERROR #101 : COULD NOT ESTABLISH SQL CONNECTION!");
					return null;
				} else {

					PreparedStatement ps = null;
					ResultSet rs = null;
					try {
						String Query = simplelist.Settings.getString("SimpleList.MySQL.Database.Query");
						Query = Query.replace("{Table}",
								simplelist.Settings.getString("SimpleList.MySQL.Database.Table"));
						Query = Query.replace("{Field}",
								simplelist.Settings.getString("SimpleList.MySQL.Database.Field"));
						Query = Query.replace("{time}", "" + GetTime());
						ps = conn.prepareStatement(Query);
						rs = ps.executeQuery();
						while (rs.next()) {
							tmpArray.add(rs.getString(
									simplelist.Settings.getString("SimpleList.MySQL.Database.Field")).toLowerCase());
						}
						DebugPrint("Whitelist (type:" + Type +" count: " + tmpArray.toArray().length + ")");
						return tmpArray;
					} catch (SQLException ex) {
						log.log(Level.SEVERE,
								"[SIMPLELIST] ERROR #102 : SQL STATEMENT COULD NOT BE EXECUTED!",
								ex);
					} finally {
						try {
							if (ps != null)
								ps.close();
							if (conn != null)
								conn.close();
						} catch (SQLException ex) {
							log.log(Level.SEVERE,
									"[SIMPLELIST] ERROR #103 : FAILED TO CLOSE SQL DATABASE!",
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
			log.log(Level.SEVERE,
					"[SIMPLELIST] ERROR #104 : CONNECTION TYPE, \"" + simplelist.Settings.getString("SimpleList.DatabaseType") + "\" DOES NOT EXIST!");
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
					sender.sendMessage(simplelist.Phrases.getString("Phrases.AddUser.Whitelisted")
							.replace("{player}", p));
					return true;
				}

				simplelist.WhiteListedPlayers.add(p.toLowerCase());
				String ConType = simplelist.Settings.getString("SimpleList.DatabaseType");
				if(ConType.equals("file")){
						try{
							BufferedWriter fW = new BufferedWriter(new FileWriter(maindir + simplelist.Settings.getString("SimpleList.File.Name")));
							for(int i = 0; i< simplelist.WhiteListedPlayers.size(); i = i + 1){
								fW.write(simplelist.WhiteListedPlayers.get(i));
								fW.newLine();
							}
						    fW.close();
						}catch (Exception e){
							e.printStackTrace();
						}
						ConsoleCommandSender console = getServer().getConsoleSender();
						console.sendMessage(ChatColor.YELLOW + "[SimpleList] " + (player == null ? simplelist.CPhrases.getString("ConsolePhrases.AddUser.IfConsole") 
								.replace("{player}", p) : simplelist.CPhrases.getString("ConsolePhrases.AddUser.IfPlayer")
								.replace("{player}", p)
								.replace("{whitelister}", player.getName())));
						sender.sendMessage(simplelist.Phrases.getString("Phrases.AddUser.Success")
								.replace("{player}", p));
						return true;
				}else if(ConType.equals("mysql")){
						Connection conn = null;
						PreparedStatement ps = null;
						try {
							conn = connector.getSQLConnection();
							ps = conn.prepareStatement("INSERT INTO "
									+ simplelist.Settings.getString("SimpleList.MySQL.Database.Table") + " (" + simplelist.Settings.getString("SimpleList.MySQL.Database.Field") + ") VALUES(?)");
							ps.setString(1, p);
							ps.executeUpdate();
						} catch (SQLException ex) {
							log.log(Level.SEVERE,
									"[SIMPLELIST] ERROR #005 : SQL STATEMENT COULD NOT BE EXECUTED!",
									ex);
						} finally {
							try {
								if (ps != null)
									ps.close();
								if (conn != null)
									conn.close();
							} catch (SQLException ex) {
								log.log(Level.SEVERE,
										"[SIMPLELIST] ERROR #106 : FAILED TO CLOSE SQL DATABASE!",
										ex);
							}
						}

						ConsoleCommandSender console = getServer().getConsoleSender();
						console.sendMessage(ChatColor.YELLOW + "[SimpleList] " + (player == null ? simplelist.CPhrases.getString("ConsolePhrases.AddUser.IfConsole") 
								.replace("{player}", p) : simplelist.CPhrases.getString("ConsolePhrases.AddUser.IfPlayer")
								.replace("{player}", p)
								.replace("{whitelister}", player.getName())));
						sender.sendMessage(simplelist.Phrases.getString("Phrases.AddUser.Success")
								.replace("{player}", p));
						return true;
				}else{
					log.log(Level.SEVERE,
							"[SIMPLELIST] ERROR #107 : CONNECTION TYPE, \"" + simplelist.Settings.getString("SimpleList.DatabaseType") + "\" DOES NOT EXIST!");
					
				}
			}
		}
		sender.sendMessage(simplelist.Phrases.getString("Phrases.NoPermissions"));
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
					sender.sendMessage(simplelist.Phrases.getString("Phrases.RemoveUser.NotWhitelisted")
							.replace("{player}", p));
					return true;
				}

				if (victim != null) {
					p = victim.getName();
				}

				simplelist.WhiteListedPlayers.remove(p
						.toLowerCase());
				String ConType = simplelist.Settings.getString("SimpleList.DatabaseType");
				if(ConType.equals("file")){
					try{
						BufferedWriter fW = new BufferedWriter(new FileWriter(maindir + simplelist.Settings.getString("SimpleList.File.Name")));
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
								+ simplelist.Settings.getString("SimpleList.MySQL.Database.Table") + " WHERE " + simplelist.Settings.getString("SimpleList.MySQL.Database.Field") + " = ?");
						ps.setString(1, p);
						ps.executeUpdate();
					} catch (SQLException ex) {
						log.log(Level.SEVERE,
								"[SIMPLELIST] ERROR #008 : SQL STATEMENT COULD NOT BE EXECUTED!",
								ex);
					} finally {
						try {
							if (ps != null)
								ps.close();
							if (conn != null)
								conn.close();
						} catch (SQLException ex) {
							log.log(Level.SEVERE,
									"[SIMPLELIST] ERROR #009 : FAILED TO CLOSE SQL DATABASE!",
									ex);
						}
					}
				}else{
					log.log(Level.SEVERE,
							"[SIMPLELIST] ERROR #110 : CONNECTION TYPE, \"" + simplelist.Settings.getString("SimpleList.DatabaseType") + "\" DOES NOT EXIST!");
					return true;
				}
				ConsoleCommandSender console = getServer().getConsoleSender();
				console.sendMessage(ChatColor.YELLOW + "[SimpleList] " + (player == null ? simplelist.CPhrases.getString("ConsolePhrases.RemoveUser.IfConsole") 
						.replace("{player}", p) : simplelist.CPhrases.getString("ConsolePhrases.RemoveUser.IfPlayer")
						.replace("{player}", p)
						.replace("{whitelister}", player.getName())));
				sender.sendMessage(simplelist.Phrases.getString("Phrases.RemoveUser.Success")
						.replace("{player}", p));
				return true;
			}
		}
		sender.sendMessage(simplelist.Phrases.getString("Phrases.NoPermissions"));
		return true;
	}

	public static void DebugPrint(String MSG) {
		if (Settings.getBoolean("simplelist.debug")) {
			log.log(Level.INFO, "[SIMPLELIST-DEBUG] " + MSG);
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
			String ConType = simplelist.Settings.getString("SimpleList.DatabaseType");
			if(ConType.equals("file") || ConType.equals("mysql")){
				ArrayList<String> TmpArray = new ArrayList<String>();
				TmpArray = GetWhitelist(ConType);
				if(!TmpArray.equals(null)){
					if(First){
						ConsoleCommandSender console = getServer().getConsoleSender();
						console.sendMessage(ChatColor.YELLOW + "[SimpleList] " + simplelist.CPhrases.getString("ConsolePhrases.Whitelist.Loaded"));
					}
					simplelist.WhiteListedPlayers = TmpArray;
				}
				TmpArray = null;
				return;
			}
			log.log(Level.SEVERE,
					"[SIMPLELIST] ERROR #111 : CONNECTION TYPE, \"" + simplelist.Settings.getString("SimpleList.DatabaseType") + "\" DOES NOT EXIST!");
		}
	}
}