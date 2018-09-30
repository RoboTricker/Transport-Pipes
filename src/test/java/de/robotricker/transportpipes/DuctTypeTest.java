package de.robotricker.transportpipes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;
import de.robotricker.transportpipes.ducts.types.BaseDuctType;
import de.robotricker.transportpipes.log.SentryService;
import de.robotricker.transportpipes.protocol.ProtocolService;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DuctTypeTest {

    private Injector injector;

    @Before
    public void setUp() throws Exception {
        injector = new InjectorBuilder().addDefaultHandlers("de.robotricker.transportpipes").create();

        ItemStack dummyItem = new ItemStack(Material.APPLE);

        injector.register(Logger.class, mock(Logger.class));

        injector.register(JavaPlugin.class, mock(TransportPipes.class));

        injector.register(SentryService.class, mock(SentryService.class));
        when(injector.getSingleton(SentryService.class).isInitialized()).thenReturn(true);

        injector.register(ItemService.class, mock(ItemService.class));
        when(injector.getSingleton(ItemService.class).createModelledItem(anyInt())).thenReturn(dummyItem);
        when(injector.getSingleton(ItemService.class).createSkullItemStack(any(), any(), any())).thenReturn(dummyItem);
        when(injector.getSingleton(ItemService.class).changeDisplayName(any(), any())).thenReturn(dummyItem);
        when(injector.getSingleton(ItemService.class).setDuctNBTTags(any(), any())).thenReturn(dummyItem);

        injector.register(ProtocolService.class, mock(ProtocolService.class));

        DuctService ductService = spy(injector.newInstance(DuctService.class));
        ductService.register();
        injector.register(DuctService.class, ductService);
    }

    @Test
    public void testDuctItems(){
        assertNotNull(BaseDuctType.valueOf("Pipe"));
        assertNotNull(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Iron"));
        assertNotNull(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Golden"));
        assertNotNull(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Crafting"));
        assertNotNull(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Void"));
        assertNotNull(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Extraction"));
        assertNotNull(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Ice"));
        assertNotNull(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Blue"));
        assertNotNull(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Red"));
        assertNotNull(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Yellow"));
        assertNotNull(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Green"));
        assertNotNull(BaseDuctType.valueOf("Pipe").ductTypeValueOf("White"));
        assertNotNull(BaseDuctType.valueOf("Pipe").ductTypeValueOf("Black"));
        assertNull(BaseDuctType.valueOf("Pipe").ductTypeValueOf("dummy"));
    }

    @After
    public void tearDown() throws Exception {

    }
}
