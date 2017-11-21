package de.robotricker.transportpipes.pipeutils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.logisticscraft.logisticsapi.BlockSide;
import com.logisticscraft.logisticsapi.event.ItemContainerRegisterEvent;
import com.logisticscraft.logisticsapi.event.ItemContainerUnregisterEvent;

import de.robotricker.transportpipes.api.PipeAPI;
import de.robotricker.transportpipes.api.TransportPipesContainer;
import de.robotricker.transportpipes.pipeitems.ItemData;
import de.robotricker.transportpipes.pipes.FilteringMode;
import de.robotricker.transportpipes.pipes.PipeDirection;

public class LogisticsAPIUtils implements Listener {

	@EventHandler
	public void onRegister(final ItemContainerRegisterEvent e) {
		System.out.println("Item container registered at " + e.getLocation());
		PipeAPI.unregisterTransportPipesContainer(e.getLocation());
		PipeAPI.registerTransportPipesContainer(e.getLocation(), new TransportPipesContainer() {

			@Override
			public ItemStack insertItem(PipeDirection insertDirection, ItemStack insertion) {
				return e.getItemContainer().insertItem(BlockSide.fromBlockFace(insertDirection.toBlockFace()), insertion);
			}

			@Override
			public int howMuchSpaceForItemAsync(PipeDirection insertDirection, ItemStack insertion) {
				return e.getItemContainer().howMuchSpaceForItemAsync(BlockSide.fromBlockFace(insertDirection.toBlockFace()), insertion);
			}

			@Override
			public ItemStack extractItem(PipeDirection extractDirection, int extractAmount, List<ItemData> filterItems, FilteringMode filteringMode) {
				List<ItemStack> wrappedFilterItems = new ArrayList<>();
				for (ItemData id : filterItems) {
					wrappedFilterItems.add(id.toItemStack());
				}
				return e.getItemContainer().extractItem(BlockSide.fromBlockFace(extractDirection.toBlockFace()), extractAmount, wrappedFilterItems, com.logisticscraft.logisticsapi.item.FilteringMode.fromId(filteringMode.getId()));
			}
		});
	}

	@EventHandler
	public void onUnregister(ItemContainerUnregisterEvent e) {
		System.out.println("Item container unregistered at " + e.getLocation());
		PipeAPI.unregisterTransportPipesContainer(e.getLocation());
	}

}
