package de.robotricker.transportpipes.pipeutils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.comphenix.packetwrapper.WrapperPlayClientWindowClick;
import com.comphenix.packetwrapper.WrapperPlayClientWindowClick.InventoryClickType;
import com.comphenix.packetwrapper.WrapperPlayServerSetSlot;
import com.comphenix.packetwrapper.WrapperPlayServerWindowItems;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipes.PipeType;

public class ProtocolUtils {

	public static void init() {
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(TransportPipes.instance, PacketType.Play.Server.WINDOW_ITEMS, PacketType.Play.Server.SET_SLOT) {

			@Override
			public void onPacketSending(PacketEvent event) {
				PacketContainer p = event.getPacket();

				//replace blaze rod items with wooden tool textured item only on client side if player has model system active
				boolean modelledPipeManager = TransportPipes.armorStandProtocol.getPlayerPipeManager(event.getPlayer()).equals(TransportPipes.modelledPipeManager);
				if (modelledPipeManager) {
					if (p.getType() == PacketType.Play.Server.WINDOW_ITEMS) {
						WrapperPlayServerWindowItems wrapper = new WrapperPlayServerWindowItems(p);
						List<ItemStack> newList = new ArrayList<ItemStack>();
						for (int i = 0; i < wrapper.getSlotData().size(); i++) {
							ItemStack before = wrapper.getSlotData().get(i);
							newList.add(TransportPipes.replaceVanillaWithModelledItemStack(before));
						}
						wrapper.setSlotData(newList);
						event.setPacket(wrapper.getHandle());
					} else {
						WrapperPlayServerSetSlot wrapper = new WrapperPlayServerSetSlot(p);
						ItemStack before = wrapper.getSlotData();
						wrapper.setSlotData(TransportPipes.replaceVanillaWithModelledItemStack(before));
						event.setPacket(wrapper.getHandle());
					}
				}
			}
		});

		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(TransportPipes.instance, PacketType.Play.Client.WINDOW_CLICK) {

			@Override
			public void onPacketReceiving(final PacketEvent event) {

				//if client player has model system active, a click on the fake wooden tool in his inventory will be converted to a click on a blaze rod on the server,
				//therefore the server doesn't know anything
				boolean modelledPipeManager = TransportPipes.armorStandProtocol.getPlayerPipeManager(event.getPlayer()).equals(TransportPipes.modelledPipeManager);
				if (modelledPipeManager) {
					PacketContainer p = event.getPacket();
					final WrapperPlayClientWindowClick wrapper = new WrapperPlayClientWindowClick(p);

					if (wrapper.getClickedItem().getType() != Material.AIR) {
						ItemStack modelledItem = wrapper.getClickedItem();
						if (isModelledItem(modelledItem) && event.getPlayer().getOpenInventory().getCursor().getType() != Material.AIR && wrapper.getButton() == 0) {
							WrapperPlayServerSetSlot w = new WrapperPlayServerSetSlot();
							w.setWindowId(wrapper.getWindowId());
							w.setSlot(wrapper.getSlot());
							w.setSlotData(increaseAmount(modelledItem, event.getPlayer().getOpenInventory().getCursor().getAmount()));
							w.sendPacket(event.getPlayer());

							w = new WrapperPlayServerSetSlot();
							w.setWindowId(-1);
							w.setSlot(-1);
							w.setSlotData(new ItemStack(Material.AIR));
							w.sendPacket(event.getPlayer());
						} else if (isModelledItem(modelledItem) && event.getPlayer().getOpenInventory().getCursor().getType() != Material.AIR && wrapper.getButton() == 1) {
							WrapperPlayServerSetSlot w = new WrapperPlayServerSetSlot();
							w.setWindowId(wrapper.getWindowId());
							w.setSlot(wrapper.getSlot());
							w.setSlotData(increaseAmount(modelledItem, 1));
							w.sendPacket(event.getPlayer());

							w = new WrapperPlayServerSetSlot();
							w.setWindowId(-1);
							w.setSlot(-1);
							ItemStack item = modelledItem.getAmount() >= 2 ? modelledItem : new ItemStack(Material.AIR, 2);
							item.setAmount(item.getAmount() - 1);
							w.setSlotData(item);
							w.sendPacket(event.getPlayer());
						}

						wrapper.setClickedItem(TransportPipes.replaceModelledWithVanillaItemStack(wrapper.getClickedItem()));
					} else if (wrapper.getShift() == InventoryClickType.PICKUP_ALL) {
						WrapperPlayServerSetSlot w = new WrapperPlayServerSetSlot();
						w.setWindowId(-1);
						w.setSlot(-1);

						int amount = 0;
						for (ItemStack ii : event.getPlayer().getInventory()) {
							if (ii != null && ii.hasItemMeta() && ii.getItemMeta().hasDisplayName() && ii.getItemMeta().getDisplayName().equals(event.getPlayer().getOpenInventory().getCursor().getItemMeta().getDisplayName())) {
								amount += ii.getAmount();
							}
						}

						ItemStack f = TransportPipes.replaceVanillaWithModelledItemStack(event.getPlayer().getOpenInventory().getCursor());
						
						ItemStack item = increaseAmount(f, amount);
						w.setSlotData(item);
						w.sendPacket(event.getPlayer());
					}

					event.setPacket(wrapper.getHandle());

				}
			}
		});
	}

	private static ItemStack increaseAmount(ItemStack is, int inc) {
		is = is.clone();
		is.setAmount(is.getAmount() + inc);
		return is;
	}

	private static boolean isModelledItem(ItemStack is) {
		if (is.getType() == Material.WOOD_PICKAXE) {
			if (PipeType.getFromPipeItem(is) != null || TransportPipes.isItemStackWrench(is)) {
				return true;
			}
		}
		return false;
	}

}
