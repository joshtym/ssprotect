package com.serendipitymc.protection.protect;

import com.serendipitymc.protection.command.TownLockCommand;
import com.serendipitymc.protection.listener.ClickListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;

public final class protect extends JavaPlugin {
	public static Configuration config;
	public static File cacheFile;
	private static File cacheFolder;
	private static ConcurrentHashMap<String, Integer> cacheMap;
	
	public void onEnable() {
		cacheFolder = getDataFolder();
		File configFile = new File(cacheFolder, "config.yml");
		
		cacheFile = new File(cacheFolder, "cache.data");
		cacheMap = new ConcurrentHashMap();
		
		initConfigFile(configFile);
		loadCacheFile(cacheFile);
		
		getServer().getPluginManager().registerEvents(new ClickListener(), this);
		getCommand("townlock").setExecutor(new TownLockCommand());
		
		new BukkitRunnable() {
			public void run() {
				protect.saveCacheFile(protect.cacheFile);
			}
		}.runTaskTimer(this, 20L, 1200L);
	}
	
	public void onDisable() {
		saveCacheFile(cacheFile);
		config = null;
		cacheMap = null;
	}
	
	public void initConfigFile(File configFile) {
		if ((configFile instanceof File)) {
			if (!(configFile.exists())) {
				try {
					getDataFolder().mkdir();
					getConfig().options().copyDefaults(true);
					getConfig().options().header("Serendipity Protection");
					getConfig().options().copyHeader(true);
					saveConfig();
				}
				catch (Exception e) {
					e.getMessage();
				}
			}
			loadConfig(configFile);
		}
	}
	
	public void loadConfig(File configFile) {
		FileConfiguration cfile = getConfig();
		config = YamlConfiguration.loadConfiguration(configFile);
		
		if (!(config.contains("townprotection.enabled")))
			getConfig().addDefault("townprotection.enabled", "true");
			
		cfile.options().copyDefaults(true);
		getConfig().options().header("Serendipity Protection\n");
		getConfig().options().copyHeader(true);
		saveConfig();
	}
	
	public static void saveCacheFile(File cFile) {
		if (cFile.exists()) {
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(cFile));
				
				for (Map.Entry<String, Integer> entry : cacheMap.entrySet()) {
					writer.write((String)entry.getKey() + ", " + entry.getValue());
					writer.newLine();
				}
				writer.close();
			}
			catch (Exception e) {
				System.out.println("Couldn't write cache file: " + e.getMessage());
			}
		}
	}
	
	public static void loadCacheFile(File cFile) {
		if (!(cFile.exists())) {
			try {
				cFile.getParentFile().mkdirs();
				cFile.createNewFile();
			}
			catch (IOException e) {
				e.getMessage();
			}
		}
		
		try {
			Scanner scanner = new Scanner(new FileReader(cFile.getAbsolutePath()));
			
			while (scanner.hasNextLine()) {
				String[] col = scanner.nextLine().split(",");
				cacheMap.put(col[0], Integer.valueOf(Integer.parseInt(col[1])));
			}
			scanner.close();
		}
		catch (Exception e) {
			e.getMessage();
		}
	}
	
	public static boolean hasTownSettings(String townIdentifier) {
		try {
			if (((Integer)cacheMap.get(townIdentifier)).equals(Integer.valueOf(1)))
				return true;
			return false;
		}
		catch (Exception e) {}
		return false;
	}
	
	public static boolean toggleTownSettings(String townIdentifier, Player player) {
		int flagTypeIndex = townIdentifier.indexOf(".noclick");
		boolean isNoClick = true;
		
		if (flagTypeIndex == -1) {
			flagTypeIndex = townIdentifier.indexOf(".lockout");
			isNoClick = false;
		}
		
		try {
			if (((Integer)cacheMap.get(townIdentifier)).equals(Integer.valueOf(1))) {
				cacheMap.put(townIdentifier, Integer.valueOf(0));
				
				if (isNoClick)
					player.sendMessage("You've set noclick in " + townIdentifier.substring(0, flagTypeIndex) + " to false.");
				else
					player.sendMessage("You've set lockout in " + townIdentifier.substring(0, flagTypeIndex) + " to false.");
			}
			else {
				cacheMap.put(townIdentifier, Integer.valueOf(1));
				
				if (isNoClick)
					player.sendMessage("You've set noclick in " + townIdentifier.substring(0, flagTypeIndex) + " to true.");
				else
					player.sendMessage("You've set lockout in " + townIdentifier.substring(0, flagTypeIndex) + " to true.");
			}
		}
		catch (Exception e) {
			cacheMap.put(townIdentifier, Integer.valueOf(1));
		}
		return false;
	}
	
	public static boolean isTPEnabled() {
		if (config.getString("townprotection.enabled").equals("true"))
			return true;
		return false;
	}
}
