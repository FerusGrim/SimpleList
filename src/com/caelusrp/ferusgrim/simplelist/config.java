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
        String maindir = "plugins/simplelist/";
        File Settings = new File(maindir + "config.yml");
        try {
            config = new YamlConfiguration();
            if(Create == false){
                config.load(Settings);
            }
            SetDefault("SimpleList.Enabled", true);
            SetDefault("SimpleList.Debug-Mode", false);
            SetDefault("SimpleList.DatabaseType", "file");
            SetDefault("SimpleList.MySQL.Host", "localhost");
            SetDefault("SimpleList.MySQL.Port", 3306);
            SetDefault("SimpleList.MySQL.Username", "root");
            SetDefault("SimpleList.MySQL.Password", "toor");
            SetDefault("SimpleList.MySQL.Database.Database", "whitelist");
            SetDefault("SimpleList.MySQL.Database.Table", "users");
            SetDefault("SimpleList.MySQL.Database.Field", "username");
            SetDefault("SimpleList.MySQL.Database.Query", "SELECT {Field} FROM {Table};");
            SetDefault("SimpleList.File.Name", "players.txt");
            SetDefault("SimpleList.File.Update-Interval", 10);
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