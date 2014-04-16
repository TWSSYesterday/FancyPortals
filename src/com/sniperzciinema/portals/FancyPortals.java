
package com.sniperzciinema.portals;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.sniperzciinema.portals.Portals.Portal;
import com.sniperzciinema.portals.Portals.PortalHandler;
import com.sniperzciinema.portals.Portals.PortalHandler.PortalType;
import com.sniperzciinema.portals.Util.Coords;
import com.sniperzciinema.portals.Util.FileManager;
import com.sniperzciinema.portals.Util.FancyMessage.FancyMessage;


public class FancyPortals extends JavaPlugin {

	private PortalHandler	portalHandler;
	private FileManager		fileManager;
	private Logger			logger;

	public Logger getConsoleLogger() {
		return this.logger;
	}

	public FileManager getFileManager() {
		return this.fileManager;
	}

	public PortalHandler getPortalHandler() {
		return this.portalHandler;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (cmd.getName().equalsIgnoreCase("FancyPortals"))
		{
			Player p = (Player) sender;
			if (!p.hasPermission("FancyPortals.Create"))
			{
				sender.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "Invalid Permissions!");
				return true;
			}
			if ((args.length >= 3) && (args[0].equalsIgnoreCase("Create") || args[0].equalsIgnoreCase("C")))
			{
				String portal = args[1];
				if (this.portalHandler.getPortal(portal) != null)
				{
					sender.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "FancyPortal Already Exists!");
					return true;
				}
				if (p.getTargetBlock(null, 50).getType().isSolid())
				{
					sender.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "Invalid FancyPortal Block. Please use a non Solid block!");
					return true;
				}
				PortalType portalType;

				StringBuilder arg = new StringBuilder(args[2]);
				for (int argC = 3; argC < args.length; argC++)
					arg.append(" ").append(args[argC]);

				String target = arg.toString();
				if (target.contains(","))
				{
					portalType = PortalType.LOCATION;
					if (StringUtils.countMatches(target, ",") != 5)
					{
						sender.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "Unable to create a Target Portal with those coordinates!");
						sender.sendMessage("§7Remember the layout: §e§o/FP Create PortalToSky world,x,y,z,yaw,pitch");
						return true;
					}
				}
				else
					if (target.contains("/"))
					{
						if (target.toLowerCase().startsWith("s"))
						{
							portalType = PortalType.SERVER_COMMAND;
							target = target.replaceFirst(target.startsWith("S/") ? "S/" : "s/", "");

						}
						else
						{
							portalType = PortalType.PLAYER_COMMAND;
							target = target.replaceFirst("/", "");
						}
					}
					else
						portalType = PortalType.BUNGEE;
				ArrayList<String> blockArray = this.portalHandler.getAdjacentBlocks(p.getTargetBlock(null, 50).getLocation());

				this.portalHandler.createPortal(portal, portalType, blockArray, target);

				sender.sendMessage(ChatColor.RED + "FancyPortal Created: " + ChatColor.GREEN + "Successfully created a " + portalType.toString() + " portal " + args[1]);

			}
			else
				if ((args.length == 2) && (args[0].equalsIgnoreCase("Remove") || args[0].equalsIgnoreCase("R")))
				{
					if (this.portalHandler.getPortal(args[1]) != null)
					{
						this.portalHandler.removePortal(this.portalHandler.getPortal(args[1]));
						sender.sendMessage(ChatColor.RED + "FancyPortal Removed: " + ChatColor.GREEN + "Successfully removed portal " + args[1]);

					}
					else
						sender.sendMessage(ChatColor.RED + "Invalid FancyPortal: " + ChatColor.GREEN + "This portal apperently doesn't exist!");

				}
				else
					if ((args.length == 1) && (args[0].equalsIgnoreCase("List") || args[0].equalsIgnoreCase("L")))
					{
						int i = 1;
						sender.sendMessage("");
						sender.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "---------------[ FancyPortals ]---------------");
						for (Portal portal : this.portalHandler.getPortals())
						{
							new FancyMessage(i + ". ").then("" + ChatColor.YELLOW + ChatColor.BOLD + portal.getName()).command("/FancyPortals Info " + portal.getName()).tooltip(ChatColor.GRAY + "" + ChatColor.ITALIC + "Click to see Portals Info").send(p);
							i++;
						}

					}
					else
						if ((args.length == 2) && (args[0].equalsIgnoreCase("Info") || args[0].equalsIgnoreCase("I")))
						{
							String portalName = args[1];
							if (this.portalHandler.getPortal(portalName) == null)
							{
								sender.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "FancyPortal Doesn't Exists!");
								return true;
							}
							else
							{
								Portal portal = this.portalHandler.getPortal(portalName);
								sender.sendMessage("");
								sender.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "----------[ " + portal.getName() + " ]----------");
								sender.sendMessage("");
								sender.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "Location: " + ChatColor.WHITE + new Coords(
										portal.getCoords().get(0)).asStringIgnoreYawAndPitch());

								sender.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "Portal Block: " + ChatColor.WHITE + new Coords(
										portal.getCoords().get(0)).asLocationIgnoreYawAndPitch().getBlock().getType());

								sender.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Portal Type: " + ChatColor.WHITE + portal.getType().toString());
								if (portal.getType() == PortalType.BUNGEE)
									sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Bungee Target: " + ChatColor.WHITE + portal.getBungeeTarget());
								else
									if (portal.getType() == PortalType.LOCATION)
										sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Location Target: " + ChatColor.WHITE + portal.getLocationTarget());
									else
										if (portal.getType() == PortalType.PLAYER_COMMAND)
											sender.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "Player Command: " + ChatColor.WHITE + portal.getCommand());
										else
											if (portal.getType() == PortalType.SERVER_COMMAND)
												sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Server Command: " + ChatColor.WHITE + portal.getCommand());
								sender.sendMessage("");
								new FancyMessage("                 §7§l[§4§nRemove Portal§7§l]").tooltip("§aClick to suggest §cremoving§a this arena.").suggest("/FP Remove " + portal.getName()).send(p);
								sender.sendMessage("");
							}
						}
						else

						{
							sender.sendMessage("");
							sender.sendMessage("" + ChatColor.WHITE + ChatColor.BOLD + "-------------[ FancyPortals Help ]-------------");
							new FancyMessage(
									"§c§l/FancyPortals §eCreate §a<PortalName> §d<Target>          ").itemTooltip(FancyMessage.getFancyMessageItem("Create", "To Create a FancyPortal:", "§e§l/FP Create <Portal Name> <target>", " ", "§fNow for target you'd want to type:", "§fworld,x,y,z §a<- Teleport to Specific Location", "§fServerName §a<- Teleport to a Bungee Server", "§f/Command §a<- Make the player use a command", "§7S§f/Command §a<- Make the server use the command", " ", "" + "§7Ex: §o/FP Create PortalToSpawn /Spawn", "" + "§7Ex: §o/FP Create PortalToHub Hub", "" + "§7Ex: §o/FP Create PortalToSky world,10,100,10,-1.5,90", "" + "§7Ex: §o/FP Create PortalToSay s/Say Hello <player>")).suggest("/FP Create <PortalName> [S]<Bungee/world,x,y,z,yaw,pitch/Command>").send(p);
							new FancyMessage(
									"§c§l/FancyPortals §eRemove §a<PortalName>                     ").itemTooltip(FancyMessage.getFancyMessageItem("Remove", "To Remove a Fancy Portal:", "§e§l/FP Remove <Portal Name>")).suggest("/FP Remove <PortalName>").send(p);
							new FancyMessage(
									"§c§l/FancyPortals §eList                                      ").itemTooltip(FancyMessage.getFancyMessageItem("List", "To show a list of all FancyPortals", "§e§l/FP List", " ", "§7[§eTip§7]§f Try clicking a portal in the list!")).suggest("/FP List").send(p);
							new FancyMessage(
									"§c§l/FancyPortals §eInfo §a<PortalName>                       ").itemTooltip(FancyMessage.getFancyMessageItem("Info", "Show the info for that FancyPortals", "§e§l/FP Info <Portal Name>")).suggest("/FP Info <Portal Name>").send(p);
							sender.sendMessage("");
						}
		}
		return true;
	}

	@Override
	public void onDisable() {
	}

	@Override
	public void onEnable() {
		this.logger = getLogger();
		this.fileManager = new FileManager(this);
		this.portalHandler = new PortalHandler(this, this.fileManager, this.logger);
		// Register the event listener
		getServer().getPluginManager().registerEvents(new Listeners(this, this.portalHandler), this);

		Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
	}
}
