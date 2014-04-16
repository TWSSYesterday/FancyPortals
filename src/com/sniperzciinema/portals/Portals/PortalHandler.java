
package com.sniperzciinema.portals.Portals;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import com.sniperzciinema.portals.Util.BlockCrawler;
import com.sniperzciinema.portals.Util.Coords;
import com.sniperzciinema.portals.Util.FileManager;


public class PortalHandler {

	public enum PortalType
	{
		BUNGEE, LOCATION, SERVER_COMMAND, PLAYER_COMMAND;
	}

	private ArrayList<Portal>	portals;
	private Plugin				plugin;
	private FileManager			fileManager;

	public PortalHandler(Plugin plugin, FileManager fileManager, Logger log)
	{
		this.portals = new ArrayList<Portal>();
		this.plugin = plugin;
		this.fileManager = fileManager;

		if (fileManager.getPortals().get("Portals") != null)
			for (String portalName : fileManager.getPortals().getConfigurationSection("Portals").getKeys(false))
			{
				Portal portal = loadPortal(portalName);
				this.portals.add(portal);
				log.fine("Loaded Portal: " + portal.getName());
			}

	}

	public void savePortal(Portal portal) {
		String portalName = portal.getName();
		String bungeeTarget = portal.getBungeeTarget();
		String command = portal.getCommand();
		List<String> coords = portal.getCoords();
		String target = portal.getLocationTarget();
		PortalType type = portal.getType();

		this.fileManager.getPortals().set("Portals." + portalName, portalName);
		this.fileManager.getPortals().set("Portals." + portalName + ".Bungee Target", bungeeTarget);
		this.fileManager.getPortals().set("Portals." + portalName + ".Command", command);
		this.fileManager.getPortals().set("Portals." + portalName + ".Coords", coords);
		this.fileManager.getPortals().set("Portals." + portalName + ".Target", target);
		this.fileManager.getPortals().set("Portals." + portalName + ".Type", type.toString());

		this.fileManager.savePortals();
	}

	/**
	 * Load a portal from the config
	 * 
	 * @param string
	 * @return
	 */
	public Portal loadPortal(String name) {
		Portal portal = new Portal();
		portal.setName(name);
		portal.setBungeeTarget(this.fileManager.getPortals().getString("Portals." + name + ".Bungee Target"));
		portal.setCommand(this.fileManager.getPortals().getString("Portals." + name + ".Command"));
		List<String> list = this.fileManager.getPortals().getStringList("Portals." + name + ".Coords");
		ArrayList<String> coords = new ArrayList<String>();
		for (String s : list)
			coords.add(s);
		portal.setCoords(coords);
		portal.setLocationTarget(this.fileManager.getPortals().getString("Portals." + name + ".Target"));
		portal.setType(PortalType.valueOf(this.fileManager.getPortals().getString("Portals." + name + ".Type")));

		return portal;
	}

	/**
	 * Create a portal using bungee
	 * 
	 * @param name
	 * @param type
	 * @param blocks
	 * @param bungeeTarget
	 * @throws NotValidCoord
	 */
	public void createPortal(String name, PortalType type, ArrayList<String> coords, String arg) {

		Portal portal = new Portal(name, type, coords, arg);
		this.portals.add(portal);
		savePortal(portal);

	}

	/**
	 * Remove a portal
	 * 
	 * @param portal
	 */
	public void removePortal(Portal portal) {
		this.portals.remove(portal);

		this.fileManager.getPortals().set("Portals." + portal.getName(), null);
		this.plugin.saveConfig();
	}

	/**
	 * Get the portal
	 * 
	 * @param name
	 * @return the portal or null if it doesn't exist
	 */
	public Portal getPortal(String name) {
		for (Portal portal : this.portals)
			if (portal.getName().equalsIgnoreCase(name))
				return portal;
		return null;
	}

	public Portal getPortal(Location location) {
		for (Portal portal : this.portals)
			if (portal.getCoords().contains(new Coords(location).asStringIgnoreYawAndPitch()))
				return portal;
		return null;
	}

	public boolean hasPortals() {

		return !this.portals.isEmpty();
	}

	public ArrayList<Portal> getPortals() {
		return this.portals;
	}

	public ArrayList<String> getAdjacentBlocks(Location loc) {
		BlockCrawler blockSpider = new BlockCrawler(50);
		ArrayList<String> blockArray = new ArrayList<String>();
		blockSpider.start(loc.getBlock(), blockArray);
		return blockArray;
	}
}
