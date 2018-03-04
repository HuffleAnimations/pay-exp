package be.huffle.payexp;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import net.md_5.bungee.api.ChatColor;

public class PayExp extends JavaPlugin implements Listener
{
	public PayExp()
	{
		super();
	}
	
	protected PayExp(final JavaPluginLoader loader, final PluginDescriptionFile description, final File dataFolder, final File file)
	{
		super(loader, description, dataFolder, file);
	}
	
	@Override
	public void onLoad()
	{
		// TODO Stub
	}
	
	@Override
	public void onEnable()
	{
		Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public void onDisable()
	{
		// TODO Stub
	}
	
	public int getTotalExp(Player player) {
		int level = player.getLevel();
		int exp;
		if(level <= 15) {
			exp = level * level + 6 * level;
		} else if(level <= 31) {
			exp = (int)(2.5 *(level * level) - 40.5 * level + 360);
		} else {
			exp = (int)(4.5 *(level * level) - 162.5 * level + 2220);
		}
		return (int)(exp + player.getExpToLevel() * player.getExp());
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equals("payexp") && sender instanceof Player && args.length == 2) {
			Player player = (Player)sender;
			Player target = Bukkit.getPlayer(args[0]);
			int amount = Integer.parseInt(args[1]);
			if(target != null) {
				if(getTotalExp(player) >= amount) {
					target.giveExp(amount);
					int currentexp = getTotalExp(player);
					player.setLevel(0);
					player.setExp(0);
					player.giveExp(currentexp - amount);
					target.sendMessage(ChatColor.GREEN + player.getName() + " has given you " + amount + " exp");
					player.sendMessage(ChatColor.GREEN + "" + amount + " exp has been removed");
				} else {
					player.sendMessage(ChatColor.RED + "Not enough exp");
				}
			} else {
				player.sendMessage(ChatColor.RED + "Player does not exist");
			}
			return true;
		} else if(command.getName().equals("checkexp") && sender instanceof Player) {
			Player player = (Player)sender;
			player.sendMessage(ChatColor.GREEN + "You have " + getTotalExp(player) + " exp");
			return true;
		}
		return false;
	}
}
































