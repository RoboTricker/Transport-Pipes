package de.robotricker.transportpipes.rendersystems.pipe.vanilla.model;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.robotricker.transportpipes.duct.DuctRegister;
import de.robotricker.transportpipes.duct.pipe.GoldenPipe;
import de.robotricker.transportpipes.duct.types.pipetype.PipeType;
import de.robotricker.transportpipes.hitbox.AxisAlignedBB;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.rendersystems.pipe.vanilla.model.data.VanillaPipeModelData;

public abstract class VanillaPipeModel {

    static ItemStack ITEM_BLAZE_ROD = new ItemStack(Material.BLAZE_ROD);
    static ItemStack ITEM_CARPET_WHITE = new ItemStack(Material.WHITE_CARPET);
    static ItemStack ITEM_CARPET_YELLOW = new ItemStack(Material.YELLOW_CARPET);
    static Map<PipeType, ItemStack> pipeBlocks;
    static Map<GoldenPipe.Color, ItemStack> goldenPipeColorCarpets;

    AxisAlignedBB aabb;

    public abstract List<ArmorStandData> createASD(VanillaPipeModelData data);

    public AxisAlignedBB getAABB() {
        return aabb;
    }

    public static void init(DuctRegister ductRegister) {
        pipeBlocks = new HashMap<>();
        pipeBlocks.put(ductRegister.baseDuctTypeOf("Pipe").ductTypeOf("White"), new ItemStack(Material.GLASS));
        pipeBlocks.put(ductRegister.baseDuctTypeOf("Pipe").ductTypeOf("Blue"), new ItemStack(Material.BLUE_STAINED_GLASS));
        pipeBlocks.put(ductRegister.baseDuctTypeOf("Pipe").ductTypeOf("Red"), new ItemStack(Material.RED_STAINED_GLASS));
        pipeBlocks.put(ductRegister.baseDuctTypeOf("Pipe").ductTypeOf("Yellow"), new ItemStack(Material.YELLOW_STAINED_GLASS));
        pipeBlocks.put(ductRegister.baseDuctTypeOf("Pipe").ductTypeOf("Green"), new ItemStack(Material.GREEN_STAINED_GLASS));
        pipeBlocks.put(ductRegister.baseDuctTypeOf("Pipe").ductTypeOf("Black"), new ItemStack(Material.BLACK_STAINED_GLASS));
        pipeBlocks.put(ductRegister.baseDuctTypeOf("Pipe").ductTypeOf("Golden"), new ItemStack(Material.GOLD_BLOCK));
        pipeBlocks.put(ductRegister.baseDuctTypeOf("Pipe").ductTypeOf("Iron"), new ItemStack(Material.IRON_BLOCK));
        pipeBlocks.put(ductRegister.baseDuctTypeOf("Pipe").ductTypeOf("Ice"), new ItemStack(Material.ICE));
        pipeBlocks.put(ductRegister.baseDuctTypeOf("Pipe").ductTypeOf("Void"), new ItemStack(Material.OBSIDIAN));
        pipeBlocks.put(ductRegister.baseDuctTypeOf("Pipe").ductTypeOf("Extraction"), new ItemStack(Material.OAK_PLANKS));
        pipeBlocks.put(ductRegister.baseDuctTypeOf("Pipe").ductTypeOf("Crafting"), new ItemStack(Material.CRAFTING_TABLE));

        goldenPipeColorCarpets = new HashMap<>();
        goldenPipeColorCarpets.put(GoldenPipe.Color.WHITE, ITEM_CARPET_WHITE);
        goldenPipeColorCarpets.put(GoldenPipe.Color.YELLOW, ITEM_CARPET_YELLOW);
        goldenPipeColorCarpets.put(GoldenPipe.Color.GREEN, new ItemStack(Material.GREEN_CARPET));
        goldenPipeColorCarpets.put(GoldenPipe.Color.BLUE, new ItemStack(Material.BLUE_CARPET));
        goldenPipeColorCarpets.put(GoldenPipe.Color.RED, new ItemStack(Material.RED_CARPET));
        goldenPipeColorCarpets.put(GoldenPipe.Color.BLACK, new ItemStack(Material.BLACK_CARPET));
    }

}
