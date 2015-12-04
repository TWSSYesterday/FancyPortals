package me.bimmr.fancyportals.Util;

import java.util.HashMap;
import java.util.UUID;

public class Timer {
	private static HashMap<UUID, Long> cooldowns = new HashMap<UUID, Long>();

	private static long time = 1L;

	public static void addToCooldown(UUID uuid) {
		cooldowns.put(uuid, Long.valueOf(System.currentTimeMillis()));
	}

	public static boolean isCooledDown(UUID uuid) {
		return (!cooldowns.containsKey(uuid)) || ((System.currentTimeMillis() - ((Long) cooldowns.get(uuid)).longValue()) / 1000L >= time);
	}

	public static long getTimeRemaining(UUID uuid) {
		return time - (System.currentTimeMillis() - ((Long) cooldowns.get(uuid)).longValue()) / 1000L;
	}
}