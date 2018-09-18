package de.robotricker.transportpipes.rendersystems.pipe.vanilla.model;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.robotricker.transportpipes.ducts.pipe.GoldenPipe;
import de.robotricker.transportpipes.ducts.types.BasicDuctType;
import de.robotricker.transportpipes.ducts.types.PipeType;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.rendersystems.pipe.vanilla.model.data.VanillaPipeModelData;
import de.robotricker.transportpipes.utils.hitbox.AxisAlignedBB;

public abstract class VanillaPipeModel {

    static ItemStack ITEM_BLAZE_ROD = new ItemStack(Material.BLAZE_ROD);
    static ItemStack ITEM_CARPET_WHITE = new ItemStack(Material.CARPET, 1, (short) 0);
    static ItemStack ITEM_CARPET_YELLOW = new ItemStack(Material.CARPET, 1, (short) 4);
    static Map<PipeType, ItemStack> pipeBlocks;
    static Map<GoldenPipe.Color, ItemStack> goldenPipeColorCarpets;

    AxisAlignedBB aabb;

    public abstract List<ArmorStandData> createASD(VanillaPipeModelData data);

    public AxisAlignedBB getAABB() {
        return aabb;
    }

    public static void init() {
        pipeBlocks = new HashMap<>();
        pipeBlocks.put(BasicDuctType.valueOf("Pipe").ductTypeValueOf("White"), new ItemStack(Material.GLASS));
        pipeBlocks.put(BasicDuctType.valueOf("Pipe").ductTypeValueOf("Blue"), new ItemStack(Material.STAINED_GLASS, 1, (short) 11));
        pipeBlocks.put(BasicDuctType.valueOf("Pipe").ductTypeValueOf("Red"), new ItemStack(Material.STAINED_GLASS, 1, (short) 14));
        pipeBlocks.put(BasicDuctType.valueOf("Pipe").ductTypeValueOf("Yellow"), new ItemStack(Material.STAINED_GLASS, 1, (short) 4));
        pipeBlocks.put(BasicDuctType.valueOf("Pipe").ductTypeValueOf("Green"), new ItemStack(Material.STAINED_GLASS, 1, (short) 13));
        pipeBlocks.put(BasicDuctType.valueOf("Pipe").ductTypeValueOf("Black"), new ItemStack(Material.STAINED_GLASS, 1, (short) 15));
        pipeBlocks.put(BasicDuctType.valueOf("Pipe").ductTypeValueOf("Golden"), new ItemStack(Material.GOLD_BLOCK));
        pipeBlocks.put(BasicDuctType.valueOf("Pipe").ductTypeValueOf("Iron"), new ItemStack(Material.IRON_BLOCK));
        pipeBlocks.put(BasicDuctType.valueOf("Pipe").ductTypeValueOf("Ice"), new ItemStack(Material.ICE));
        pipeBlocks.put(BasicDuctType.valueOf("Pipe").ductTypeValueOf("Void"), new ItemStack(Material.OBSIDIAN));
        pipeBlocks.put(BasicDuctType.valueOf("Pipe").ductTypeValueOf("Extraction"), new ItemStack(Material.WOOD));
        pipeBlocks.put(BasicDuctType.valueOf("Pipe").ductTypeValueOf("Crafting"), new ItemStack(Material.WORKBENCH));

        goldenPipeColorCarpets = new HashMap<>();
        goldenPipeColorCarpets.put(GoldenPipe.Color.WHITE, ITEM_CARPET_WHITE);
        goldenPipeColorCarpets.put(GoldenPipe.Color.YELLOW, ITEM_CARPET_YELLOW);
        goldenPipeColorCarpets.put(GoldenPipe.Color.GREEN, new ItemStack(Material.CARPET, 1, (short) 5));
        goldenPipeColorCarpets.put(GoldenPipe.Color.BLUE, new ItemStack(Material.CARPET, 1, (short) 11));
        goldenPipeColorCarpets.put(GoldenPipe.Color.RED, new ItemStack(Material.CARPET, 1, (short) 14));
        goldenPipeColorCarpets.put(GoldenPipe.Color.BLACK, new ItemStack(Material.CARPET, 1, (short) 15));
    }

}
