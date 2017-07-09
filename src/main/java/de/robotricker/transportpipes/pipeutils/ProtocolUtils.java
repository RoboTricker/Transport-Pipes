package de.robotricker.transportpipes.pipeutils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.comphenix.packetwrapper.WrapperPlayClientWindowClick;
import com.comphenix.packetwrapper.WrapperPlayClientWindowClick.InventoryClickType;
import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import com.comphenix.packetwrapper.WrapperPlayServerSetSlot;
import com.comphenix.packetwrapper.WrapperPlayServerWindowItems;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.pipes.PipeType;

public class ProtocolUtils {

	public static void init() {
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(TransportPipes.instance, PacketType.Play.Server.WINDOW_ITEMS, PacketType.Play.Server.SET_SLOT) {

			@Override
			public void onPacketSending(PacketEvent event) {
				PacketContainer p = event.getPacket();

				//replace blaze rod items with wooden tool textured item only on client side if player has model system active
				boolean playerUsesModelSystem = TransportPipes.armorStandProtocol.getPlayerPipeManager(event.getPlayer()).equals(TransportPipes.modelledPipeManager);
				if (playerUsesModelSystem) {
					if (p.getType() == PacketType.Play.Server.WINDOW_ITEMS) {
						WrapperPlayServerWindowItems wrapper = new WrapperPlayServerWindowItems(p);
						List<ItemStack> newList = new ArrayList<ItemStack>();
						for (int i = 0; i < wrapper.getSlotData().size(); i++) {
							ItemStack before = wrapper.getSlotData().get(i);
							newList.add(replaceVanillaWithModelledItemStack(before));
						}
						wrapper.setSlotData(newList);
						event.setPacket(wrapper.getHandle());
					} else {
						WrapperPlayServerSetSlot wrapper = new WrapperPlayServerSetSlot(p);
						ItemStack before = wrapper.getSlotData();
						wrapper.setSlotData(replaceVanillaWithModelledItemStack(before));
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
				boolean playerUsesModelSystem = TransportPipes.armorStandProtocol.getPlayerPipeManager(event.getPlayer()).equals(TransportPipes.modelledPipeManager);
				if (playerUsesModelSystem) {
					PacketContainer p = event.getPacket();
					final WrapperPlayClientWindowClick wrapper = new WrapperPlayClientWindowClick(p);

					ItemStack clickedItem = replaceModelledWithVanillaItemStack(wrapper.getClickedItem());
					wrapper.setClickedItem(clickedItem);
					ItemStack cursorItem = event.getPlayer().getOpenInventory().getCursor();

					if (isPipeOrWrench(clickedItem) && wrapper.getShift() != InventoryClickType.PICKUP_ALL) {
						if (cursorItem.isSimilar(clickedItem) && wrapper.getButton() == 0) {
							WrapperPlayServerSetSlot w = new WrapperPlayServerSetSlot();
							w.setWindowId(wrapper.getWindowId());
							w.setSlot(wrapper.getSlot());
							w.setSlotData(changeItemStackAmount(clickedItem, cursorItem.getAmount()));
							w.sendPacket(event.getPlayer());

							w = new WrapperPlayServerSetSlot();
							w.setWindowId(-1);
							w.setSlot(-1);
							w.setSlotData(changeItemStackAmount(cursorItem, -(64 - clickedItem.getAmount())));
							w.sendPacket(event.getPlayer());
						} else if (cursorItem.isSimilar(clickedItem) && wrapper.getButton() == 1) {
							WrapperPlayServerSetSlot w = new WrapperPlayServerSetSlot();
							w.setWindowId(wrapper.getWindowId());
							w.setSlot(wrapper.getSlot());
							w.setSlotData(changeItemStackAmount(clickedItem, 1));
							w.sendPacket(event.getPlayer());

							w = new WrapperPlayServerSetSlot();
							w.setWindowId(-1);
							w.setSlot(-1);
							ItemStack item = changeItemStackAmount(cursorItem, clickedItem.getAmount() < 64 ? -1 : 0);
							w.setSlotData(item);
							w.sendPacket(event.getPlayer());
						}
					} else if (wrapper.getShift() == InventoryClickType.PICKUP_ALL && isPipeOrWrench(cursorItem) && clickedItem.getType() == Material.AIR) {
						WrapperPlayServerSetSlot w = new WrapperPlayServerSetSlot();
						w.setWindowId(-1);
						w.setSlot(-1);

						int amount = 0;
						for (ItemStack ii : event.getPlayer().getInventory()) {
							if (ii != null && ii.hasItemMeta() && ii.getItemMeta().hasDisplayName() && ii.getItemMeta().getDisplayName().equals(cursorItem.getItemMeta().getDisplayName())) {
								amount += ii.getAmount();
							}
						}

						ItemStack item = changeItemStackAmount(cursorItem, amount);
						w.setSlotData(item);
						w.sendPacket(event.getPlayer());
					}

					event.setPacket(wrapper.getHandle());

				}
			}
		});
		
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(TransportPipes.instance, PacketType.Play.Server.ENTITY_METADATA) {
			@Override
			public void onPacketSending(PacketEvent event) {
				PacketContainer p = event.getPacket();
				WrapperPlayServerEntityMetadata wrapper = new WrapperPlayServerEntityMetadata(p);
				for(int i = 0; i < wrapper.getMetadata().size(); i++){
					WrappedWatchableObject wwo = wrapper.getMetadata().get(i);
//					if(wwo.getIndex() == 6 && wwo.getValue() instanceof CraftItemStack){
//						
//					}
				}
			}
		});
		
	}

	private static boolean isPipeOrWrench(ItemStack is) {
		return PipeType.getFromPipeItem(is) != null || TransportPipes.isItemStackWrench(is);
	}

	private static ItemStack changeItemStackAmount(ItemStack is, int added) {
		if (is.getAmount() + added <= 0) {
			return new ItemStack(Material.AIR);
		}
		is = is.clone();
		is.setAmount(Math.min(is.getAmount() + added, 64));
		return is;
	}

	private static ItemStack replaceVanillaWithModelledItemStack(ItemStack before) {
		if (before == null || before.getType() == Material.AIR) {
			return before;
		}
		PipeType pt = PipeType.getFromPipeItem(before);
		PipeColor pc = PipeColor.getPipeColorByPipeItem(before);
		boolean wrench = TransportPipes.isItemStackWrench(before);

		ItemStack modelledIs = null;
		ItemStack vanillaIs = null;

		if (!wrench && pt != null) {
			modelledIs = TransportPipes.modelledPipeManager.getPipeItem(pt, pc);
			vanillaIs = TransportPipes.vanillaPipeManager.getPipeItem(pt, pc);
		} else if (wrench) {
			modelledIs = TransportPipes.modelledPipeManager.getWrenchItem();
			vanillaIs = TransportPipes.vanillaPipeManager.getWrenchItem();
		}

		if (before.isSimilar(vanillaIs)) {
			ItemStack returnedIs = modelledIs.clone();
			returnedIs.setAmount(before.getAmount());
			return returnedIs;
		}
		return before;
	}

	private static ItemStack replaceModelledWithVanillaItemStack(ItemStack before) {
		if (before == null || before.getType() == Material.AIR) {
			return before;
		}
		PipeType pt = PipeType.getFromPipeItem(before);
		PipeColor pc = PipeColor.getPipeColorByPipeItem(before);
		boolean wrench = TransportPipes.isItemStackWrench(before);

		ItemStack modelledIs = null;
		ItemStack vanillaIs = null;

		if (!wrench && pt != null) {
			modelledIs = TransportPipes.modelledPipeManager.getPipeItem(pt, pc);
			vanillaIs = TransportPipes.vanillaPipeManager.getPipeItem(pt, pc);
		} else if (wrench) {
			modelledIs = TransportPipes.modelledPipeManager.getWrenchItem();
			vanillaIs = TransportPipes.vanillaPipeManager.getWrenchItem();
		}

		if (before.isSimilar(modelledIs)) {
			ItemStack returnedIs = vanillaIs.clone();
			returnedIs.setAmount(before.getAmount());
			return returnedIs;
		}
		return before;
	}

}
