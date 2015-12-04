package me.bimmr.fancyportals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

import me.bimmr.fancyportals.Portals.Portal;
import me.bimmr.fancyportals.Portals.PortalHandler;
import me.bimmr.fancyportals.Util.Coords;
import me.bimmr.fancyportals.Util.FileManager;
import me.bimmr.fancyportals.Util.Metrics;
import me.bimmr.fancyportals.Util.Timer;
import me.bimmr.fancyportals.Util.Updater;
import me.bimmr.fancyportals.Util.FancyMessage.FancyMessage;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class FancyPortals extends JavaPlugin {
	private PortalHandler portalHandler;
	private FileManager fileManager;
	private Listeners listeners;
	private Plugin me;

	public void onDisable() {
		if (this.getConfig().getBoolean("Timer Instead of Move Event.Enabled")) {
			Bukkit.getScheduler().cancelTasks((Plugin) this);
		}
	}

	public void onEnable() {
		this.me = (Plugin) this;
		this.fileManager = new FileManager((Plugin) this);
		this.portalHandler = new PortalHandler((Plugin) this, this.fileManager);
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
		if (this.getConfig().getBoolean("Metrics")) {
			this.startMetrics();
		}
		if (this.getConfig().getBoolean("Check For Updates")) {
			this.checkForUpdates(78080);
		}
		if (this.getConfig().getBoolean("Timer Instead of Move Event.Enabled")) {
			System.out.println("Timer On");
			this.startTimer();
		} else {
			this.listeners = new Listeners((Plugin) this, this.portalHandler);
			this.getServer().getPluginManager().registerEvents((Listener) this.listeners, (Plugin) this);
		}
		Bukkit.getMessenger().registerOutgoingPluginChannel((Plugin) this, "BungeeCord");
	}

	void checkForUpdates(int id) {
		Updater updater = new Updater((Plugin) this, id, this.getFile(), Updater.UpdateType.NO_DOWNLOAD, true);
		boolean update = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE;
		String updateName = updater.getLatestName();
		String updateLink = updater.getLatestFileLink();
		if (update) {
			Collection<? extends Player> onlinePlayers;
			for (int length = (onlinePlayers = Bukkit.getOnlinePlayers()).size(), i = 0; i < length; ++i) {
				Player player = (Player) onlinePlayers.toArray()[i];
				if (player.isOp()) {
					new FancyMessage(ChatColor.YELLOW + "Update for FancyPortals Availble: (" + ChatColor.DARK_AQUA + ChatColor.BOLD + updateName + ChatColor.YELLOW + ")").link(updateLink).tooltip("Click here to open the link").send(player);
				}
			}
		}
	}

	void startMetrics() {
		try {
			Metrics metrics = new Metrics((Plugin) this);
			metrics.start();
			System.out.println("Metrics was started!");
		} catch (IOException e) {
			System.out.println("Metrics was unable to start...");
		}
	}

	public FileManager getFileManager() {
		return this.fileManager;
	}

	public PortalHandler getPortalHandler() {
		return this.portalHandler;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("FancyPortals")) {
			Player p = (Player) sender;
			if (args.length >= 3 && (args[0].equalsIgnoreCase("Create") || args[0].equalsIgnoreCase("C"))) {
				if (!p.hasPermission("FancyPortals.Create")) {
					sender.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD).append("Invalid Permissions!").toString());
					return true;
				}
				String portal = args[1];
				if (this.portalHandler.getPortal(portal) != null) {
					sender.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD).append("FancyPortal Already Exists!").toString());
					return true;
				}
				if (p.getTargetBlock((HashSet<Material>) null, 50).getType().isSolid()) {
					sender.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD).append("Invalid FancyPortal Block. Please use a non Solid block!").toString());
					return true;
				}
				StringBuilder arg = new StringBuilder(args[2]);
				for (int argC = 3; argC < args.length; ++argC) {
					arg.append(" ").append(args[argC]);
				}
				String target = arg.toString();
				PortalHandler.PortalType portalType;
				if (target.contains(",")) {
					portalType = PortalHandler.PortalType.LOCATION;
					if (StringUtils.countMatches(target, ",") != 3 && StringUtils.countMatches(target, ",") != 5) {
						sender.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD).append("Unable to create a Target Portal with those coordinates!").toString());
						sender.sendMessage("§7Remember the layout: §e§o/FP Create PortalToSky world,x,y,z[,yaw,pitch]");
						return true;
					}
				} else if (target.contains("/")) {
					if (target.toLowerCase().startsWith("s")) {
						portalType = PortalHandler.PortalType.SERVER_COMMAND;
						target = target.replaceFirst(target.startsWith("S/") ? "S/" : "s/", "");
					} else {
						portalType = PortalHandler.PortalType.PLAYER_COMMAND;
						target = target.replaceFirst("/", "");
					}
				} else {
					portalType = PortalHandler.PortalType.BUNGEE;
				}
				ArrayList<String> blockArray = this.portalHandler.getAdjacentBlocks(p.getTargetBlock((HashSet<Material>) null, 50).getLocation());
				this.portalHandler.createPortal(portal, portalType, blockArray, target);
				sender.sendMessage(ChatColor.RED + "FancyPortal Created: " + ChatColor.GREEN + "Successfully created a " + portalType.toString() + " portal " + args[1]);
			} else if (args.length == 2 && (args[0].equalsIgnoreCase("Remove") || args[0].equalsIgnoreCase("R"))) {
				if (!p.hasPermission("FancyPortals.Remove")) {
					sender.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD).append("Invalid Permissions!").toString());
					return true;
				}
				if (this.portalHandler.getPortal(args[1]) != null) {
					this.portalHandler.removePortal(this.portalHandler.getPortal(args[1]));
					sender.sendMessage(ChatColor.RED + "FancyPortal Removed: " + ChatColor.GREEN + "Successfully removed portal " + args[1]);
				} else {
					sender.sendMessage(ChatColor.RED + "Invalid FancyPortal: " + ChatColor.GREEN + "This portal apperently doesn't exist!");
				}
			} else if (args.length == 2 && (args[0].equalsIgnoreCase("Info") || args[0].equalsIgnoreCase("I"))) {
				if (!p.hasPermission("FancyPortals.Info")) {
					sender.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD).append("Invalid Permissions!").toString());
					return true;
				}
				String portalName = args[1];
				if (this.portalHandler.getPortal(portalName) == null) {
					sender.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD).append("FancyPortal Doesn't Exists!").toString());
					return true;
				}
				Portal portal2 = this.portalHandler.getPortal(portalName);
				sender.sendMessage("");
				sender.sendMessage(new StringBuilder().append(ChatColor.YELLOW).append(ChatColor.BOLD).append("----------[ ").append(portal2.getName()).append(" ]----------").toString());
				sender.sendMessage("");
				sender.sendMessage(new StringBuilder().append(ChatColor.AQUA).append(ChatColor.BOLD).append("Location: ").append(ChatColor.WHITE).append(new Coords(portal2.getCoords().get(0)).asStringIgnoreYawAndPitch()).toString());
				sender.sendMessage(new StringBuilder().append(ChatColor.GRAY).append(ChatColor.BOLD).append("Portal Block: ").append(ChatColor.WHITE).append(new Coords(portal2.getCoords().get(0)).asLocationIgnoreYawAndPitch().getBlock().getType()).toString());
				sender.sendMessage(new StringBuilder().append(ChatColor.LIGHT_PURPLE).append(ChatColor.BOLD).append("Portal Type: ").append(ChatColor.WHITE).append(portal2.getType().toString()).toString());
				if (portal2.getType() == PortalHandler.PortalType.BUNGEE) {
					sender.sendMessage(new StringBuilder().append(ChatColor.YELLOW).append(ChatColor.BOLD).append("Bungee Target: ").append(ChatColor.WHITE).append(portal2.getBungeeTarget()).toString());
				} else if (portal2.getType() == PortalHandler.PortalType.LOCATION) {
					sender.sendMessage(new StringBuilder().append(ChatColor.GREEN).append(ChatColor.BOLD).append("Location Target: ").append(ChatColor.WHITE).append(portal2.getLocationTarget()).toString());
				} else if (portal2.getType() == PortalHandler.PortalType.PLAYER_COMMAND) {
					sender.sendMessage(new StringBuilder().append(ChatColor.BLUE).append(ChatColor.BOLD).append("Player Command: ").append(ChatColor.WHITE).append(portal2.getCommand()).toString());
				} else if (portal2.getType() == PortalHandler.PortalType.SERVER_COMMAND) {
					sender.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD).append("Server Command: ").append(ChatColor.WHITE).append(portal2.getCommand()).toString());
				}
				sender.sendMessage("");
				new FancyMessage("                 §7§l[§4§nRemove Portal§7§l]").tooltip("§aClick to suggest §cremoving§a this portal.").suggest("/FP Remove " + portal2.getName()).send(p);
				sender.sendMessage("");
			} else if (args.length == 1 && (args[0].equalsIgnoreCase("List") || args[0].equalsIgnoreCase("L"))) {
				if (!p.hasPermission("FancyPortals.List")) {
					sender.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD).append("Invalid Permissions!").toString());
					return true;
				}
				int i = 1;
				sender.sendMessage("");
				sender.sendMessage(new StringBuilder().append(ChatColor.YELLOW).append(ChatColor.BOLD).append("---------------[ FancyPortals ]---------------").toString());
				for (Portal portal2 : this.portalHandler.getPortals()) {
					new FancyMessage(String.valueOf(i) + ". ").then(new StringBuilder().append(ChatColor.YELLOW).append(ChatColor.BOLD).append(portal2.getName()).toString()).command("/FancyPortals Info " + portal2.getName()).tooltip(new StringBuilder().append(ChatColor.GRAY).append(ChatColor.ITALIC).append("Click to see Portals Info").toString()).send(p);
					++i;
				}
			} else if (args.length == 2 && (args[0].equalsIgnoreCase("Show") || args[0].equalsIgnoreCase("S"))) {
				if (!p.hasPermission("FancyPortals.Show")) {
					sender.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD).append("Invalid Permissions!").toString());
					return true;
				}
				sender.sendMessage("");
				if (this.portalHandler.getPortal(args[0]) != null) {
					Portal portal3 = this.portalHandler.getPortal(args[0]);
					sender.sendMessage(new StringBuilder().append(ChatColor.YELLOW).append(ChatColor.BOLD).append(portal3.getName()).append(ChatColor.YELLOW).append(" has been filled in with black glass").toString());
				} else {
					sender.sendMessage(new StringBuilder().append(ChatColor.YELLOW).append(ChatColor.BOLD).append(args[0]).append(ChatColor.YELLOW).append(" isn't a valid portal name").toString());
				}
			} else {
				if (!p.hasPermission("FancyPortals.Help")) {
					sender.sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD).append("Invalid Permissions!").toString());
					return true;
				}
				sender.sendMessage("");
				sender.sendMessage(new StringBuilder().append(ChatColor.WHITE).append(ChatColor.BOLD).append("-------------[ FancyPortals Help ]-------------").toString());
				new FancyMessage("§c§l/FancyPortals §eCreate §a<PortalName> §d<Target>").itemTooltip(FancyMessage.getFancyMessageItem("Create", "To Create a FancyPortal:", "§e§l/FP Create <Portal Name> <target>", " ", "§fNow for target you'd want to type:", "§fworld,x,y,z §a<- Teleport to Specific Location", "§fworld,x,y,z,yaw,pitch §a<- Teleport to Specific Location", "§fServerName §a<- Teleport to a Bungee Server", "§f/Command §a<- Make the player use a command", "§fs/Command §a<- Make the server use the command", " ", "§cWhen using commands theres some variables:", "§f<player> §7- §fThe play who triggered it", "§f@p §7- §fThe closest player", "§f@r §7- §fA random player", "§f@a §7- §fAll players", " ", "§7Ex: §o/FP Create PortalToSpawn /Spawn", "§7Ex: §o/FP Create PortalToHub Hub", "§7Ex: §o/FP Create PortalToSky world,10,100,10,-1.5,90", "§7Ex: §o/FP Create PortalToSay s/Say Hello <player>")).suggest("/FP Create <PortalName> [S]<Bungee/world,x,y,z,yaw,pitch/Command>").send(p);
				new FancyMessage("§c§l/FancyPortals §eRemove §a<PortalName>").itemTooltip(FancyMessage.getFancyMessageItem("Remove", "To Remove a Fancy Portal:", "§e§l/FP Remove <Portal Name>")).suggest("/FP Remove <PortalName>").send(p);
				new FancyMessage("§c§l/FancyPortals §eInfo     §a<PortalName>").itemTooltip(FancyMessage.getFancyMessageItem("Info", "Show the info for that FancyPortals", "§e§l/FP Info <Portal Name>")).suggest("/FP Info <Portal Name>").send(p);
				new FancyMessage("§c§l/FancyPortals §eShow     §a<PortalName>").itemTooltip(FancyMessage.getFancyMessageItem("Show", "Outline that FancyPortals", "§e§l/FP Show <Portal Name>")).suggest("/FP Show <Portal Name>").send(p);
				new FancyMessage("§c§l/FancyPortals §eList").itemTooltip(FancyMessage.getFancyMessageItem("List", "To show a list of all FancyPortals", "§e§l/FP List", " ", "§7[§eTip§7]§f Try clicking a portal in the list!")).suggest("/FP List").send(p);
				sender.sendMessage("");
			}
		}
		return true;
	}

	void startTimer() {
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin) this, (Runnable) new Runnable() {
			@Override
			public void run() {
				if (Bukkit.getOnlinePlayers().size() != 0 && !FancyPortals.this.portalHandler.getPortals().isEmpty()) {
					for (Portal p : FancyPortals.this.portalHandler.getPortals()) {
						Location loc = new Coords(p.getCoords().get(0)).asLocation();
						if (loc.getChunk().isLoaded()) {
							loc.getChunk().load();
						}
						if (loc.getChunk().getEntities().length != 0) {
							Entity[] entities;
							for (int length = (entities = loc.getChunk().getEntities()).length, i = 0; i < length; ++i) {
								Entity e = entities[i];
								if (p.getCoords().contains(new Coords(e.getLocation()).asStringIgnoreYawAndPitch())) {
									if (!(e instanceof Player) || ((Player) e).hasPermission("FancyPortals.Use")) {
										if (Timer.isCooledDown(e.getUniqueId())) {
											Timer.addToCooldown(e.getUniqueId());
											Portal portal = p;
											if (e instanceof Player && portal.getType() == PortalHandler.PortalType.BUNGEE) {
												ByteArrayDataOutput out = ByteStreams.newDataOutput();
												out.writeUTF("Connect");
												out.writeUTF(portal.getBungeeTarget());
												((Player) e).sendPluginMessage(FancyPortals.this.me, "BungeeCord", out.toByteArray());
											} else if (portal.getType() == PortalHandler.PortalType.LOCATION) {
												Location target = e.getWorld().getSpawnLocation();
												target = new Coords(portal.getLocationTarget()).asLocation();
												e.teleport(target);
											} else if (portal.getType() == PortalHandler.PortalType.SERVER_COMMAND) {
												String command = portal.getCommand();
												if (e instanceof Player) {
													command = command.replaceAll("<player>", ((Player) e).getName());
												}
												if (command.contains("@p")) {
													double closest = 2.147483647E9;
													Player closestP = (e instanceof Player) ? (Player) e : null;
													Collection<? extends Player> onlinePlayers;
													for (int length2 = (onlinePlayers = Bukkit.getOnlinePlayers()).size(), j = 0; j < length2; ++j) {
														Player ppl = (Player) onlinePlayers.toArray()[j];
														double distance = ppl.getLocation().distance(new Coords(p.getCoords().get(0)).asLocation());
														if (distance < closest) {
															closest = distance;
															closestP = ppl;
														}
													}
													command = command.replaceAll("@p", closestP.getName());
												}
												if (command.contains("@r")) {
													Random r = new Random();
													Player ran = (Player) Bukkit.getOnlinePlayers().toArray()[r.nextInt(Bukkit.getOnlinePlayers().size())];
													command = command.replaceAll("@r", ran.getName());
												}
												if (command.contains("@a")) {
													Collection<? extends Player> onlinePlayers2;
													for (int length3 = (onlinePlayers2 = Bukkit.getOnlinePlayers()).size(), k = 0; k < length3; ++k) {
														Player ppl2 = (Player) onlinePlayers2.toArray()[k];
														Bukkit.getServer().dispatchCommand((CommandSender) Bukkit.getConsoleSender(), command.replaceAll("@a", ppl2.getName()));
													}
												} else {
													Bukkit.getServer().dispatchCommand((CommandSender) Bukkit.getConsoleSender(), command);
												}
											} else if (e instanceof Player && portal.getType() == PortalHandler.PortalType.PLAYER_COMMAND) {
												String command = portal.getCommand();
												if (e instanceof Player) {
													command = command.replaceAll("<player>", ((Player) e).getName());
												}
												if (command.contains("@p")) {
													double closest = 2.147483647E9;
													Player closestP = (Player) e;
													Collection<? extends Player> onlinePlayers3;
													for (int length4 = (onlinePlayers3 = Bukkit.getOnlinePlayers()).size(), l = 0; l < length4; ++l) {
														Player ppl = (Player) onlinePlayers3.toArray()[l];
														double distance = ppl.getLocation().distance(new Coords(p.getCoords().get(0)).asLocation());
														if (distance < closest) {
															closest = distance;
															closestP = ppl;
														}
													}
													command = command.replaceAll("@p", closestP.getName());
												}
												if (command.contains("@r")) {
													Random r = new Random();
													Player ran = (Player) Bukkit.getOnlinePlayers().toArray()[r.nextInt(Bukkit.getOnlinePlayers().size())];
													command = command.replaceAll("@r", ran.getName());
												}
												if (command.contains("@a")) {
													Collection<? extends Player> onlinePlayers4;
													for (int length5 = (onlinePlayers4 = Bukkit.getOnlinePlayers()).size(), n = 0; n < length5; ++n) {
														Player ppl2 = (Player) onlinePlayers4.toArray()[n];
														((Player) e).performCommand(command.replaceAll("@a", ppl2.getName()));
													}
												} else {
													((Player) e).performCommand(command);
												}
											}
										}
									} else if (e instanceof Player) {
										((Player) e).sendMessage(new StringBuilder().append(ChatColor.RED).append(ChatColor.BOLD).append("Invalid Permissions!").toString());
									}
								}
							}
						}
					}
				}
			}
		}, 0L, this.me.getConfig().getLong("Timer Instead of Move Event.Time in Seconds") * 20L);
	}
}
