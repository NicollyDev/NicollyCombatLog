package me.nicolly;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.nicolly.listeners.PlayerListeners;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

public class CombatLog extends JavaPlugin {

	private static CombatLog instance;

	private static final HashMap<UUID, Long> COMBAT_LOG = new HashMap<>();

	@Override
	public void onEnable() {
		instance = this;

		Bukkit.getScheduler().runTaskTimerAsynchronously(getInstance(), () -> {
			Set<UUID> toRemove = new HashSet<>();

			COMBAT_LOG.forEach((uuid, time) -> {
				Player player = Bukkit.getPlayer(uuid);
				if (player == null || !player.isOnline() || !inCombat(player)) {
					toRemove.add(uuid);
					return;
				}
				long seconds = TimeUnit.MILLISECONDS.toSeconds(time - System.currentTimeMillis());
				if (seconds > 0)
					sendActionMessage(player, ChatColor.RED + "Você está em combate por " + seconds + " segundos");
				else
					sendActionMessage(player, ChatColor.GREEN + "Você não está mais em combate!");
			});

			toRemove.forEach(uuid -> {
				COMBAT_LOG.remove(uuid);
			});
		}, 0L, 20L);
		
		getServer().getPluginManager().registerEvents(new PlayerListeners(), getInstance());
	}

	@Override
	public void onDisable() {

	}

	public static CombatLog getInstance() {
		return instance;
	}

	public static void addCombat(Player player) {
		if (player != null)
			COMBAT_LOG.put(player.getUniqueId(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(15));
	}

	public static void forceRemoveCombat(Player player) {
		if (player != null)
			COMBAT_LOG.remove(player.getUniqueId());
	}

	public static boolean inCombat(Player player) {
		if (player == null)
			return false;
		if (!COMBAT_LOG.containsKey(player.getUniqueId()))
			return false;
		return COMBAT_LOG.get(player.getUniqueId()) - System.currentTimeMillis() > 0;
	}

	public static void sendActionMessage(Player player, Object message) {
		if (player == null || !player.isOnline())
			return;
		sendPacket(player,
				(Packet<?>) new PacketPlayOutChat(IChatBaseComponent.ChatSerializer
						.a("{\"text\": \"" + ChatColor.translateAlternateColorCodes('&', message.toString()) + "\"}"),
						(byte) 2));
	}
 
	public static void sendPacket(Player player, Packet<?> packet) {
		if (packet != null && player != null && player.isOnline()) {
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
		}
	}

}
