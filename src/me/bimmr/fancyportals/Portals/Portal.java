package me.bimmr.fancyportals.Portals;

import java.util.ArrayList;
import me.bimmr.fancyportals.Util.Coords;

public class Portal {
	private String name;
	private PortalHandler.PortalType type;
	private ArrayList<String> coords;
	private String locationTarget;
	private String command;
	private String bungeeTarget;

	public Portal() {
		super();
	}

	public Portal(String name, PortalHandler.PortalType type, ArrayList<String> coords, String arg) {
		setName(name);
		setType(type);
		setCoords(coords);

		if (type == PortalHandler.PortalType.BUNGEE)
			setBungeeTarget(arg);
		else if (type == PortalHandler.PortalType.LOCATION)
			setLocationTarget(new Coords(arg).asString());
		else
			setCommand(arg);
	}

	public String getBungeeTarget() {
		return this.bungeeTarget;
	}

	public String getCommand() {
		return this.command;
	}

	public ArrayList<String> getCoords() {
		return this.coords;
	}

	public String getLocationTarget() {
		return this.locationTarget;
	}

	public String getName() {
		return this.name;
	}

	public PortalHandler.PortalType getType() {
		return this.type;
	}

	public void setBungeeTarget(String bungeeTarget) {
		this.bungeeTarget = bungeeTarget;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public void setCoords(ArrayList<String> coords) {
		this.coords = coords;
	}

	public void setLocationTarget(String locationTarget) {
		this.locationTarget = locationTarget;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(PortalHandler.PortalType type) {
		this.type = type;
	}
}