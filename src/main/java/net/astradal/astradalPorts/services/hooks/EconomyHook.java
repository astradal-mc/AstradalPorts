package net.astradal.astradalPorts.services.hooks;

import net.astradal.astradalPorts.services.ConfigService;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Logger;

/**
 * A hook for interacting with the Vault Economy API.
 * This class should be instantiated once and safely handles cases where Vault
 * or an economy plugin is not present.
 */
public class EconomyHook {

    private final Logger logger;
    private final ConfigService configService;
    private boolean enabled = false;
    private Economy economy = null;

    public EconomyHook(Logger logger, ConfigService configService) {
        this.logger = logger;
        this.configService = configService;
    }

    /**
     * Initializes the hook by checking for the Vault plugin and an economy provider.
     * This should be called from the main plugin's onEnable method.
     */
    public void initialize() {
        // 1. Check if economy is enabled in the config
        if (!configService.isEconomyEnabled()) {
            logger.info("Economy features are disabled in the config.yml.");
            return;
        }

        // 2. Check if the Vault plugin is present
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            logger.warning("Vault not found. Economy features will be disabled.");
            return;
        }

        // 3. Try to hook into an economy provider
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            logger.warning("No economy provider found (e.g., EssentialsX). Economy features will be disabled.");
            return;
        }

        this.economy = rsp.getProvider();
        this.enabled = true;
        logger.info("Successfully hooked into Vault and found an economy provider: " + economy.getName());
    }

    /**
     * Checks if the Economy hook is active and ready for use.
     * @return true if the hook is successfully enabled.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Charges a player a fee for a transaction.
     * Respects the 'requireBalance' config setting.
     *
     * @param player The player to charge.
     * @param amount The amount to withdraw.
     * @return true if the transaction was successful, false otherwise.
     */
    public boolean chargeFee(Player player, double amount) {
        if (!enabled || amount <= 0) {
            return true; // No fee to charge, so the transaction is "successful".
        }

        // Check if the player has enough money if required by the config
        if (configService.isEconomyRequireBalance() && !economy.has(player, amount)) {
            return false;
        }

        EconomyResponse response = economy.withdrawPlayer(player, amount);
        return response.transactionSuccess();
    }

    /**
     * Deposits money into an offline player's account. This could be used for town banks.
     * * @param player The offline player (or Towny-managed account).
     * @param amount The amount to deposit.
     */
    public void deposit(OfflinePlayer player, double amount) {
        if (enabled && amount > 0) {
            economy.depositPlayer(player, amount);
        }
    }

    /**
     * Formats a double into a currency string (e.g., "$10.50").
     * @param amount The amount to format.
     * @return The formatted currency string.
     */
    public String format(double amount) {
        if (!enabled) {
            return String.valueOf(amount);
        }
        return economy.format(amount);
    }
}