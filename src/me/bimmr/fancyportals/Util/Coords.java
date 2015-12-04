package me.bimmr.fancyportals.Util;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class Coords {
	private String world;
	private int x;
	private int y;
	private int z;
	private float pitch;
	private float yaw;

	public Coords(Location loc) {
		setWorld(loc.getWorld());
		setX(loc.getBlockX());
		setY(loc.getBlockY());
		setZ(loc.getBlockZ());
		setYaw(loc.getYaw());
		setPitch(loc.getPitch());
	}

	public Coords(String string) {
		String[] list = string.split(",");
		this.world = list[0];
		this.x = Integer.parseInt(list[1]);
		this.y = Integer.parseInt(list[2]);
		this.z = Integer.parseInt(list[3]);

		if (StringUtils.countMatches(string, ",") == 5) {
			setYaw(Float.parseFloat(list[4]));
			setPitch(Float.parseFloat(list[5]));
		}
	}

	public Coords(World world, int x, int y, int z) {
		setWorld(world);
		setX(x);
		setY(y);
		setZ(z);
	}

	public Coords(World world, int x, int y, int z, float yaw, float pitch) {
		setWorld(world);
		setX(x);
		setY(y);
		setZ(z);
	}

	public Location asLocation() {
		return new Location(Bukkit.getWorld(this.world), this.x, this.y, this.z, this.yaw, this.pitch);
	}

	public Location asLocationIgnoreYawAndPitch() {
		return new Location(Bukkit.getWorld(this.world), this.x, this.y, this.z);
	}

	public String asString() {
		return this.world + "," + this.x + "," + this.y + "," + this.z + "," + this.yaw + "," + this.pitch;
	}

	public String asStringIgnoreYawAndPitch() {
		return this.world + "," + this.x + "," + this.y + "," + this.z;
	}

	public float getPitch() {
		return this.pitch;
	}

	public String getWorld() {
		return this.world;
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public float getYaw() {
		return this.yaw;
	}

	public int getZ() {
		return this.z;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	public void setWorld(World world) {
		this.world = world.getName();
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setYaw(float yaw) {
		this.yaw = yaw;
	}

	public void setZ(int z) {
		this.z = z;
	}
}