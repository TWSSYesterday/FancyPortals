
package com.sniperzciinema.portals.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.sniperzciinema.portals.Portals.Portal;


public class FancyPortalEnter extends Event implements Cancellable {

	private boolean						cancelled	= false;
	private Portal						portal;
	private Player						player;

	private static final HandlerList	handlers	= new HandlerList();

	public static HandlerList getHandlerList() {
		return FancyPortalEnter.handlers;
	}

	public FancyPortalEnter(Portal portal, Player player)
	{
		setPortal(portal);
		setPlayer(player);
	}

	@Override
	public HandlerList getHandlers() {
		return FancyPortalEnter.handlers;
	}

	/**
	 * @return the player
	 */
	public Player getPlayer() {
		return this.player;
	}

	/**
	 * @return the portal
	 */
	public Portal getPortal() {
		return this.portal;
	}

	/**
	 * @return the cancelled
	 */
	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	/**
	 * @param cancelled
	 *            the cancelled to set
	 */
	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	/**
	 * @param player
	 *            the player to set
	 */
	public void setPlayer(Player player) {
		this.player = player;
	}

	/**
	 * @param portal
	 *            the portal to set
	 */
	public void setPortal(Portal portal) {
		this.portal = portal;
	}
}
