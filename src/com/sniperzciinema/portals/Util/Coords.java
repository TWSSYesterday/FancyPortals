
package com.sniperzciinema.portals.Util;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;


public class Coords {

	private String	world;
	private int		x, y, z;
	private float	pitch, yaw;

	public Coords(Location loc)
	{
		setWorld(loc.getWorld());
		setX(loc.getBlockX());
		setY(loc.getBlockY());
		setZ(loc.getBlockZ());
		setYaw(loc.getYaw());
		setPitch(loc.getPitch());
	}

	public Coords(String string)
	{
		String[] list = string.split(",");
		this.world = list[0];
		this.x = Integer.parseInt(list[1]);
		this.y = Integer.parseInt(list[2]);
		this.z = Integer.parseInt(list[3]);

		if (StringUtils.countMatches(string, ",") == 5)
		{
			setYaw(Float.parseFloat(list[4]));
			setPitch(Float.parseFloat(list[5]));
		}
	}

	public Coords(World world, int x, int y, int z)
	{
		setWorld(world);
		setX(x);
		setY(y);
		setZ(z);
	}

	public Coords(World world, int x, int y, int z, float yaw, float pitch)
	{
		setWorld(world);
		setX(x);
		setY(y);
		setZ(z);
	}

	/**
	 * @return Coords as a location
	 */
	public Location asLocation() {
		return new Location(Bukkit.getWorld(this.world), this.x, this.y, this.z, this.yaw,
				this.pitch);
	}

	/**
	 * @return Coords as a location ignoreing yaw and pitch
	 */
	public Location asLocationIgnoreYawAndPitch() {
		return new Location(Bukkit.getWorld(this.world), this.x, this.y, this.z);
	}

	/**
	 * @return Coords as a string
	 */
	public String asString() {
		return this.world + "," + this.x + "," + this.y + "," + this.z + "," + this.yaw + "," + this.pitch;
	}

	/**
	 * @return Coords as a string ignoring yaw and pitch
	 */
	public String asStringIgnoreYawAndPitch() {
		return this.world + "," + this.x + "," + this.y + "," + this.z;
	}

	/**
	 * @return the pitch
	 */
	public float getPitch() {
		return this.pitch;
	}

	/**
	 * @return the world
	 */
	public String getWorld() {
		return this.world;
	}

	/**
	 * @return the x
	 */
	public int getX() {
		return this.x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return this.y;
	}

	/**
	 * @return the yaw
	 */
	public float getYaw() {
		return this.yaw;
	}

	/**
	 * @return the z
	 */
	public int getZ() {
		return this.z;
	}

	/**
	 * @param pitch
	 *            the pitch to set
	 */
	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	/**
	 * @param world
	 *            the world to set
	 */
	public void setWorld(World world) {
		this.world = world.getName();
	}

	/**
	 * @param x
	 *            the x to set
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * @param y
	 *            the y to set
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * @param yaw
	 *            the yaw to set
	 */
	public void setYaw(float yaw) {
		this.yaw = yaw;
	}

	/**
	 * @param z
	 *            the z to set
	 */
	public void setZ(int z) {
		this.z = z;
	}

}
