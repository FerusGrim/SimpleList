// Copyright Nicholas Badger (A.K.A. FerusGrim) //
// Attribution-NonCommercial-ShareAlike 4.0 International (CC BY-NC-SA 4.0) //

package com.caelusrp.ferusgrim.simplelist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class config {
    public static YamlConfiguration config;

    public static void SetDefault(String Path, Object Value){
        config.set(Path, config.get(Path, Value));
    }

    public static YamlConfiguration loadMain(boolean Create){
        String maindir = "plugins/SimpleList/";
        File Settings = new File(maindir + "config.yml");
        try {
            config = new YamlConfiguration();
            if(Create == false){
                config.load(Settings);
            }
            SetDefault("simplelist.enabled", true);
            SetDefault("simplelist.connection", "mysql");
            SetDefault("simplelist.debug", false);
            SetDefault("simplelist.notification.disallow-message", "§e[SL]§f Sorry, {player}, but you aren't whitelisted.");
            SetDefault("simplelist.notification.print-failure", true);
            SetDefault("simplelist.notification.failure-message", "§e[SL]§f {player} attempted to join.");
            SetDefault("simplelist.file.name", "players.txt");
            SetDefault("simplelist.file.update-interval", "10");
            SetDefault("simplelist.mysql.host", "localhost");
            SetDefault("simplelist.mysql.port", 3306);
            SetDefault("simplelist.mysql.username", "root");
            SetDefault("simplelist.mysql.password", "toor");
            SetDefault("simplelist.mysql.database", "whitelist");
            SetDefault("simplelist.mysql.table", "users");
            SetDefault("simplelist.mysql.field", "username");
            SetDefault("simplelist.mysql.query", "SELECT {field} FROM `{table}`;");
            config.save(Settings);
            return config;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }
}