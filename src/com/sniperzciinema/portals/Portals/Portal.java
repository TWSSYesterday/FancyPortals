
package com.sniperzciinema.portals.Portals;

import java.util.ArrayList;

import com.sniperzciinema.portals.Portals.PortalHandler.PortalType;
import com.sniperzciinema.portals.Util.Coords;


public class Portal {

	private String				name;
	private PortalType			type;
	private ArrayList<String>	coords;
	private String				locationTarget;
	private String				command;
	private String				bungeeTarget;

	public Portal()
	{

	}

	public Portal(String name, PortalType type, ArrayList<String> coords, String arg)
	{
		setName(name);
		setType(type);
		setCoords(coords);

		if (type == PortalType.BUNGEE)
			setBungeeTarget(arg);

		else
			if (type == PortalType.LOCATION)
				setLocationTarget(new Coords(arg).asString());
			else
				setCommand(arg);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the type
	 */
	public PortalType getType() {
		return this.type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(PortalType type) {
		this.type = type;
	}

	/**
	 * @return the command
	 */
	public String getCommand() {
		return this.command;
	}

	/**
	 * @param command
	 *            the command to set
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * @return the bungeeTarget
	 */
	public String getBungeeTarget() {
		return this.bungeeTarget;
	}

	/**
	 * @param bungeeTarget
	 *            the bungeeTarget to set
	 */
	public void setBungeeTarget(String bungeeTarget) {
		this.bungeeTarget = bungeeTarget;
	}

	/**
	 * @return the coords
	 */
	public ArrayList<String> getCoords() {
		return this.coords;
	}

	/**
	 * @param coords
	 *            the coords to set
	 */
	public void setCoords(ArrayList<String> coords) {
		this.coords = coords;
	}

	/**
	 * @return the locationTarget
	 */
	public String getLocationTarget() {
		return this.locationTarget;
	}

	/**
	 * @param locationTarget
	 *            the locationTarget to set
	 */
	public void setLocationTarget(String locationTarget) {
		this.locationTarget = locationTarget;
	}
}
