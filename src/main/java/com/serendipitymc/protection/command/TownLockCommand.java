package com.serendipitymc.protection.command;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.serendipitymc.protection.protect.protect;

public class TownLockCommand implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player)sender;
			parseCommand(player,args);
		}
		return true;
	}
	
	private void parseCommand(Player player, String[] split) {
		try {
			if (!(protect.isTPEnabled())) {
				player.sendMessage("SerenProtect is disabled");
				return;
			}
			if (split.length != 0) {
				Resident res = TownyUniverse.getDataSource().getResident(player.getName());
				Town resTown = res.getTown();
				Resident mayor = resTown.getMayor();
				String townName = resTown.getName();
				resTown = null;
				
				if (!(player.getName().equals(mayor.getName()))) {
					player.sendMessage("You have to be the mayor of your town to use this");
					return;
				}
				
				if (split[0].equalsIgnoreCase("noclick") || split[0].equalsIgnoreCase("lockout"))
					handleToggle(townName, split[0], player);
				else if ((split[0].equalsIgnoreCase("save")) && (player.hasPermission("serendipity.townlock.debug"))) {
					player.sendMessage("Saving the cache");
					protect.saveCacheFile(protect.cacheFile);
				}
				else if ((split[0].equalsIgnoreCase("load")) && (player.hasPermission("serendipity.townlock.debug"))) {
					player.sendMessage("Loading cache from disk");
					protect.loadCacheFile(protect.cacheFile);
				}
				else if (split[0].equalsIgnoreCase("status"))
					player.sendMessage(String.format("Townlock status [%s]: lockout: %s, noclick: %s", new Object[] { townName, Boolean.valueOf(protect.hasTownSettings(townName + ".lockout")), Boolean.valueOf(protect.hasTownSettings(townName + ".noclick")) } ));
			}
		}
		catch (Exception e) {
			TownyMessaging.sendErrorMsg(player, "You have to be the mayor of a town for this to work");
		}
	}
	
	private void handleToggle(String townName, String command, Player player) {
		if (command.equalsIgnoreCase("noclick"))
			protect.toggleTownSettings(townName + ".noclick", player);
		else if (command.equalsIgnoreCase("lockout"))
			protect.toggleTownSettings(townName + ".lockout", player);
	}
}
