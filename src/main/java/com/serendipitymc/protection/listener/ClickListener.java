package com.serendipitymc.protection.listener;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.db.TownyDataSource;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.WorldCoord;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.serendipitymc.protection.protect.protect;

public class ClickListener implements Listener {
	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled = true)
	public void canClick(PlayerInteractEvent pie) {
		try {
			Location playerLocation = pie.getPlayer().getLocation();
			Player player = pie.getPlayer();
			TownyWorld World = TownyUniverse.getDataSource().getWorld(playerLocation.getWorld().getName());
			WorldCoord playerCoord = new WorldCoord(World.getName(), Coord.parseCoord(playerLocation));
			
			if (player.hasPermission("serendipity.townlock.bypass"))
				return;
			if (!(playerCoord.getTownyWorld().isUsingTowny()))
				return;
			if (TownyUniverse.isWilderness(playerLocation.getBlock()))
				return;
			if (!(playerCoord.getTownBlock().hasTown()))
				return;
			
			String atTown = TownyUniverse.getTownName(playerLocation);
			
			if ((!(protect.hasTownSettings(atTown + ".noclick"))) || (!(protect.isTPEnabled())))
				return;
			
			TownBlock townb = playerCoord.getTownBlock();
			Town town = townb.getTown();
			Resident curPlayer = TownyUniverse.getDataSource().getResident(pie.getPlayer().getName());
			
			if (town.hasResident(curPlayer))
				return;
				
			town = null;
			townb = null;
			
			if ((pie.getAction() == Action.LEFT_CLICK_AIR) || (pie.getAction() == Action.LEFT_CLICK_BLOCK)) {
				TownyMessaging.sendMsg(pie.getPlayer(), "You are not allowed to do that here");
				pie.setCancelled(true);
			}
			else if ((pie.getAction() == Action.RIGHT_CLICK_AIR) || (pie.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				TownyMessaging.sendMsg(pie.getPlayer(), "You are not allowed to do that here");
				pie.setCancelled(true);
			}
		}
		catch (Exception e) {
			pie.getPlayer().sendMessage("Please let a staff member know error: 0xfe80");
		}
	}
	
	@EventHandler(priority=EventPriority.HIGH, ignoreCancelled = true)
	public void playerIsMoving(PlayerMoveEvent event) {
		if ((event.getFrom().getBlockX() == event.getTo().getBlockX()) && (event.getFrom().getBlockZ() == event.getTo().getBlockZ()) && (event.getFrom().getBlockY() == event.getTo().getBlockY()))
			return;
		if (event.getPlayer().hasPermission("serendipity.townlock.bypass"))
			return;
			
			Player player = event.getPlayer();
			Location from = event.getFrom();
			Location to = event.getTo();
			
			try {
				TownyWorld toWorld = TownyUniverse.getDataSource().getWorld(to.getWorld().getName());
				WorldCoord toCoord = new WorldCoord(toWorld.getName(), Coord.parseCoord(to));
				
				if (!(toCoord.getTownyWorld().isUsingTowny()))
					return;
				if (TownyUniverse.isWilderness(to.getBlock()))
					return;
				if (!(toCoord.getTownBlock().hasTown()))
					return;
				
				String atTown = TownyUniverse.getTownName(to);
				
				if ((!(protect.hasTownSettings(atTown + ".lockout"))) || (!(protect.isTPEnabled())))
					return;
					
				TownBlock townb = toCoord.getTownBlock();
				Town town = townb.getTown();
				Resident curPlayer = TownyUniverse.getDataSource().getResident(player.getName());
				
				if (town.hasResident(curPlayer))
					return;
				
				town = null;
				townb = null;
				
				TownyMessaging.sendMsg(player, "You are not allowed there");
				player.teleport(from);
				return;
			}
			catch (NotRegisteredException localNotRegisteredException) {}
	}
}
