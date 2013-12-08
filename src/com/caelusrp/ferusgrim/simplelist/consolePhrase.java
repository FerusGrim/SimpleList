package com.caelusrp.ferusgrim.simplelist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class consolePhrase {
	public static YamlConfiguration consolePhrase;
		
	public static void SetDefault(String Path, Object Value){
			consolePhrase.set(Path, consolePhrase.get(Path, Value));
	}
		
	public static YamlConfiguration loadMain(boolean Create){
		String maindir = "plugins/simplelist/";
		File CPhrases = new File (maindir + "console_phrases.yml");
		try {
			consolePhrase = new YamlConfiguration();
			if (Create == false){
				consolePhrase.load(CPhrases);
			}
			SetDefault("ConsolePhrases.Whitelist.Active", "Whitelist is active!");
			SetDefault("ConsolePhrases.Whitelist.NotActive", "Whitelist is not active!");
			SetDefault("ConsolePhrases.Whitelist.Loaded", "Whitelist loaded!");
			SetDefault("ConsolePhrases.Whitelist.DeniedAccess", "{player} was denied!");
			SetDefault("ConsolePhrases.Toggle.WhitelistOn.IfConsole", "A console administrator toggled the whitelist on!");
			SetDefault("ConsolePhrases.Toggle.WhitelistOn.IfPlayer", "{player} toggled the whitelist on!");
			SetDefault("ConsolePhrases.Toggle.WhitelistOff.IfConsole", "A console administrator toggled the whitelist off!");
			SetDefault("ConsolePhrases.Toggle.WhitelistOff.IfPlayer", "{player} toggled the whitelist off!");
			SetDefault("ConsolePhrases.Refresh.IfConsole", "A console administrator refreshed the whitelist!");
			SetDefault("ConsolePhrases.Refresh.IfPlayer", "{player} refreshed the whitelist!");
			SetDefault("ConsolePhrases.Reload.IfConsole", "A console administrator reloaded the whitelist!");
			SetDefault("ConsolePhrases.Reload.IfPlayer", "{player} reloaded the whitelist!");
			SetDefault("ConsolePhrases.AddUser.IfConsole", "A console administrator added {player} to the whitelist!");
			SetDefault("ConsolePhrases.AddUser.IfPlayer", "{whitelister} added {player} to the whitelist!");
			SetDefault("ConsolePhrases.RemoveUser.IfConsole", "A console administrator removed {player} from the whitelist!");
			SetDefault("ConsolePhrases.RemoveUser.IfPlayer","{whitelister} removed {player} from the whitelist!");
			consolePhrase.save(CPhrases);
			return consolePhrase;
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