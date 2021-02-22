package me.nicolly.listeners;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import me.nicolly.CombatLog;

public class PlayerListeners implements Listener {

	@EventHandler
	private void onDamage(EntityDamageByEntityEvent event) {
		if ((event.getEntity() instanceof Player) && (event.getDamager() instanceof Player)) {
			CombatLog.addCombat((Player) event.getEntity());
			CombatLog.addCombat((Player) event.getDamager());
		}
	}
	
	@EventHandler
	private void onDeath(PlayerDeathEvent event) {
		CombatLog.forceRemoveCombat(event.getEntity());
	}

	@EventHandler
	private void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (player != null && CombatLog.inCombat(player)) {
			Bukkit.broadcastMessage(ChatColor.RED + player.getName() + " SAIU EM COMBATE.");
			Set<ItemStack> toDrop = new HashSet<>();
			
			Arrays.stream(player.getInventory().getContents()).forEach(item -> {
				if (item != null && item.getType() != Material.AIR)
					toDrop.add(item);
			});

			Arrays.stream(player.getInventory().getArmorContents()).forEach(item -> {
				if (item != null && item.getType() != Material.AIR)
					toDrop.add(item);
			});
			
			player.getInventory().clear();
			toDrop.forEach(item -> {
				if (item != null && item.getType() != Material.AIR)
					player.getWorld().dropItem(player.getLocation(), item);
			});
		}
	}

}