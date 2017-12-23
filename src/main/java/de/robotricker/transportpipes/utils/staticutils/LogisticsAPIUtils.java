package de.robotricker.transportpipes.utils.staticutils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.api.PipeAPI;
import de.robotricker.transportpipes.api.TransportPipesContainer;
import de.robotricker.transportpipes.duct.pipe.utils.FilteringMode;
import de.robotricker.transportpipes.pipeitems.ItemData;
import de.robotricker.transportpipes.utils.WrappedDirection;

public class LogisticsAPIUtils implements Listener {

	/*@EventHandler
	public void onRegister(final com.logisticscraft.logisticsapi.event.ItemContainerRegisterEvent e) {
		TransportPipes.instance.getLogger().info("Item container registered at " + e.getLocation());
		PipeAPI.unregisterTransportPipesContainer(e.getLocation());
		PipeAPI.registerTransportPipesContainer(e.getLocation(), wrapLogisticsAPIItemContainer(e.getItemContainer()));
	}

	@EventHandler
	public void onUnregister(com.logisticscraft.logisticsapi.event.ItemContainerUnregisterEvent e) {
		TransportPipes.instance.getLogger().info("Item container unregistered at " + e.getLocation());
		PipeAPI.unregisterTransportPipesContainer(e.getLocation());
	}

	public static TransportPipesContainer wrapLogisticsAPIItemContainer(final com.logisticscraft.logisticsapi.item.ItemContainer ic) {
		return new TransportPipesContainer() {

			@Override
			public ItemStack insertItem(WrappedDirection insertDirection, ItemStack insertion) {
				return ic.insertItem(com.logisticscraft.logisticsapi.util.bukkit.BlockSide.fromBlockFace(insertDirection.toBlockFace()), insertion);
			}

			@Override
			public int howMuchSpaceForItemAsync(WrappedDirection insertDirection, ItemStack insertion) {
				return ic.howMuchSpaceForItemAsync(com.logisticscraft.logisticsapi.util.bukkit.BlockSide.fromBlockFace(insertDirection.toBlockFace()), insertion);
			}

			@Override
			public ItemStack extractItem(WrappedDirection extractDirection, int extractAmount, List<ItemData> filterItems, FilteringMode filteringMode) {
				List<ItemStack> wrappedFilterItems = new ArrayList<>();
				for (ItemData id : filterItems) {
					wrappedFilterItems.add(id.toItemStack());
				}
				return ic.extractItem(com.logisticscraft.logisticsapi.util.bukkit.BlockSide.fromBlockFace(extractDirection.toBlockFace()), extractAmount, wrappedFilterItems, com.logisticscraft.logisticsapi.item.FilteringMode.fromId(filteringMode.getId()));
			}
		};
	}*/

}
