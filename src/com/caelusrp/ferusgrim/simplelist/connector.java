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
            return DriverManager.getConnection("jdbc:mysql://" + simplelist.Settings.getString("simplelist.mysql.host") + ":" + simplelist.Settings.getInt("simplelist.mysql.port") +"/" + simplelist.Settings.getString("simplelist.mysql.database") + "?autoReconnect=true&user=" + simplelist.Settings.getString("simplelist.mysql.username") + "&password=" + simplelist.Settings.getString("simplelist.mysql.password"));
        } catch (SQLException ex) {
        	simplelist.log.log(Level.SEVERE, "§e[SL]§c Connection to the server was refused!", ex);
        }
        return null;
    }

}