package me.bimmr.fancyportals.Util.FancyMessage;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Achievement;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.gson.stream.JsonWriter;

public class FancyMessage {
	private final List<MessagePart> messageParts;
	private String jsonString;
	private boolean dirty;
	private Class<?> nmsChatSerializer = Reflection.getNMSClass("IChatBaseComponent$ChatSerializer");
	private Class<?> nmsTagCompound = Reflection.getNMSClass("NBTTagCompound");
	private Class<?> nmsPacketPlayOutChat = Reflection.getNMSClass("PacketPlayOutChat");
	private Class<?> nmsAchievement = Reflection.getNMSClass("Achievement");
	private Class<?> nmsStatistic = Reflection.getNMSClass("Statistic");

	private Class<?> nmsItemStack = Reflection.getNMSClass("ItemStack");
	private Class<?> obcStatistic = Reflection.getOBCClass("CraftStatistic");

	private Class<?> obcItemStack = Reflection.getOBCClass("inventory.CraftItemStack");

	public static ItemStack getFancyMessageItem(String title, String... strings) {
		ItemStack item = new ItemStack(Material.STONE);
		ItemMeta im = item.getItemMeta();
		List<String> lore = Arrays.asList(strings);
		im.setDisplayName(title);
		im.setLore(lore);
		item.setItemMeta(im);
		return item;
	}

	public FancyMessage(String firstPartText) {
		this.messageParts = new ArrayList<MessagePart>();
		this.messageParts.add(new MessagePart(firstPartText));
		this.jsonString = null;
		this.dirty = false;
	}

	public FancyMessage achievementTooltip(Achievement which) {
		try {
			Object achievement = Reflection.getMethod(this.obcStatistic, "getNMSAchievement", new Class[0]).invoke(null, new Object[] { which });
			return achievementTooltip((String) Reflection.getField(this.nmsAchievement, "name").get(achievement));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	public FancyMessage achievementTooltip(String name) {
		onHover("show_achievement", "achievement." + name);
		return this;
	}

	public FancyMessage color(ChatColor color) {
		if (!color.isColor())
			throw new IllegalArgumentException(color.name() + " is not a color");
		latest().color = color;
		this.dirty = true;
		return this;
	}

	public FancyMessage command(String command) {
		onClick("run_command", command);
		return this;
	}

	public FancyMessage file(String path) {
		onClick("open_file", path);
		return this;
	}

	public FancyMessage itemTooltip(ItemStack itemStack) {
		try {
			Object nmsItem = Reflection.getMethod(this.obcItemStack, "asNMSCopy", new Class[] { ItemStack.class }).invoke(null, new Object[] { itemStack });
			return itemTooltip(Reflection.getMethod(this.nmsItemStack, "save", new Class[0]).invoke(nmsItem, new Object[] { this.nmsTagCompound.newInstance() }).toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	public FancyMessage itemTooltip(String itemJSON) {
		onHover("show_item", itemJSON);
		return this;
	}

	private MessagePart latest() {
		return (MessagePart) this.messageParts.get(this.messageParts.size() - 1);
	}

	public FancyMessage link(String url) {
		onClick("open_url", url);
		return this;
	}

	private String makeMultilineTooltip(String[] lines) {
		StringWriter string = new StringWriter();
		JsonWriter json = new JsonWriter(string);
		try {
			json.beginObject().name("id").value(1L);
			json.name("tag").beginObject().name("display").beginObject();
			json.name("Name").value("\\u00A7f" + lines[0].replace("\"", "\\\""));
			json.name("Lore").beginArray();
			for (int i = 1; i < lines.length; i++) {
				String line = lines[i];
				json.value(line.isEmpty() ? " " : line.replace("\"", "\\\""));
			}
			json.endArray().endObject().endObject().endObject();
			json.close();
		} catch (Exception e) {
			throw new RuntimeException("invalid tooltip");
		}
		return string.toString();
	}

	private void onClick(String name, String data) {
		MessagePart latest = latest();
		latest.clickActionName = name;
		latest.clickActionData = data;
		this.dirty = true;
	}

	private void onHover(String name, String data) {
		MessagePart latest = latest();
		latest.hoverActionName = name;
		latest.hoverActionData = data;
		this.dirty = true;
	}

	public void send(Iterable<Player> players) {
		for (Player player : players)
			send(player);
	}

	public void send(Player player) {
		try {
			Object handle = Reflection.getHandle(player);
			Object connection = Reflection.getField(handle.getClass(), "playerConnection").get(handle);
			Object serialized = Reflection.getMethod(this.nmsChatSerializer, "a", new Class[] { String.class }).invoke(null, new Object[] { toJSONString() });
			Object packet = this.nmsPacketPlayOutChat.getConstructor(new Class[] { Reflection.getNMSClass("IChatBaseComponent") }).newInstance(new Object[] { serialized });
			Reflection.getMethod(connection.getClass(), "sendPacket", new Class[0]).invoke(connection, new Object[] { packet });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public FancyMessage statisticTooltip(Statistic which) {
		Statistic.Type type = which.getType();
		if (type != Statistic.Type.UNTYPED)
			throw new IllegalArgumentException("That statistic requires an additional " + type + " parameter!");
		try {
			Object statistic = Reflection.getMethod(this.obcStatistic, "getNMSStatistic", new Class[0]).invoke(null, new Object[] { which });
			return achievementTooltip((String) Reflection.getField(this.nmsStatistic, "name").get(statistic));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	public FancyMessage statisticTooltip(Statistic which, EntityType entity) {
		Statistic.Type type = which.getType();
		if (type == Statistic.Type.UNTYPED)
			throw new IllegalArgumentException("That statistic needs no additional parameter!");
		if (type != Statistic.Type.ENTITY)
			throw new IllegalArgumentException("Wrong parameter type for that statistic - needs " + type + "!");
		try {
			Object statistic = Reflection.getMethod(this.obcStatistic, "getEntityStatistic", new Class[0]).invoke(null, new Object[] { which, entity });
			return achievementTooltip((String) Reflection.getField(this.nmsStatistic, "name").get(statistic));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	public FancyMessage statisticTooltip(Statistic which, Material item) {
		Statistic.Type type = which.getType();
		if (type == Statistic.Type.UNTYPED)
			throw new IllegalArgumentException("That statistic needs no additional parameter!");
		if (((type == Statistic.Type.BLOCK) && (item.isBlock())) || (type == Statistic.Type.ENTITY))
			throw new IllegalArgumentException("Wrong parameter type for that statistic - needs " + type + "!");
		try {
			Object statistic = Reflection.getMethod(this.obcStatistic, "getMaterialStatistic", new Class[0]).invoke(null, new Object[] { which, item });
			return achievementTooltip((String) Reflection.getField(this.nmsStatistic, "name").get(statistic));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	public FancyMessage style(ChatColor[] styles) {
		for (ChatColor style : styles)
			if (!style.isFormat())
				throw new IllegalArgumentException(style.name() + " is not a style");
		latest().styles = styles;
		this.dirty = true;
		return this;
	}

	public FancyMessage suggest(String command) {
		onClick("suggest_command", command);
		return this;
	}

	public FancyMessage then(Object obj) {
		this.messageParts.add(new MessagePart(obj.toString()));
		this.dirty = true;
		return this;
	}

	public String toJSONString() {
		if ((!this.dirty) && (this.jsonString != null))
			return this.jsonString;
		StringWriter string = new StringWriter();
		JsonWriter json = new JsonWriter(string);
		try {
			if (this.messageParts.size() == 1) {
				latest().writeJson(json);
			} else {
				json.beginObject().name("text").value("").name("extra").beginArray();
				for (MessagePart part : this.messageParts)
					part.writeJson(json);
				json.endArray().endObject();
				json.close();
			}
		} catch (Exception e) {
			throw new RuntimeException("invalid message");
		}
		this.jsonString = string.toString();
		this.dirty = false;
		return this.jsonString;
	}

	public FancyMessage tooltip(List<String> lines) {
		return tooltip((String[]) lines.toArray());
	}

	public FancyMessage tooltip(String text) {
		return tooltip(text.split("\\n"));
	}

	public FancyMessage tooltip(String[] lines) {
		if (lines.length == 1)
			onHover("show_text", lines[0]);
		else
			itemTooltip(makeMultilineTooltip(lines));
		return this;
	}
}