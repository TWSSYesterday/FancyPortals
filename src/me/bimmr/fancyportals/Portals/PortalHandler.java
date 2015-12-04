package me.bimmr.fancyportals.Portals;

import java.util.ArrayList;
import java.util.List;

import me.bimmr.fancyportals.Util.BlockCrawler;
import me.bimmr.fancyportals.Util.Coords;
import me.bimmr.fancyportals.Util.FileManager;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public class PortalHandler {
	private ArrayList<Portal> portals;
	private Plugin plugin;
	private FileManager fileManager;

	public PortalHandler(Plugin plugin, FileManager fileManager) {
		this.portals = new ArrayList<Portal>();
		this.plugin = plugin;
		this.fileManager = fileManager;

		if (fileManager.getPortals().get("Portals") != null)
			for (String portalName : fileManager.getPortals().getConfigurationSection("Portals").getKeys(false)) {
				Portal portal = loadPortal(portalName);
				this.portals.add(portal);
				System.out.println("Loaded Portal: " + portal.getName());
			}
	}

	public void createPortal(String name, PortalType type, ArrayList<String> coords, String arg) {
		Portal portal = new Portal(name, type, coords, arg);
		this.portals.add(portal);
		savePortal(portal);
	}

	public ArrayList<String> getAdjacentBlocks(Location loc) {
		BlockCrawler blockSpider = new BlockCrawler(50);
		ArrayList<String> blockArray = new ArrayList<String>();
		blockSpider.start(loc.getBlock(), blockArray);
		return blockArray;
	}

	public Portal getPortal(Location location) {
		for (Portal portal : this.portals)
			if (portal.getCoords().contains(new Coords(location).asStringIgnoreYawAndPitch()))
				return portal;
		return null;
	}

	public Portal getPortal(String name) {
		for (Portal portal : this.portals)
			if (portal.getName().equalsIgnoreCase(name))
				return portal;
		return null;
	}

	public ArrayList<Portal> getPortals() {
		return this.portals;
	}

	public boolean hasPortals() {
		return !this.portals.isEmpty();
	}

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

	public void removePortal(Portal portal) {
		this.portals.remove(portal);

		this.fileManager.getPortals().set("Portals." + portal.getName(), null);
		this.plugin.saveConfig();
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

	public static enum PortalType {
		BUNGEE, LOCATION, SERVER_COMMAND, PLAYER_COMMAND;
	}
}