package de.robotricker.transportpipes.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.logisticscraft.logisticsapi.event.ItemContainerRegisterEvent;
import com.logisticscraft.logisticsapi.event.ItemContainerUnregisterEvent;
import com.logisticscraft.logisticsapi.item.ItemContainer;
import com.logisticscraft.logisticsapi.util.bukkit.BlockSide;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.api.PipeAPI;
import de.robotricker.transportpipes.api.TransportPipesContainer;
import de.robotricker.transportpipes.duct.pipe.utils.FilteringMode;
import de.robotricker.transportpipes.pipeitems.ItemData;

public class LogisticsAPIUtils implements Listener {

	@EventHandler
	public void onRegister(final ItemContainerRegisterEvent e) {
		TransportPipes.instance.getLogger().info("Item container registered at " + e.getLocation());
		PipeAPI.unregisterTransportPipesContainer(e.getLocation());
		PipeAPI.registerTransportPipesContainer(e.getLocation(), wrapLogisticsAPIItemContainer(e.getItemContainer()));
	}

	@EventHandler
	public void onUnregister(ItemContainerUnregisterEvent e) {
		TransportPipes.instance.getLogger().info("Item container unregistered at " + e.getLocation());
		PipeAPI.unregisterTransportPipesContainer(e.getLocation());
	}

	public static TransportPipesContainer wrapLogisticsAPIItemContainer(final ItemContainer ic) {
		return new TransportPipesContainer() {

			@Override
			public ItemStack insertItem(WrappedDirection insertDirection, ItemStack insertion) {
				return ic.insertItem(BlockSide.fromBlockFace(insertDirection.toBlockFace()), insertion);
			}

			@Override
			public int howMuchSpaceForItemAsync(WrappedDirection insertDirection, ItemStack insertion) {
				return ic.howMuchSpaceForItemAsync(BlockSide.fromBlockFace(insertDirection.toBlockFace()), insertion);
			}

			@Override
			public ItemStack extractItem(WrappedDirection extractDirection, int extractAmount, List<ItemData> filterItems, FilteringMode filteringMode) {
				List<ItemStack> wrappedFilterItems = new ArrayList<>();
				for (ItemData id : filterItems) {
					wrappedFilterItems.add(id.toItemStack());
				}
				return ic.extractItem(BlockSide.fromBlockFace(extractDirection.toBlockFace()), extractAmount, wrappedFilterItems, com.logisticscraft.logisticsapi.item.FilteringMode.fromId(filteringMode.getId()));
			}
		};
	}

}
