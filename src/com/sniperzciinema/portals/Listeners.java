
package com.sniperzciinema.portals;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.sniperzciinema.portals.Events.FancyPortalEnter;
import com.sniperzciinema.portals.Portals.Portal;
import com.sniperzciinema.portals.Portals.PortalHandler;
import com.sniperzciinema.portals.Portals.PortalHandler.PortalType;
import com.sniperzciinema.portals.Util.Coords;


public class Listeners implements Listener {

	private Plugin					plugin;
	private PortalHandler			portalHandler;

	private HashMap<String, Long>	cooldowns;
	private int						seconds;

	public Listeners(Plugin plugin, PortalHandler portalHandler)
	{
		this.plugin = plugin;
		this.portalHandler = portalHandler;
		this.cooldowns = new HashMap<String, Long>();
		this.seconds = 1;
	}

	@EventHandler
	public void onBlockPhysicsEvent(BlockPhysicsEvent e) {
		if (e.getBlock().getType() == Material.PORTAL)
			e.setCancelled(true);
	}

	@EventHandler
	public void onPlayerMoveEvent(PlayerMoveEvent e) {
		boolean isSameBlock = e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockY() == e.getTo().getBlockY() && e.getFrom().getBlockZ() == e.getTo().getBlockZ();
		if (!isSameBlock)
		{
			Player player = e.getPlayer();
			if (this.portalHandler.hasPortals())
				if (this.portalHandler.getPortal(e.getTo()) != null)
					if (player.hasPermission("Portals.Use"))
					{

						if (isCooledDown(player))
						{
							activateCooldown(player);

							Portal portal = this.portalHandler.getPortal(e.getTo());
							FancyPortalEnter fpEnter = new FancyPortalEnter(portal, player);
							Bukkit.getPluginManager().callEvent(fpEnter);

							if (!fpEnter.isCancelled())
								if (portal.getType() == PortalType.BUNGEE)
								{
									ByteArrayDataOutput out = ByteStreams.newDataOutput();
									out.writeUTF("Connect");
									out.writeUTF(portal.getBungeeTarget());
									player.sendPluginMessage(this.plugin, "BungeeCord", out.toByteArray());
								}
								else
									if (portal.getType() == PortalType.LOCATION)
									{
										Location target = player.getWorld().getSpawnLocation();

										target = new Coords(portal.getLocationTarget()).asLocation();

										player.teleport(target);
									}
									else
										if (portal.getType() == PortalType.SERVER_COMMAND)
										{
											String command = portal.getCommand();

											Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("<player>", player.getName()));

										}
										else
											if (portal.getType() == PortalType.PLAYER_COMMAND)
											{
												String command = portal.getCommand();

												player.performCommand(command);

											}

						}

					}
					else
						player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "Invalid Permissions!");
		}
	}

	public boolean isCooledDown(Player player) {
		if (!this.cooldowns.containsKey(player.getName()) || (System.currentTimeMillis() - this.cooldowns.get(player.getName())) / 1000 >= this.seconds)
			return true;
		else
			return false;
	}

	public void activateCooldown(Player player) {
		this.cooldowns.put(player.getName(), System.currentTimeMillis());
	}

}
