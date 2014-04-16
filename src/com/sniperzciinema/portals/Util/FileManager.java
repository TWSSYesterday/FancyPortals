
package com.sniperzciinema.portals.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;


public class FileManager {

	Plugin						plugin;
	private YamlConfiguration	portals;
	private File				portalsFile;

	public FileManager(Plugin plugin)
	{
		this.plugin = plugin;
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public FileConfiguration getConfig() {
		return this.plugin.getConfig();
	}

	public void saveConfig() {
		this.plugin.saveConfig();
	}

	public void reloadConfig() {
		this.plugin.reloadConfig();
	}

	public void reloadPortals() {
		if (this.portalsFile == null)
			this.portalsFile = new File(this.plugin.getDataFolder(), "Portals.yml");

		this.portals = YamlConfiguration.loadConfiguration(this.portalsFile);
		InputStream defConfigStream = this.plugin.getResource("Portals.yml");

		if (defConfigStream != null)
		{
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			if (!this.portalsFile.exists() || this.portalsFile.length() == 0)
				this.portals.setDefaults(defConfig);
		}
	}

	public FileConfiguration getPortals() {
		if (this.portals == null)
		{
			reloadPortals();
			savePortals();
		}
		return this.portals;
	}

	public void savePortals() {
		if (this.portals == null || this.portalsFile == null)
			return;
		try
		{
			getPortals().save(this.portalsFile);
		}
		catch (IOException ex)
		{
			Bukkit.getLogger().log(Level.SEVERE, "Could not save config " + this.portalsFile, ex);
		}
	}

}
