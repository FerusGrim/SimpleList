package com.caelusrp.ferusgrim.simplelist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class phrase {
	public static YamlConfiguration phrase;
	
	public static void SetDefault(String Path, Object Value){
		phrase.set(Path, phrase.get(Path, Value));
	}
	
	public static YamlConfiguration loadMain(boolean Create){
		String maindir = "plugins/simplelist/";
		File Phrases = new File(maindir + "phrases.yml");
		try {
			phrase = new YamlConfiguration();
			if(Create == false){
				phrase.load(Phrases);
			}
            SetDefault("Phrases.Refresh", "Whitelist has been refreshed!");
            SetDefault("Phrases.Reload", "Configuration has been reloaded!");
            SetDefault("Phrases.NoPermissions", "Insufficient privelages!");
            SetDefault("Phrases.NotifyFail.ShowFails", true);
            SetDefault("Phrases.NotifyFail.ToMod", "User, {player}, attempted to join!");
            SetDefault("Phrases.NotifyFail.ToUser", "Sorry, {player}, but you aren't whitelisted!");
            SetDefault("Phrases.AddUser.Success", "User, {player}, added to the whitelist!");
            SetDefault("Phrases.AddUser.Whitelisted", "User, {player}, is already whitelisted!");
            SetDefault("Phrases.RemoveUser.Success", "User, {player}, removed from the whitelist!");
            SetDefault("Phrases.RemoveUser.NotWhitelisted", "User, {player}, isn't whitelisted!");
            SetDefault("Phrases.Toggle.WhitelistOn", "Whitelist is now active!");
            SetDefault("Phrases.Toggle.WhitelistOff", "Whitelist has been deactivated!");
			phrase.save(Phrases);
			return phrase;
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