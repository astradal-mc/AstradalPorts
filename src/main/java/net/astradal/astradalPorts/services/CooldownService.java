package net.astradal.astradalPorts.services;

import net.astradal.astradalPorts.core.PortType;
import net.astradal.astradalPorts.database.repositories.CooldownRepository;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Manages teleportation cooldowns for players.
 * This service caches cooldowns for online players and persists them to the database on logout.
 */
public class CooldownService {

    private final CooldownRepository cooldownRepository;
    private final ConfigService configService;

    // Cache for online players: Player UUID -> Map<PortType, Last Use Timestamp (ms)>
    private final Map<UUID, Map<PortType, Long>> playerCooldowns = new ConcurrentHashMap<>();

    public CooldownService(CooldownRepository cooldownRepository, ConfigService configService) {
        this.cooldownRepository = cooldownRepository;
        this.configService = configService;
    }

    /**
     * Loads a player's cooldown data from the database into the cache.
     * Called when a player joins the server.
     * @param playerUUID The UUID of the player.
     */
    public void loadPlayerCooldowns(UUID playerUUID) {
        Map<String, Long> rawCooldowns = cooldownRepository.getCooldowns(playerUUID);
        Map<PortType, Long> cooldownMap = new HashMap<>();

        rawCooldowns.forEach((typeString, timestamp) ->
            PortType.fromString(typeString).ifPresent(portType -> cooldownMap.put(portType, timestamp)));

        playerCooldowns.put(playerUUID, cooldownMap);
    }

    /**
     * Saves a player's cooldown data from the cache to the database.
     * Called when a player quits the server.
     * @param playerUUID The UUID of the player.
     */
    public void savePlayerCooldowns(UUID playerUUID) {
        Map<PortType, Long> cooldownMap = playerCooldowns.remove(playerUUID);
        if (cooldownMap != null) {
            cooldownMap.forEach((portType, timestamp) ->
                cooldownRepository.saveLastUse(playerUUID, portType.name(), timestamp)
            );
        }
    }

    /**
     * Checks if a player is currently on cooldown for a specific port type.
     * @param player The player to check.
     * @param type The PortType of the cooldown.
     * @return true if the player is on cooldown, false otherwise.
     */
    public boolean isOnCooldown(Player player, PortType type) {
        return getRemainingSeconds(player, type) > 0;
    }

    /**
     * Gets the remaining cooldown time in seconds for a player and port type.
     * @param player The player to check.
     * @param type The PortType of the cooldown.
     * @return The number of seconds remaining, or 0 if not on cooldown.
     */
    public long getRemainingSeconds(Player player, PortType type) {
        long cooldownDurationMillis = TimeUnit.SECONDS.toMillis(configService.getCooldown(type.name()));
        if (cooldownDurationMillis <= 0) {
            return 0; // Cooldown is disabled for this type
        }

        long lastUse = playerCooldowns
            .getOrDefault(player.getUniqueId(), Map.of())
            .getOrDefault(type, 0L);

        long timeSinceLastUse = System.currentTimeMillis() - lastUse;

        if (timeSinceLastUse >= cooldownDurationMillis) {
            return 0; // Cooldown has expired
        }

        return TimeUnit.MILLISECONDS.toSeconds(cooldownDurationMillis - timeSinceLastUse);
    }

    /**
     * Applies a cooldown to a player for a specific port type.
     * This should be called immediately after a successful teleport.
     * @param player The player to apply the cooldown to.
     * @param type The PortType of the cooldown.
     */
    public void applyCooldown(Player player, PortType type) {
        playerCooldowns
            .computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
            .put(type, System.currentTimeMillis());
    }
}