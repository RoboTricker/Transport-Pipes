package de.robotricker.transportpipes.protocol.pipemodels.modelled;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.pipes.Pipe;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipeutils.PipeColor;
import de.robotricker.transportpipes.pipeutils.PipeDirection;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.protocol.pipemodels.PipeModel;

public abstract class ModelledPipeModel extends PipeModel{

	protected static final ItemStack ITEM_HOE_MID_ICE = new ItemStack(Material.WOOD_HOE, 1, (short) 10);
	protected static final ItemStack ITEM_HOE_CONN_ICE = new ItemStack(Material.WOOD_HOE, 1, (short) 11);
	protected static final ItemStack ITEM_HOE_MID_GOLDEN = new ItemStack(Material.WOOD_HOE, 1, (short) 10);
	protected static final ItemStack ITEM_HOE_CONN_GOLDEN = new ItemStack(Material.WOOD_HOE, 1, (short) 11);
	protected static final ItemStack ITEM_HOE_MID_IRON = new ItemStack(Material.WOOD_HOE, 1, (short) 10);
	protected static final ItemStack ITEM_HOE_CONN_IRON = new ItemStack(Material.WOOD_HOE, 1, (short) 11);
	
	public abstract ArmorStandData createMIDArmorStandData(PipeType pt, PipeColor pc);
	
	public abstract ArmorStandData createCONNArmorStandData(PipeType pt, PipeColor pc, PipeDirection pd);
	
	public void sendPipe(Player p, Pipe pipe){
		
	}
	
	public void updatePipe(Player p, Pipe pipe, List<PipeDirection> oldConns, List<PipeDirection> newConns){
		
	}
	
	public void removePipe(Player p, Pipe pipe){
		
	}

}
