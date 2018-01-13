package de.robotricker.transportpipes.utils.staticutils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.logisticscraft.logisticsapi.data.LogisticBlockFace;
import com.logisticscraft.logisticsapi.event.LogisticBlockLoadEvent;
import com.logisticscraft.logisticsapi.event.LogisticBlockUnloadEvent;
import com.logisticscraft.logisticsapi.item.ItemStorage;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.api.PipeAPI;
import de.robotricker.transportpipes.api.TransportPipesContainer;
import de.robotricker.transportpipes.duct.pipe.utils.FilteringMode;
import de.robotricker.transportpipes.pipeitems.ItemData;
import de.robotricker.transportpipes.utils.WrappedDirection;

public class LogisticsAPIUtils implements Listener {

    @EventHandler
    public void onRegister(final LogisticBlockLoadEvent e) {
        if(e instanceof ItemStorage){
            TransportPipes.instance.getLogger().info("Item container registered at " + e.getLocation());
            PipeAPI.unregisterTransportPipesContainer(e.getLocation());
            PipeAPI.registerTransportPipesContainer(e.getLocation(), wrapLogisticsAPIItemContainer((ItemStorage) e.getLogisticBlock()));
        }
    }

    @EventHandler
    public void onUnregister(LogisticBlockUnloadEvent e) {
        TransportPipes.instance.getLogger().info("Item container unregistered at " + e.getLocation());
        PipeAPI.unregisterTransportPipesContainer(e.getLocation());
    }

    public static TransportPipesContainer wrapLogisticsAPIItemContainer(final ItemStorage ic) {
        return new TransportPipesContainer() {

            @Override
            public ItemStack insertItem(WrappedDirection insertDirection, ItemStack insertion) {
                return ic.insertItem(LogisticBlockFace.getBlockFace(insertDirection.toBlockFace()), insertion, false);
            }

            @Override
            public int howMuchSpaceForItemAsync(WrappedDirection insertDirection, ItemStack insertion) {
                return ic.howMuchSpaceForItemAsync(LogisticBlockFace.getBlockFace(insertDirection.toBlockFace()), insertion);
            }

            @Override
            public ItemStack extractItem(WrappedDirection extractDirection, int extractAmount, List<ItemData> filterItems, FilteringMode filteringMode) {
                List<ItemStack> wrappedFilterItems = new ArrayList<>();
                for (ItemData id : filterItems) {
                    wrappedFilterItems.add(id.toItemStack());
                }
                // TODO: Implement simulated filters first!
                return ic.extractItem(LogisticBlockFace.getBlockFace(extractDirection.toBlockFace()), extractAmount, false);
            }
        };
    }

}
