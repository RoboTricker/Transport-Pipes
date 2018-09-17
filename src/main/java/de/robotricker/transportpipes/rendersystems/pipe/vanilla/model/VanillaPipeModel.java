package de.robotricker.transportpipes.rendersystems.pipe.vanilla.model;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.robotricker.transportpipes.ducts.pipe.PipeType;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.rendersystems.pipe.vanilla.model.data.VanillaPipeModelData;
import de.robotricker.transportpipes.utils.hitbox.AxisAlignedBB;

public abstract class VanillaPipeModel {

    static final ItemStack ITEM_BLAZE_ROD = new ItemStack(Material.BLAZE_ROD);
    static final ItemStack ITEM_CARPET_WHITE = new ItemStack(Material.CARPET, 1, (short) 0);
    static final ItemStack ITEM_CARPET_YELLOW = new ItemStack(Material.CARPET, 1, (short) 4);
    static final ItemStack ITEM_CARPET_GREEN = new ItemStack(Material.CARPET, 1, (short) 5);
    static final ItemStack ITEM_CARPET_BLUE = new ItemStack(Material.CARPET, 1, (short) 11);
    static final ItemStack ITEM_CARPET_RED = new ItemStack(Material.CARPET, 1, (short) 14);
    static final ItemStack ITEM_CARPET_BLACK = new ItemStack(Material.CARPET, 1, (short) 15);

    static Map<PipeType, ItemStack> modelBlocks;

    AxisAlignedBB aabb;

    public abstract List<ArmorStandData> createASD(VanillaPipeModelData data);

    public AxisAlignedBB getAABB() {
        return aabb;
    }

    public static void init() {
        modelBlocks = new HashMap<>();
        modelBlocks.put(PipeType.valueOf("White"), new ItemStack(Material.GLASS));
        modelBlocks.put(PipeType.valueOf("Blue"), new ItemStack(Material.STAINED_GLASS, 1, (short) 11));
        modelBlocks.put(PipeType.valueOf("Red"), new ItemStack(Material.STAINED_GLASS, 1, (short) 14));
        modelBlocks.put(PipeType.valueOf("Yellow"), new ItemStack(Material.STAINED_GLASS, 1, (short) 4));
        modelBlocks.put(PipeType.valueOf("Green"), new ItemStack(Material.STAINED_GLASS, 1, (short) 13));
        modelBlocks.put(PipeType.valueOf("Black"), new ItemStack(Material.STAINED_GLASS, 1, (short) 15));
        modelBlocks.put(PipeType.valueOf("Golden"), new ItemStack(Material.GOLD_BLOCK));
        modelBlocks.put(PipeType.valueOf("Iron"), new ItemStack(Material.IRON_BLOCK));
        modelBlocks.put(PipeType.valueOf("Ice"), new ItemStack(Material.ICE));
        modelBlocks.put(PipeType.valueOf("Void"), new ItemStack(Material.OBSIDIAN));
        modelBlocks.put(PipeType.valueOf("Extraction"), new ItemStack(Material.WOOD));
        modelBlocks.put(PipeType.valueOf("Crafting"), new ItemStack(Material.WORKBENCH));
    }

}
