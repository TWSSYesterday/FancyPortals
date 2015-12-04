package me.bimmr.fancyportals.Util.FancyMessage;

import org.bukkit.ChatColor;

import com.google.gson.stream.JsonWriter;

final class MessagePart {
	ChatColor color = null;
	ChatColor[] styles = null;
	String clickActionName = null;
	String clickActionData = null;
	String hoverActionName = null;
	String hoverActionData = null;
	final String text;

	MessagePart(String text) {
		this.text = text;
	}

	JsonWriter writeJson(JsonWriter json) {
		try {
			json.beginObject().name("text").value(this.text);
			if (this.color != null)
				json.name("color").value(this.color.name().toLowerCase());
			if (this.styles != null)
				for (ChatColor style : this.styles)
					json.name(style.name().toLowerCase()).value(true);
			if ((this.clickActionName != null) && (this.clickActionData != null))
				json.name("clickEvent").beginObject().name("action").value(this.clickActionName).name("value").value(this.clickActionData).endObject();
			if ((this.hoverActionName != null) && (this.hoverActionData != null))
				json.name("hoverEvent").beginObject().name("action").value(this.hoverActionName).name("value").value(this.hoverActionData).endObject();
			return json.endObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return json;
	}
}