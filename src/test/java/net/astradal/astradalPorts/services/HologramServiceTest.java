package net.astradal.astradalPorts.services;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import net.astradal.astradalPorts.core.Portstone;
import net.astradal.astradalPorts.core.PortType;
import net.astradal.astradalPorts.database.repositories.HologramRepository;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.TextDisplay;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HologramServiceTest {

    private HologramService hologramService;

    @Mock
    private HologramRepository mockHologramRepository;

    // We will spy on the mock world to intercept method calls
    private World worldSpy;
    private Portstone testPortstone;

    // The ArgumentCaptor to safely capture the Consumer lambda
    @Captor
    private ArgumentCaptor<Consumer<TextDisplay>> consumerCaptor;

    // Captor for the Component passed to the .text() method
    @Captor
    private ArgumentCaptor<Component> componentCaptor;

    @BeforeEach
    void setUp() {
        ServerMock server = MockBukkit.mock();
        World realWorld = server.addSimpleWorld("test-world");
        worldSpy = spy(realWorld);

        hologramService = new HologramService(Logger.getLogger("TestLogger"), mockHologramRepository);

        testPortstone = new Portstone(
            UUID.randomUUID(), PortType.LAND, "test-world",
            10.0, 64.0, 10.0,
            "TestTown", "TestNation", "Test Port",
            0.0, org.bukkit.Material.LODESTONE, true
        );

        // Override the portstone's location to use our spy world
        Location testLocation = new Location(worldSpy, 10.0, 64.0, 10.0);
        testPortstone = spy(testPortstone);
        doReturn(testLocation).when(testPortstone).getLocation();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void createOrUpdateHologram_shouldSpawnNewHologram_whenNoneExists() {
        // Arrange
        TextDisplay fakeHologram = mock(TextDisplay.class);
        when(fakeHologram.getUniqueId()).thenReturn(UUID.randomUUID());
        doReturn(fakeHologram).when(worldSpy).spawn(any(Location.class), eq(TextDisplay.class), any(Consumer.class));

        // Act
        hologramService.createOrUpdateHologram(testPortstone);

        // Assert
        verify(worldSpy).spawn(any(Location.class), eq(TextDisplay.class), consumerCaptor.capture());

        Consumer<TextDisplay> capturedConsumer = consumerCaptor.getValue();
        capturedConsumer.accept(fakeHologram);
        verify(fakeHologram).text(componentCaptor.capture());

        Component capturedText = componentCaptor.getValue();
        String plainText = PlainTextComponentSerializer.plainText().serialize(capturedText);
        assertEquals("Test Port\n[LAND]", plainText);

        // Get the values from the mocks/spies BEFORE the verify call.
        final String expectedPortstoneId = testPortstone.getId().toString();
        final UUID expectedHologramId = fakeHologram.getUniqueId();

        // Now, use the clean local variables in the verify call.
        verify(mockHologramRepository).saveHologram(eq(expectedPortstoneId), eq(expectedHologramId));
    }

}