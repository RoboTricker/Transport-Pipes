package de.robotricker.transportpipes.utils.staticutils;

import java.util.List;

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

	@EventHandler
	public void onRegister(final com.logisticscraft.logisticsapi.event.LogisticBlockLoadEvent e) {
		if (e.getLogisticBlock() instanceof com.logisticscraft.logisticsapi.item.ItemStorage) {
			TransportPipes.instance.getLogger().info("Item container registered at " + e.getLocation());
			PipeAPI.unregisterTransportPipesContainer(e.getLocation());
			PipeAPI.registerTransportPipesContainer(e.getLocation(), wrapLogisticsAPIItemContainer((com.logisticscraft.logisticsapi.item.ItemStorage) e.getLogisticBlock()));
		}
	}

	@EventHandler
	public void onUnregister(com.logisticscraft.logisticsapi.event.LogisticBlockUnloadEvent e) {
		TransportPipes.instance.getLogger().info("Item container unregistered at " + e.getLocation());
		PipeAPI.unregisterTransportPipesContainer(e.getLocation());
	}

	public static TransportPipesContainer wrapLogisticsAPIItemContainer(final com.logisticscraft.logisticsapi.item.ItemStorage ic) {
		return new TransportPipesContainer() {

			@Override
			public ItemStack insertItem(WrappedDirection insertDirection, ItemStack insertion) {
				return ic.insertItem(com.logisticscraft.logisticsapi.data.LogisticBlockFace.getBlockFace(insertDirection.toBlockFace()), insertion, false);
			}

			@Override
			public int howMuchSpaceForItemAsync(WrappedDirection insertDirection, ItemStack insertion) {
				return ic.howMuchSpaceForItemAsync(com.logisticscraft.logisticsapi.data.LogisticBlockFace.getBlockFace(insertDirection.toBlockFace()), insertion);
			}

			@Override
			public ItemStack extractItem(WrappedDirection extractDirection, int extractAmount, List<ItemData> filterItems, FilteringMode filteringMode) {
				return ic.extractItem(com.logisticscraft.logisticsapi.data.LogisticBlockFace.getBlockFace(extractDirection.toBlockFace()), extractAmount, new LogisticFilter(filterItems, filteringMode), false);
			}
		};
	}

	private static class LogisticFilter implements com.logisticscraft.logisticsapi.item.ItemFilter {

		private List<ItemData> filterItems;
		private FilteringMode filteringMode;

		public LogisticFilter(List<ItemData> filterItems, FilteringMode filteringMode) {
			this.filterItems = filterItems;
			this.filteringMode = filteringMode;
		}

		@Override
		public boolean matchesFilter(ItemStack item) {
			return new ItemData(item).applyFilter(filterItems, filteringMode) > 0;
		}

	}

}
