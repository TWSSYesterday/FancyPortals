
package com.sniperzciinema.portals.Util.FancyMessage;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Achievement;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.Statistic.Type;
import org.bukkit.craftbukkit.libs.com.google.gson.stream.JsonWriter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public class FancyMessage {

	private final List<MessagePart>	messageParts;
	private String					jsonString;
	private boolean					dirty;

	private Class<?>				nmsChatSerializer		= Reflection.getNMSClass("ChatSerializer");
	private Class<?>				nmsTagCompound			= Reflection.getNMSClass("NBTTagCompound");
	private Class<?>				nmsPacketPlayOutChat	= Reflection.getNMSClass("PacketPlayOutChat");
	private Class<?>				nmsAchievement			= Reflection.getNMSClass("Achievement");
	private Class<?>				nmsStatistic			= Reflection.getNMSClass("Statistic");
	private Class<?>				nmsItemStack			= Reflection.getNMSClass("ItemStack");

	private Class<?>				obcStatistic			= Reflection.getOBCClass("CraftStatistic");
	private Class<?>				obcItemStack			= Reflection.getOBCClass("inventory.CraftItemStack");

	public FancyMessage(final String firstPartText)
	{
		this.messageParts = new ArrayList<MessagePart>();
		this.messageParts.add(new MessagePart(firstPartText));
		this.jsonString = null;
		this.dirty = false;
	}

	public FancyMessage color(final ChatColor color) {
		if (!color.isColor())
			throw new IllegalArgumentException(color.name() + " is not a color");
		latest().color = color;
		this.dirty = true;
		return this;
	}

	public FancyMessage style(final ChatColor... styles) {
		for (final ChatColor style : styles)
			if (!style.isFormat())
				throw new IllegalArgumentException(style.name() + " is not a style");
		latest().styles = styles;
		this.dirty = true;
		return this;
	}

	public FancyMessage file(final String path) {
		onClick("open_file", path);
		return this;
	}

	public FancyMessage link(final String url) {
		onClick("open_url", url);
		return this;
	}

	public FancyMessage suggest(final String command) {
		onClick("suggest_command", command);
		return this;
	}

	public FancyMessage command(final String command) {
		onClick("run_command", command);
		return this;
	}

	public FancyMessage achievementTooltip(final String name) {
		onHover("show_achievement", "achievement." + name);
		return this;
	}

	public FancyMessage achievementTooltip(final Achievement which) {
		try
		{
			Object achievement = Reflection.getMethod(this.obcStatistic, "getNMSAchievement").invoke(null, which);
			return achievementTooltip((String) Reflection.getField(this.nmsAchievement, "name").get(achievement));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return this;
		}
	}

	public FancyMessage statisticTooltip(final Statistic which) {
		Type type = which.getType();
		if (type != Type.UNTYPED)
			throw new IllegalArgumentException(
					"That statistic requires an additional " + type + " parameter!");
		try
		{
			Object statistic = Reflection.getMethod(this.obcStatistic, "getNMSStatistic").invoke(null, which);
			return achievementTooltip((String) Reflection.getField(this.nmsStatistic, "name").get(statistic));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return this;
		}
	}

	public FancyMessage statisticTooltip(final Statistic which, Material item) {
		Type type = which.getType();
		if (type == Type.UNTYPED)
			throw new IllegalArgumentException("That statistic needs no additional parameter!");
		if (type == Type.BLOCK && item.isBlock() || type == Type.ENTITY)
			throw new IllegalArgumentException(
					"Wrong parameter type for that statistic - needs " + type + "!");
		try
		{
			Object statistic = Reflection.getMethod(this.obcStatistic, "getMaterialStatistic").invoke(null, which, item);
			return achievementTooltip((String) Reflection.getField(this.nmsStatistic, "name").get(statistic));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return this;
		}
	}

	public FancyMessage statisticTooltip(final Statistic which, EntityType entity) {
		Type type = which.getType();
		if (type == Type.UNTYPED)
			throw new IllegalArgumentException("That statistic needs no additional parameter!");
		if (type != Type.ENTITY)
			throw new IllegalArgumentException(
					"Wrong parameter type for that statistic - needs " + type + "!");
		try
		{
			Object statistic = Reflection.getMethod(this.obcStatistic, "getEntityStatistic").invoke(null, which, entity);
			return achievementTooltip((String) Reflection.getField(this.nmsStatistic, "name").get(statistic));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return this;
		}
	}

	public FancyMessage itemTooltip(final String itemJSON) {
		onHover("show_item", itemJSON);
		return this;
	}

	public FancyMessage itemTooltip(final ItemStack itemStack) {
		try
		{
			Object nmsItem = Reflection.getMethod(this.obcItemStack, "asNMSCopy", ItemStack.class).invoke(null, itemStack);
			return itemTooltip(Reflection.getMethod(this.nmsItemStack, "save").invoke(nmsItem, this.nmsTagCompound.newInstance()).toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return this;
		}
	}

	public FancyMessage tooltip(final String text) {
		return tooltip(text.split("\\n"));
	}

	public FancyMessage tooltip(final List<String> lines) {
		return tooltip((String[]) lines.toArray());
	}

	public FancyMessage tooltip(final String... lines) {
		if (lines.length == 1)
			onHover("show_text", lines[0]);
		else
			itemTooltip(makeMultilineTooltip(lines));
		return this;
	}

	public FancyMessage then(final Object obj) {
		this.messageParts.add(new MessagePart(obj.toString()));
		this.dirty = true;
		return this;
	}

	public String toJSONString() {
		if (!this.dirty && this.jsonString != null)
			return this.jsonString;
		StringWriter string = new StringWriter();
		JsonWriter json = new JsonWriter(string);
		try
		{
			if (this.messageParts.size() == 1)
				latest().writeJson(json);
			else
			{
				json.beginObject().name("text").value("").name("extra").beginArray();
				for (final MessagePart part : this.messageParts)
					part.writeJson(json);
				json.endArray().endObject();
				json.close();
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("invalid message");
		}
		this.jsonString = string.toString();
		this.dirty = false;
		return this.jsonString;
	}

	public void send(Player player) {
		try
		{
			Object handle = Reflection.getHandle(player);
			Object connection = Reflection.getField(handle.getClass(), "playerConnection").get(handle);
			Object serialized = Reflection.getMethod(this.nmsChatSerializer, "a", String.class).invoke(null, toJSONString());
			Object packet = this.nmsPacketPlayOutChat.getConstructor(Reflection.getNMSClass("IChatBaseComponent")).newInstance(serialized);
			Reflection.getMethod(connection.getClass(), "sendPacket").invoke(connection, packet);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void send(final Iterable<Player> players) {
		for (final Player player : players)
			send(player);
	}

	private MessagePart latest() {
		return this.messageParts.get(this.messageParts.size() - 1);
	}

	private String makeMultilineTooltip(final String[] lines) {
		StringWriter string = new StringWriter();
		JsonWriter json = new JsonWriter(string);
		try
		{
			json.beginObject().name("id").value(1);
			json.name("tag").beginObject().name("display").beginObject();
			json.name("Name").value("\\u00A7f" + lines[0].replace("\"", "\\\""));
			json.name("Lore").beginArray();
			for (int i = 1; i < lines.length; i++)
			{
				final String line = lines[i];
				json.value(line.isEmpty() ? " " : line.replace("\"", "\\\""));
			}
			json.endArray().endObject().endObject().endObject();
			json.close();
		}
		catch (Exception e)
		{
			throw new RuntimeException("invalid tooltip");
		}
		return string.toString();
	}

	private void onClick(final String name, final String data) {
		final MessagePart latest = latest();
		latest.clickActionName = name;
		latest.clickActionData = data;
		this.dirty = true;
	}

	private void onHover(final String name, final String data) {
		final MessagePart latest = latest();
		latest.hoverActionName = name;
		latest.hoverActionData = data;
		this.dirty = true;
	}

	public static ItemStack getFancyMessageItem(String title, String... strings) {
		ItemStack item = new ItemStack(Material.STONE);
		ItemMeta im = item.getItemMeta();
		List<String> lore = Arrays.asList(strings);
		im.setDisplayName(title);
		im.setLore(lore);
		item.setItemMeta(im);
		return item;
	}
}
