package me.bimmr.fancyportals.Events;

import me.bimmr.fancyportals.Portals.Portal;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FancyPortalEnter extends Event implements Cancellable {

	private boolean cancelled = false;
	private Portal portal;
	private Player player;
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public FancyPortalEnter(Portal portal, Player player) {
		setPortal(portal);
		setPlayer(player);
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public Player getPlayer() {
		return this.player;
	}

	public Portal getPortal() {
		return this.portal;
	}

	public boolean isCancelled() {
		return this.cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void setPortal(Portal portal) {
		this.portal = portal;
	}
}