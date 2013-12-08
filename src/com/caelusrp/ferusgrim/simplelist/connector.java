// Copyright Nicholas Badger (A.K.A. FerusGrim) //
// Attribution-NonCommercial-ShareAlike 4.0 International (CC BY-NC-SA 4.0) //

package com.caelusrp.ferusgrim.simplelist;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.logging.Level;

public class connector {

    public static Connection getSQLConnection() {
    	
        try {
            return DriverManager.getConnection("jdbc:mysql://" + 
            		simplelist.Settings.getString("SimpleList.MySQL.Host") + ":" + 
            		simplelist.Settings.getInt("SimpleList.MySQL.Port") +"/" + 
            		simplelist.Settings.getString("SimpleList.MySQL.Database.Database") + "?autoReconnect=true&user=" + 
            		simplelist.Settings.getString("SimpleList.MySQL.Username") + "&password=" + 
            		simplelist.Settings.getString("SimpleList.MySQL.Password"));
        } catch (SQLException ex) {
        	getServer();
			simplelist.log.log(Level.SEVERE,
					"[SIMPLELIST] ERROR #112 : CONNECTION TO THE SQL SERVER WAS REFUSED!",
					ex);
        }
        return null;
    }

	private static void getServer() {
	}

}