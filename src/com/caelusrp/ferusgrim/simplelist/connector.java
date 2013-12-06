// Copyright Nicholas Badger (A.K.A. FerusGrim) //
// Attribution-NonCommercial-ShareAlike 4.0 International (CC BY-NC-SA 4.0) //

package com.caelusrp.ferusgrim.simplelist;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

public class connector {

    public static Connection getSQLConnection() {
    	
        try {
            return DriverManager.getConnection("jdbc:mysql://" + simplelist.Settings.getString("simplelist.mysql.host") + ":" + simplelist.Settings.getInt("simplelist.mysql.port") +"/" + simplelist.Settings.getString("simplelist.mysql.database") + "?autoReconnect=true&user=" + simplelist.Settings.getString("simplelist.mysql.username") + "&password=" + simplelist.Settings.getString("simplelist.mysql.password"));
        } catch (SQLException ex) {
        	getServer();
			ConsoleCommandSender console = Bukkit.getConsoleSender();
			console.sendMessage(ChatColor.YELLOW + "[SL-" + ChatColor.RED + "011" + ChatColor.YELLOW + "] "
					+ ChatColor.RED + "Connection to the server was refused!");
			simplelist.log.log(Level.SEVERE,
					"STATEMENT:",
					ex);
        }
        return null;
    }

	private static void getServer() {
	}

}