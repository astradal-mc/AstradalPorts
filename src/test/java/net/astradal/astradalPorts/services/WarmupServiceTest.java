package net.astradal.astradalPorts.services;

import net.astradal.astradalPorts.core.PortType;
import net.astradal.astradalPorts.events.PortstoneTeleportEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.core.Portstone;
import net.astradal.astradalPorts.services.hooks.EconomyHook;
import net.astradal.astradalPorts.services.hooks.TownyHook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class WarmupServiceTest {

    private ServerMock server;
    private AstradalPorts plugin;
    private WarmupService warmupService;

    @Mock
    private ConfigService mockConfigService;
    @Mock
    private CooldownService mockCooldownService;

    private EconomyHook economyHook;

    private PlayerMock player;
    private Portstone sourcePortstone;
    private Portstone targetPortstone;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(AstradalPorts.class);
        player = server.addPlayer();

        // We create the fake economy hook first
        economyHook = new FakeEconomyHook(plugin.getLogger(), mockConfigService);
        // Now we can pass it into the fake towny hook
        TownyHook townyHook = new FakeTownyHook(plugin.getLogger(), economyHook);

        warmupService = new WarmupService(plugin, mockConfigService, mockCooldownService, economyHook, townyHook);
        server.getPluginManager().registerEvents(warmupService, plugin);

        sourcePortstone = new Portstone(
            UUID.randomUUID(), PortType.LAND, player.getWorld().getName(),
            0, 70, 0, "TestTown", null, "Source",
            10.0, Material.EMERALD_BLOCK, true
        );

        targetPortstone = new Portstone(
            UUID.randomUUID(), PortType.LAND, player.getWorld().getName(),
            100, 70, 100, "TestTown", null, "Destination",
            10.0, Material.EMERALD_BLOCK, true
        );

    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void warmup_shouldFail_whenPlayerCannotAffordFee() {
        // Arrange
        when(mockConfigService.getWarmup(anyString())).thenReturn(2);
        ((FakeEconomyHook) economyHook).setChargeResult(false);

        // Act
        warmupService.startWarmup(player, sourcePortstone, targetPortstone);

        // Assert the initial message is correct by checking its plain text content
        Component warmupMessage = player.nextComponentMessage();
        assertNotNull(warmupMessage, "The initial warmup message should have been sent.");
        assertEquals("Teleporting in 2 seconds. Don't move!", PlainTextComponentSerializer.plainText().serialize(warmupMessage));

        // Advance the server clock to trigger the teleport attempt
        server.getScheduler().performTicks(3 * 20L);

        // Assert
        // The teleport should have been stopped, so the player hasn't moved.
        assertNotEquals(targetPortstone.getLocation(), player.getLocation());

        // Check for the correct failure message by checking its plain text content
        Component feeMessage = player.nextComponentMessage();
        assertNotNull(feeMessage, "The fee failure message should have been sent.");
        assertEquals("You can't afford the $10.00 travel fee!", PlainTextComponentSerializer.plainText().serialize(feeMessage));

        // Ensure no other messages were sent
        assertNull(player.nextComponentMessage(), "There should be no more messages.");
    }

    @Test
    void teleport_shouldBeCancelled_byEventListener() {
        // Arrange
        Listener cancellingListener = new Listener() {
            @EventHandler
            public void onTeleport(PortstoneTeleportEvent event) {
                event.setCancelled(true);
            }
        };
        server.getPluginManager().registerEvents(cancellingListener, plugin);

        when(mockConfigService.getWarmup(anyString())).thenReturn(0);
        // Our FakeEconomyHook returns true by default, so no setup is needed here.

        // Act
        warmupService.startWarmup(player, sourcePortstone, targetPortstone);

        // Assert
        assertNotEquals(targetPortstone.getLocation(), player.getLocation(), "Player should not be teleported if the event is cancelled.");
        verify(mockCooldownService, never()).applyCooldown(any(), any());

        // Consume the "fee paid" message without checking it
        player.nextMessage();

        // Get the raw component for the cancellation message
        Component cancelMessage = player.nextComponentMessage();
        assertNotNull(cancelMessage, "Cancellation message should have been sent.");

        // Assert its plain text content
        assertEquals("Teleportation was cancelled by another process.", PlainTextComponentSerializer.plainText().serialize(cancelMessage));
    }


    // --- Fake Class Definitions ---

    private static class FakeEconomyHook extends EconomyHook {
        private boolean chargeResult = true;

        public FakeEconomyHook(Logger logger, ConfigService configService) {
            super(logger, configService);
        }

        @Override
        public boolean isEnabled() { return true; }

        @Override
        public boolean chargeFee(Player player, double amount) { return this.chargeResult; }

        @Override
        public String format(double amount) { return "$" + String.format("%.2f", amount); }

        public void setChargeResult(boolean result) { this.chargeResult = result; }
    }

    private static class FakeTownyHook extends TownyHook {
        // The constructor now accepts the EconomyHook
        public FakeTownyHook(Logger logger, EconomyHook economyHook) {
            // And passes it to the real TownyHook's constructor
            super(logger, economyHook);
        }

        @Override
        public boolean isEnabled() { return true; }

        @Override
        public void depositToTownBank(String townName, double amount) {
            // Do nothing for this test.
        }
    }
}