package net.mcbbs.id84491135;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.block.*;

/**
 *
 * @author 84491135
 * 
 */
public class RandomRespawn extends JavaPlugin implements Listener {

	Random r = new Random();
	static final String nop = "§c你无此命令权限！";
	static final String ename = "RandomRespawn";
	static final String cname = "随机复活插件";
	static final String prefix = "§b[随机复活]";
	static final String cmdpre = "[随机复活]";
	FileConfiguration f;
	Map<String, Location> dieLocations;

	public static void main(String[] args) {
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase(ename)) {
			if (args.length == 0) {
				PluginDescriptionFile description = getDescription();
				sender.sendMessage("§6---------[§b" + cname + "§c(" + ename + ")§6]-----------");
				sender.sendMessage("§3" + description.getDescription());
				sender.sendMessage("§c详细功能输入§b\"/rr ?\"§c查看");
				sender.sendMessage("§b[版本]: §a" + description.getVersion());
				sender.sendMessage("§b[作者]: §a" + description.getAuthors());
				sender.sendMessage("§b[补充]: §a如有bug请联系作者=w=");
				sender.sendMessage("§6-------------------------------------------------");
				return true;
			} else if (args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help")) {
				if (!sender.hasPermission("RandomRespawn.default")) {
					sender.sendMessage(prefix + nop);
					return true;
				}
				sender.sendMessage("§6---------------[§b帮助列表§6]--------------");
				sender.sendMessage("§c/rr set  §2设置当前世界随机复活范围");
				sender.sendMessage("§c/rr size  §2查看当前世界随机复活范围");
				sender.sendMessage("§c/rr setworld  §2设置当前世界是否使用随机复活规则");
				sender.sendMessage("§c/rr reload  §2重新加载配置文件");
				sender.sendMessage("§6------§7如有意见或建议请联系作者§6-------");
				return true;
			} else if (args[0].equalsIgnoreCase("set")) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(cmdpre + "控制台暂时无法使用此命令");
					return true;
				}
				if (!sender.hasPermission("RandomRespawn.set")) {
					sender.sendMessage(prefix + nop);
					return true;
				}
				if (args.length == 2) {
					if (args[1].matches("^[+-]?[0-9]+$")) {
						Player player = (Player) sender;
						String wn = player.getWorld().getName();
						setSize(Integer.parseInt(args[1]), sender, player.getWorld());
						if (Integer.parseInt(args[1]) > 0) {
							sender.sendMessage(prefix + "§2世界[§c" + wn + "§2]" + "§2随机复活范围已设置为:§c 长" + args[1] + " * 宽"
									+ args[1] + " §2的正方形");
						} else if (Integer.parseInt(args[1]) == 0) {
							sender.sendMessage(prefix + "§2世界[§c" + wn + "§2]" + "§2已设置为: §c原地复活");
						} else {
							sender.sendMessage(prefix + "§2世界[§c" + wn + "§2]" + "§2已设置为: §c默认复活点");
						}
						return true;
					}
				} else if (args.length == 3) {
					if (args[1].matches("^[+-]?[0-9]+$") && args[2].matches("^[+-]?[0-9]+$")) {
						Player player = (Player) sender;
						String wn = player.getWorld().getName();
						setSize(Integer.parseInt(args[1]), Integer.parseInt(args[2]), sender, player.getWorld());
						if (Integer.parseInt(args[1]) > 0 && Integer.parseInt(args[2]) > 0) {
							sender.sendMessage(
									prefix + "§2世界[§c" + wn + "§2]" + "§2随机复活范围已设置为:§c 长" + args[1] + " * 宽" + args[2]);
						} else if (Integer.parseInt(args[1]) == 0 && Integer.parseInt(args[2]) == 0) {
							sender.sendMessage(prefix + "§2世界[§c" + wn + "§2]" + "§2已设置为: §c原地复活");
						} else {
							sender.sendMessage(prefix + "§2世界[§c" + wn + "§2]" + "§2已设置为: §c默认复活点");
						}
						return true;
					}
				}

				sender.sendMessage(prefix + "§c------------------------------------------");
				sender.sendMessage(prefix + "§6用法1： /rr set 长 宽");
				sender.sendMessage(prefix + "§6用法2： /rr set 正方形边长");
				sender.sendMessage(prefix + "§c-----------------详细说明-----------------");
				sender.sendMessage(prefix + "§21.以上指令均是以死亡后的位置为中心建立的范围。");
				sender.sendMessage(prefix + "§22.参数均为正数则启动该插件。");
				sender.sendMessage(prefix + "§23.参数均为0则原地复活。");
				sender.sendMessage(prefix + "§24.参数为其它情况（例如有负数）在默认复活点出生。");
				sender.sendMessage(prefix + "§25.参数需要为整数。");
				sender.sendMessage(prefix + "§c------------------------------------------");

				return true;
			} else if (args[0].equalsIgnoreCase("size")) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(cmdpre + "控制台暂时无法使用此命令");
					return true;
				}
				if (!sender.hasPermission("RandomRespawn.default")) {
					sender.sendMessage(prefix + nop);
					return true;
				}
				Player player = (Player) sender;
				World world = player.getWorld();
				String wn = world.getName();
				if (f.contains("World." + wn)) {
					int x = f.getInt("World." + wn + ".X");
					int z = f.getInt("World." + wn + ".Z");

					if (x > 0 && z > 0) {
						sender.sendMessage(prefix + "§5世界[§c" + wn + "§5]" + "随机复活范围为: §c长" + x + " * 宽" + z);
					} else if (x == 0 || z == 0) {
						sender.sendMessage(prefix + "§5世界[§c" + wn + "§5]" + "复活为: §c原地复活");
					} else {
						sender.sendMessage(prefix + "§5世界[§c" + wn + "§5]" + "复活为: §c默认复活点");
					}
					if (!f.getBoolean("World." + wn + ".use")) {
						sender.sendMessage(prefix + "§2提示：世界[§c" + wn + "§2]还没有开启随机复活");
					}
					return true;
				} else {
					sender.sendMessage(prefix + "§5世界[§c" + wn + "§5]还没有设置随机复活范围");
					return true;
				}

			} else if (args[0].equalsIgnoreCase("setworld")) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(cmdpre + "控制台暂时无法使用此命令");
					return true;
				}
				if (!sender.hasPermission("RandomRespawn.set")) {
					sender.sendMessage(prefix + nop);
					return true;
				}
				Player player = (Player) sender;
				String n = player.getWorld().getName();
				if (args.length == 2) {
					if (args[1].contains("true") || args[1].contains("false")) {
						boolean f;
						if (args[1].contains("true")) {
							f = true;
						} else if ((args[1].contains("false"))) {
							f = false;
						} else {
							sender.sendMessage(prefix + "§c【错误】设置错误！");
							return true;
						}
						File file = new File(getDataFolder(), "config.yml");
						FileConfiguration config = load(file);
						if (!config.contains("World." + n)) {
							sender.sendMessage(prefix + "§c此世界没有设置随机复活范围，请先设置范围！");
							return true;
						}
						config.set("World." + n + ".use", f);
						try {
							config.save(file);
						} catch (IOException e) {
							e.printStackTrace();
							getLogger().info(cmdpre + "【错误】配置文件写入错误！");
							sender.sendMessage(prefix + "§c【错误】配置文件写入错误！");
						}
						this.f = getC();
						sender.sendMessage(prefix + "§2世界[§c" + n + "§2]随机复活已设置为:§c" + f);
						return true;
					}
					sender.sendMessage(prefix + "§c/rr setworld true   开启当前世界随机复活规则");
					sender.sendMessage(prefix + "§c/rr setworld false   关闭当前世界随机复活规则");
					return true;

				}
				sender.sendMessage(prefix + "§c/rr setworld true   当前世界开启随机复活规则");
				sender.sendMessage(prefix + "§c/rr setworld false   当前世界关闭随机复活规则");
				return true;

			} else if (args[0].equalsIgnoreCase("reload")) {
				if (sender.hasPermission("RandomRespawn.reload")) {
					f = getC();
					sender.sendMessage(prefix + "§2重载成功~~");

					return true;
				}
				sender.sendMessage(prefix + nop);
				return true;
			}
			return false;
		}
		return false;
	}

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
			this.saveDefaultConfig();
			getLogger().info(cname + "配置文件已创建！");
		}
		File file = new File(getDataFolder(), "config.yml");
		if (!file.exists()) {
			this.saveDefaultConfig();
			getLogger().info(cname + "配置文件已创建！");
		} else if (file.exists() && file.isFile()) {
			if (file.length() == 0) {
				file.delete();
				this.saveDefaultConfig();
				getLogger().info(cname + "配置文件已重新生成！");
			}
		}
		this.f = getC();
		dieLocations = new HashMap<String, Location>();
		getLogger().info(cname + "已启用！感谢你的使用！");
	}

	@Override
	public void onDisable() {
		getLogger().info(cname + "已关闭！感谢你的使用！");
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void Death(PlayerDeathEvent de) {
		Player player = de.getEntity();
		if (!player.hasPermission("RandomRespawn.user")) {
		} else {
			dieLocations.put(player.getName(), player.getLocation());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void Respawn(PlayerRespawnEvent re) {
		Player player = re.getPlayer();
		Location diel;
		if (!player.hasPermission("RandomRespawn.user")) {
		} else {
			try 
			{
				diel = dieLocations.get(player.getName());				
			}
			catch(Exception e)
			{
				player.sendMessage(prefix + "§a死亡记录不存在,返回默认重生点");
				return;
			}
			Location defl = re.getRespawnLocation();// 默认出生位置
			World world = diel.getWorld();
			Location newl = diel;
			String n = world.getName();
			if (f.getBoolean("World." + n + ".use")) {
				int x = f.getInt("World." + n + ".X");
				int z = f.getInt("World." + n + ".Z");
				if (x > 0 && z > 0) {
					Block block;
					int rx, rz, lx, ly, lz, cnt;
					rx = rz = lx = ly = lz = cnt = 0;
					while (true) {
						if (++cnt == 10) {
							player.sendMessage(prefix + "§a附近无可用重生点，返回默认重生点");
							break;
						}
						rx = r.nextInt(x);
						rz = r.nextInt(z);
						if (r.nextBoolean()) {
							lx = rx + diel.getBlockX();
						} else {
							lx = -rx + diel.getBlockX();
						}
						if (r.nextBoolean()) {
							lz = rz + diel.getBlockZ();
						} else {
							lz = -rz + diel.getBlockZ();
						}
						if (world.getEnvironment() == Environment.NETHER) {
							int i;
							final int MAXY = 128, MINY = 0, BIAS = 6;
							for (i = MINY + BIAS; i <= MAXY - BIAS; i++) {
								block = world.getBlockAt(lx, i, lz);
								if (block.isEmpty() && world.getBlockAt(lx, i + 1, lz).isEmpty() && world.getBlockAt(lx, i + 2, lz).isEmpty()) {
									if (!world.getBlockAt(lx, i - 1, lz).isLiquid()) {
										ly = i-2;
										break;
									} else {
										i = MAXY;
										break;
									}
								}
							}
							if (i <= MAXY - BIAS) {
								break;
							}
						} else if(world.getEnvironment() == Environment.THE_END){
							int i;
							final int MINY=0,MAXY=128;
							for (i = MINY; i <= MAXY; i++) {
								block = world.getBlockAt(lx, i, lz);
								if (!block.isEmpty() && world.getBlockAt(lx, i+1, lz).isEmpty()) {
									ly = i;
									break;
								}
							}
							if(i<=MAXY) {
								break;
							}
						} else {
							block = world.getHighestBlockAt(lx, lz);
							ly = block.getY();
							if (!(block.isLiquid() || world.getBlockAt(lx, ly - 1, lz).isLiquid())) {
								ly--;
								break;
							}
						}
					}
					if (cnt < 10)
						newl = (new Location(world, lx, ly, lz).add(0.5, 2.3, 0.5));
					else
						newl = defl;
				} else if(x==0&&z==0){
					newl = diel;
				} else {
					newl = defl;
				}
			} else {
				newl = defl;
			}
			getLogger().info(newl.getX()+" "+newl.getY()+" "+newl.getZ());
			re.setRespawnLocation(newl);// 重新设置出生点
		}
		dieLocations.remove(player.getName());
	}

	public void setSize(int x, int z, CommandSender sender, World world) {
		File file = new File(getDataFolder(), "config.yml");
		FileConfiguration config = load(file);
		String n = world.getName();
		if (config.contains("World." + n)) {
			config.set("World." + n + ".X", x);
			config.set("World." + n + ".Z", z);
		} else {
			config.set("World." + n + ".use", true);
			config.set("World." + n + ".X", x);
			config.set("World." + n + ".Z", z);
		}
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
			getLogger().info(cmdpre + "【错误】配置文件写入错误！");
			sender.sendMessage(prefix + "§c【错误】配置文件写入错误！");
		}
		this.f = getC();
	}

	public void setSize(int m, CommandSender sender, World world) {
		File file = new File(getDataFolder(), "config.yml");
		FileConfiguration config = load(file);
		String n = world.getName();
		if (config.contains("World." + n)) {
			config.set("World." + n + ".X", m);
			config.set("World." + n + ".Z", m);
		} else {
			config.set("World." + n + ".use", true);
			config.set("World." + n + ".X", m);
			config.set("World." + n + ".Z", m);
		}
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
			getLogger().info(cmdpre + "【错误】配置文件写入错误！");
			sender.sendMessage(prefix + "§c【错误】配置文件写入错误！");
		}
		this.f = getC();
	}

	public FileConfiguration load(File file) {
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				getLogger().info(cmdpre + "【错误】配置文件读取错误！");
			}
		}
		return YamlConfiguration.loadConfiguration(file);
	}

	public FileConfiguration getC() {
		File file = new File(getDataFolder(), "config.yml");
		return YamlConfiguration.loadConfiguration(file);
	}
}
