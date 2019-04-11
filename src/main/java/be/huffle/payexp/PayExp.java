package be.huffle.payexp;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;

public class PayExp extends JavaPlugin implements Listener
{
	private int amount;
	private Map<Player, ItemStack> itemStackMap = new HashMap<>();
	private Map<Projectile, Integer> projectileIntegerMap = new HashMap<>();
	private Map<Block, ItemStack> blockItemStackMap = new HashMap<>();

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

	public int getTotalExp(Player player)
	{
		int level = player.getLevel();
		int exp;
		if(level <= 15)
		{
			exp = level * level + 6 * level;
		}
		else if(level <= 31)
		{
			exp = (int)(2.5 *(level * level) - 40.5 * level + 360);
		}
		else
		{
			exp = (int)(4.5 *(level * level) - 162.5 * level + 2220);
		}
		return (int)(exp + player.getExpToLevel() * player.getExp());
	}

	private ItemStack getBottle(Player player, int amount)
	{
		String bottleText = String.format("%s%sXP: %d", ChatColor.BOLD, ChatColor.GREEN, amount);

		ItemStack bottle = new ItemStack(Material.EXPERIENCE_BOTTLE);
		ItemMeta bottleMeta = bottle.getItemMeta();
		bottleMeta.setDisplayName(bottleText);
		bottle.setItemMeta(bottleMeta);


		return bottle;
	}

	private void setAmount(int amount)
	{
		this.amount = amount;
	}

	@EventHandler
	public void throwExpBottle(PlayerInteractEvent event)
	{
		ItemStack item = event.getItem();
		if (item != null && item.getType() == Material.EXPERIENCE_BOTTLE)
		{
			itemStackMap.put(event.getPlayer(), item);
		}
	}

	@EventHandler
	public void throwXpBottle(ExpBottleEvent event)
	{
		Projectile projectile = event.getEntity();
		if(projectileIntegerMap.containsKey(projectile))
		{
			int amount = projectileIntegerMap.remove(projectile);
			event.setExperience(amount);
		}
	}

	@EventHandler
	public void onThrow(ProjectileLaunchEvent event)
	{
		Projectile entity = event.getEntity();
		ProjectileSource source = ((Projectile) entity).getShooter();
		if (source instanceof Player)
		{
			Player player = (Player)source;
			ItemStack stack = itemStackMap.remove(player);
			storeXPAmountForProjectile(stack, entity);
		}
		else if (source instanceof BlockProjectileSource)
		{
			Block block = ((BlockProjectileSource)source).getBlock();
			ItemStack stack = blockItemStackMap.remove(block);
			storeXPAmountForProjectile(stack, entity);
		}
	}

	private void storeXPAmountForProjectile(ItemStack stack, Projectile entity)
	{
		if (stack != null && stack.hasItemMeta() && stack.getItemMeta().hasDisplayName())
		{
			int amount = Integer.parseInt(stack.getItemMeta().getDisplayName().substring(String.format("%s%sXP: ",
					ChatColor.BOLD, ChatColor.GREEN).length()));
			projectileIntegerMap.put(entity, amount);
		}
	}

	@EventHandler
	public void dispenseXP(BlockDispenseEvent event)
	{
		ItemStack item = event.getItem();
		if (item != null && item.getType() == Material.EXPERIENCE_BOTTLE)
		{
			blockItemStackMap.put(event.getBlock(), item);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (command.getName().equals("payexp") && sender instanceof Player && args.length == 2)
		{
			Player player = (Player)sender;
			Player target = Bukkit.getPlayer(args[0]);
			int amount = Integer.parseInt(args[1]);
			if (target != null) {
				if(getTotalExp(player) >= amount) {
					target.giveExp(amount);
					int currentexp = getTotalExp(player);
					player.setLevel(0);
					player.setExp(0);
					player.giveExp(currentexp - amount);
					target.sendMessage(ChatColor.GREEN + player.getName() + " has given you " + amount + " exp");
					player.sendMessage(ChatColor.GREEN + "" + amount + " exp has been removed");
				}
				else
				{
					player.sendMessage(ChatColor.RED + "Not enough exp");
				}
			}
			else
			{
				player.sendMessage(ChatColor.RED + "Player does not exist");
			}
			return true;
		}
		else if (command.getName().equals("checkexp") && sender instanceof Player)
		{
			Player player = (Player)sender;
			player.sendMessage(ChatColor.GREEN + "You have " + getTotalExp(player) + " exp");
			return true;
		}
		else if (command.getName().equals("bottlexp") && sender instanceof Player && args.length == 1)
		{
			Player player = (Player)sender;
			int amount = Integer.parseInt(args[0]);
			if(getTotalExp(player) >= amount)
			{
				int currentexp = getTotalExp(player);
				ItemStack bottle = getBottle(player, amount);
				player.setLevel(0);
				player.setExp(0);
				player.giveExp(currentexp - amount);
				player.sendMessage(ChatColor.GREEN + "" + amount + " xp has been bottled up");
				if(player.getInventory().firstEmpty() == -1)
				{
					player.getLocation().getWorld().dropItemNaturally(player.getLocation(), bottle);
					player.sendMessage(ChatColor.GOLD + "Your inventory was full, so the bottle has been" +
							"thrown on the ground");
				}
				else
				{
					player.getInventory().addItem(bottle);
					player.sendMessage(ChatColor.GOLD + "The bottle has been placed in your inventory");
				}
				setAmount(amount);
			}
			else
			{
				player.sendMessage(ChatColor.RED + "The given amount is too high!");
			}
			return true;
		}

		return false;
	}
}
































