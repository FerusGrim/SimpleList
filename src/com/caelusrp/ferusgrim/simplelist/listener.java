// Copyright Nicholas Badger (A.K.A. FerusGrim) //
// Attribution-NonCommercial-ShareAlike 4.0 International (CC BY-NC-SA 4.0) //

package com.caelusrp.ferusgrim.simplelist;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.entity.Player;


public class listener implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(PlayerLoginEvent event){
        if( simplelist.WhitelistON == true){
            Player player = event.getPlayer();
            //Checks if player is on the whitelist
            if(event.getResult() == Result.ALLOWED){
                if(!simplelist.WhiteListedPlayers.contains(player.getName().toLowerCase())){
                    //Kick message
                    String DisMSG = simplelist.Settings.getString("simplelist.notification.disallow-message")
                            .replace("{player}", player.getName());
                    event.setKickMessage(DisMSG);
                    event.setResult(PlayerLoginEvent.Result.KICK_WHITELIST);
                	getServer();
					ConsoleCommandSender console = Bukkit.getConsoleSender();
                    console.sendMessage(ChatColor.YELLOW + "[SL] " + ChatColor.DARK_AQUA + player.getName() + ": " + ChatColor.RED + "DENIED" + ChatColor.DARK_AQUA + "!");
                    //Check if print connection failures is enabled
                    if(simplelist.Settings.getBoolean("simplelist.notification.print-failure") == true){
                        Bukkit.getServer().broadcast("§e[SL] §3" + simplelist.Settings.getString("simplelist.notification.failure-message").replace("{player}", player.getName()), "simplelist.displayfails");
                    }
                }
            }else{
                simplelist.DebugPrint("[SL-DEBUG] \"" + player.getName() + "\" was already denied access by another plugin.");
            }
        }
    }

	private Bukkit getServer() {
		return null;
	}
}