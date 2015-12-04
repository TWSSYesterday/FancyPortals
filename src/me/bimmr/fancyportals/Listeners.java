package me.bimmr.fancyportals;

import java.util.Collection;
import java.util.Random;

import me.bimmr.fancyportals.Events.FancyPortalEnter;
import me.bimmr.fancyportals.Portals.Portal;
import me.bimmr.fancyportals.Portals.PortalHandler;
import me.bimmr.fancyportals.Util.Coords;
import me.bimmr.fancyportals.Util.Timer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class Listeners implements Listener {
	private Plugin plugin;
	private PortalHandler portalHandler;

	public Listeners(final Plugin plugin, final PortalHandler portalHandler) {
		super();
		this.plugin = plugin;
		this.portalHandler = portalHandler;
	}

	@EventHandler
	public void onPlayerMoveEvent(final PlayerMoveEvent e) {
		final boolean isSameBlock = e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockY() == e.getTo().getBlockY() && e.getFrom().getBlockZ() == e.getTo().getBlockZ();
		if (!isSameBlock) {
			final Player player = e.getPlayer();
			if (this.portalHandler.hasPortals() && this.portalHandler.getPortal(e.getTo()) != null) {
				if (player.hasPermission("FancyPortals.Use")) {
					if (Timer.isCooledDown(player.getUniqueId())) {
						Timer.addToCooldown(player.getUniqueId());
						final Portal portal = this.portalHandler.getPortal(e.getTo());
						final FancyPortalEnter fpEnter = new FancyPortalEnter(portal, player);
						Bukkit.getPluginManager().callEvent((Event) fpEnter);
						if (!fpEnter.isCancelled()) {
							if (portal.getType() == PortalHandler.PortalType.BUNGEE) {
								final ByteArrayDataOutput out = ByteStreams.newDataOutput();
								out.writeUTF("Connect");
								out.writeUTF(portal.getBungeeTarget());
								player.sendPluginMessage(this.plugin, "BungeeCord", out.toByteArray());
							} else if (portal.getType() == PortalHandler.PortalType.LOCATION) {
								Location target = player.getWorld().getSpawnLocation();
								target = new Coords(portal.getLocationTarget()).asLocation();
								player.teleport(target);
							} else if (portal.getType() == PortalHandler.PortalType.SERVER_COMMAND) {
								String command = portal.getCommand();
								command = command.replaceAll("<player>", player.getName());
								if (command.contains("@p")) {
									double closest = 2.147483647E9;
									Player closestP = player;
									Collection<? extends Player> onlinePlayers;
									for (int length = (onlinePlayers = Bukkit.getOnlinePlayers()).size(), i = 0; i < length; ++i) {
										final Player p = (Player) onlinePlayers.toArray()[i];
										final double distance = p.getLocation().distance(player.getLocation());
										if (distance < closest) {
											closest = distance;
											closestP = p;
										}
									}
									command = command.replaceAll("@p", closestP.getName());
								}
								if (command.contains("@r")) {
									final Random r = new Random();
									final Player ran = (Player) Bukkit.getOnlinePlayers().toArray()[r.nextInt(Bukkit.getOnlinePlayers().size())];
									command = command.replaceAll("@r", ran.getName());
								}
								if (command.contains("@a")) {
									Collection<? extends Player> onlinePlayers2;
									for (int length2 = (onlinePlayers2 = Bukkit.getOnlinePlayers()).size(), j = 0; j < length2; ++j) {
										final Player p2 = (Player) onlinePlayers2.toArray()[j];
										Bukkit.getServer().dispatchCommand((CommandSender) Bukkit.getConsoleSender(), command.replaceAll("@a", p2.getName()));
									}
								} else {
									Bukkit.getServer().dispatchCommand((CommandSender) Bukkit.getConsoleSender(), command);
								}
							} else if (portal.getType() == PortalHandler.PortalType.PLAYER_COMMAND) {
								String command = portal.getCommand();
								if (command.contains("@p")) {
									double closest = 2.147483647E9;
									Player closestP = player;
									Collection<? extends Player> onlinePlayers3;
									for (int length3 = (onlinePlayers3 = Bukkit.getOnlinePlayers()).size(), k = 0; k < length3; ++k) {
										final Player p = (Player) onlinePlayers3.toArray()[k];
										final double distance = p.getLocation().distance(player.getLocation());
										if (distance < closest) {
											closest = distance;
											closestP = p;
										}
									}
									command = command.replaceAll("@p", closestP.getName());
								}
								if (command.contains("@r")) {
									final Random r = new Random();
									final Player ran = (Player) Bukkit.getOnlinePlayers().toArray()[r.nextInt(Bukkit.getOnlinePlayers().size())];
									command = command.replaceAll("@r", ran.getName());
								}
								if (command.contains("@a")) {
									Collection<? extends Player> onlinePlayers4;
									for (int length4 = (onlinePlayers4 = Bukkit.getOnlinePlayers()).size(), l = 0; l < length4; ++l) {
										final Player p2 = (Player) onlinePlayers4.toArray()[l];
										player.performCommand(command.replaceAll("@a", p2.getName()));
									}
								} else {
									player.performCommand(command);
								}
							}
						}
					}
				} else {
					player.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD).append("Invalid Permissions!").toString());
				}
			}
		}
	}
}
