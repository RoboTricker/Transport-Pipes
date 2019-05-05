package de.robotricker.transportpipes.saving;

import net.querz.nbt.CompoundTag;
import net.querz.nbt.ListTag;

import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctRegister;
import de.robotricker.transportpipes.duct.manager.GlobalDuctManager;
import de.robotricker.transportpipes.duct.pipe.CraftingPipe;
import de.robotricker.transportpipes.duct.pipe.ExtractionPipe;
import de.robotricker.transportpipes.duct.pipe.GoldenPipe;
import de.robotricker.transportpipes.duct.pipe.IronPipe;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.duct.pipe.extractionpipe.ExtractAmount;
import de.robotricker.transportpipes.duct.pipe.extractionpipe.ExtractCondition;
import de.robotricker.transportpipes.duct.pipe.filter.FilterMode;
import de.robotricker.transportpipes.duct.pipe.filter.FilterStrictness;
import de.robotricker.transportpipes.duct.pipe.filter.ItemData;
import de.robotricker.transportpipes.duct.pipe.filter.ItemFilter;
import de.robotricker.transportpipes.duct.pipe.items.PipeItem;
import de.robotricker.transportpipes.duct.types.DuctType;
import de.robotricker.transportpipes.items.ItemService;
import de.robotricker.transportpipes.location.BlockLocation;
import de.robotricker.transportpipes.location.RelativeLocation;
import de.robotricker.transportpipes.location.TPDirection;

public class LegacyDuctLoader_v4_3_1 extends DuctLoader {

    @Inject
    private GlobalDuctManager globalDuctManager;
    @Inject
    private DuctRegister ductRegister;
    @Inject
    private ItemService itemService;

    @Override
    public void loadDuctsSync(World world, CompoundTag compoundTag) {
        ListTag<CompoundTag> listTag = (ListTag<CompoundTag>) compoundTag.getListTag("Ducts");

        synchronized (globalDuctManager.getDucts()) {
            Map<Duct, CompoundTag> ductCompoundTagMap = new HashMap<>();
            for (CompoundTag ductTag : listTag) {
                if (!ductTag.getString("DuctType").equals("PIPE")) {
                    continue;
                }
                DuctType ductType;

                String ductDetails = ductTag.getString("DuctDetails");
                String pipeType = ductDetails.split(";")[1].split(":")[1];
                if (pipeType.equals("COLORED")) {
                    String color = ductDetails.split(";")[2].split(":")[1];
                    ductType = ductRegister.baseDuctTypeOf("pipe").ductTypeOf(color);
                } else {
                    ductType = ductRegister.baseDuctTypeOf("pipe").ductTypeOf(pipeType);
                }

                String locString = ductTag.getString("DuctLocation");
                BlockLocation blockLoc = new BlockLocation((int) Double.parseDouble(locString.split(":")[1]), (int) Double.parseDouble(locString.split(":")[2]), (int) Double.parseDouble(locString.split(":")[3]));
                if (ductType == null || blockLoc == null) {
                    continue;
                }
                Duct duct = globalDuctManager.createDuctObject(ductType, blockLoc, world, blockLoc.toLocation(world).getChunk());
                globalDuctManager.registerDuct(duct);
                ductCompoundTagMap.put(duct, ductTag);
            }
            // load duct specific nbt stuff later in order to be able to access other ducts inside this load process
            for (Duct duct : ductCompoundTagMap.keySet()) {
                globalDuctManager.updateDuctConnections(duct);

                CompoundTag ductTag = ductCompoundTagMap.get(duct);

                if (duct.getDuctType().getBaseDuctType().is("pipe")) {
                    //load pipeItems
                    for (CompoundTag pipeItemTag : ductTag.getListTag("PipeItems").asCompoundTagList()) {
                        String relLocString = pipeItemTag.getString("RelLoc");
                        RelativeLocation relLoc = new RelativeLocation(Float.parseFloat(relLocString.split(":")[0]), Float.parseFloat(relLocString.split(":")[1]), Float.parseFloat(relLocString.split(":")[2]));
                        int dirInt = pipeItemTag.getInt("Direction");
                        TPDirection dir = TPDirection.values()[dirInt];
                        String itemString = pipeItemTag.getString("Item");
                        ItemStack item = deserializeLegacyItemString(itemString);

                        PipeItem pipeItem = new PipeItem(item, world, duct.getBlockLoc(), relLoc, dir);
                        ((Pipe) duct).getItems().add(pipeItem);
                    }
                    //load specific pipe stuff
                    if (duct.getDuctType().is("golden")) {
                        int dir = 0;
                        for (CompoundTag line : ductTag.getListTag("Lines").asCompoundTagList()) {
                            int filteringMode = line.getInt("FilteringMode");
                            ItemFilter itemFilter = new ItemFilter();
                            convertFilteringMode(filteringMode, itemFilter);

                            int i = 0;
                            for (CompoundTag itemTag : line.getListTag("Items").asCompoundTagList()) {
                                if (itemTag.containsKey("Item")) {
                                    String itemString = itemTag.getString("Item");
                                    ItemStack item = deserializeLegacyItemString(itemString);

                                    itemFilter.getFilterItems()[i] = new ItemData(item);
                                }
                                i++;
                            }

                            ((GoldenPipe) duct).setItemFilter(GoldenPipe.Color.getByDir(TPDirection.values()[dir]), itemFilter);
                            duct.getSettingsInv().populate();

                            dir++;
                        }
                    } else if (duct.getDuctType().is("extraction")) {
                        int filteringMode = ductTag.getInt("FilteringMode");
                        ItemFilter itemFilter = new ItemFilter();
                        convertFilteringMode(filteringMode, itemFilter);

                        int i = 0;
                        for (CompoundTag itemTag : ductTag.getListTag("Items").asCompoundTagList()) {
                            if (itemTag.containsKey("Item")) {
                                String itemString = itemTag.getString("Item");
                                ItemStack item = deserializeLegacyItemString(itemString);

                                itemFilter.getFilterItems()[i] = new ItemData(item);
                            }
                            i++;
                        }

                        int extractAmountInt = ductTag.getInt("ExtractAmount");
                        int extractConditionInt = ductTag.getInt("ExtractCondition");
                        int extractDirectionInt = ductTag.getInt("ExtractDirection");

                        ((ExtractionPipe) duct).setItemFilter(itemFilter);
                        ((ExtractionPipe) duct).setExtractAmount(ExtractAmount.values()[extractAmountInt]);
                        ((ExtractionPipe) duct).setExtractCondition(ExtractCondition.values()[extractConditionInt]);
                        ((ExtractionPipe) duct).setExtractDirection(extractDirectionInt != -1 ? TPDirection.values()[extractDirectionInt] : null);
                        duct.getSettingsInv().populate();
                    } else if (duct.getDuctType().is("iron")) {
                        int outputDirInt = ductTag.getInt("OutputDirection");
                        ((IronPipe) duct).setCurrentOutputDirection(outputDirInt != -1 ? TPDirection.values()[outputDirInt] : TPDirection.UP);
                    } else if (duct.getDuctType().is("crafting")) {
                        int outputDirInt = ductTag.getInt("OutputDirection");
                        ((CraftingPipe) duct).setOutputDir(outputDirInt != -1 ? TPDirection.values()[outputDirInt] : null);

                        int i = 0;
                        for (CompoundTag itemTag : ductTag.getListTag("RecipeItems").asCompoundTagList()) {
                            if (itemTag.containsKey("Item")) {
                                String itemString = itemTag.getString("Item");
                                ItemStack item = deserializeLegacyItemString(itemString);

                                ((CraftingPipe) duct).getRecipeItems()[i] = new ItemData(item);
                            }
                            i++;
                        }

                        i = 0;
                        for (CompoundTag itemTag : ductTag.getListTag("ProcessItems").asCompoundTagList()) {
                            if (itemTag.containsKey("Item")) {
                                String itemString = itemTag.getString("Item");
                                ItemStack item = deserializeLegacyItemString(itemString);

                                ((CraftingPipe) duct).getCachedItems().add(item);
                            }
                            i++;
                        }

                        duct.getSettingsInv().populate();
                        duct.getSettingsInv().save(null);
                    }
                }

                globalDuctManager.registerDuctInRenderSystems(duct, false);
            }
        }
    }

    private void convertFilteringMode(int legacyFilteringMode, ItemFilter applyTo) {
        if (legacyFilteringMode >= 0 && legacyFilteringMode <= 3) {
            applyTo.setFilterMode(FilterMode.NORMAL);
        } else if (legacyFilteringMode == 4) {
            applyTo.setFilterMode(FilterMode.BLOCK_ALL);
        } else if (legacyFilteringMode == 5) {
            applyTo.setFilterMode(FilterMode.INVERTED);
        }

        if (legacyFilteringMode == 0 || legacyFilteringMode == 1) {
            applyTo.setFilterStrictness(FilterStrictness.MATERIAL);
        } else if (legacyFilteringMode == 2 || legacyFilteringMode == 3) {
            applyTo.setFilterStrictness(FilterStrictness.MATERIAL_METADATA);
        } else {
            applyTo.setFilterStrictness(FilterStrictness.MATERIAL_METADATA);
        }
    }

    private ItemStack deserializeLegacyItemString(String itemString) {
        return itemService.deserializeItemStack(itemString.replaceFirst("item:", "itemStack:"));
    }

}
