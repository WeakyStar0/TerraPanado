package org.confluence.mod.client.renderer.block;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.confluence.mod.Confluence;
import org.confluence.mod.common.block.common.BaseChestBlock;
import org.confluence.mod.common.init.block.ChestBlocks;

import java.util.Hashtable;
import java.util.function.BiFunction;

public class BaseChestBlockRenderer extends ChestRenderer<BaseChestBlock.BEntity> {
    private final BiFunction<BlockState, ChestType, Material> function;
    /** Grayscale textures shown to a player that already looted a per-player (Lootr) chest. */
    private final Hashtable<Block, Material[]> lootedCache = Util.make(new Hashtable<>(), map -> {
        for (DeferredBlock<BaseChestBlock> normalChest : ChestBlocks.NORMAL_CHESTS) {
            String chestName = "looted_" + normalChest.getId().getPath().replace("_chest", "");
            ResourceLocation location = Confluence.asResource("entity/chest/" + chestName);
            map.put(normalChest.get(), new Material[]{
                    new Material(Sheets.CHEST_SHEET, location),
                    new Material(Sheets.CHEST_SHEET, location.withSuffix("_left")),
                    new Material(Sheets.CHEST_SHEET, location.withSuffix("_right"))
            });
        }
    });

    public BaseChestBlockRenderer(BlockEntityRendererProvider.Context pContext) {
        super(pContext);
        this.function = new BiFunction<>() {
            private final Material[] defaultMaterials = new Material[]{
                    Sheets.CHEST_LOCATION,
                    Sheets.CHEST_LOCATION_LEFT,
                    Sheets.CHEST_LOCATION_RIGHT
            };
            private final Hashtable<Block, Material[]> unlockedCache = Util.make(new Hashtable<>(), map -> {
                for (DeferredBlock<BaseChestBlock> normalChest : ChestBlocks.NORMAL_CHESTS) {
                    String chestName = "unlocked_" + normalChest.getId().getPath().replace("_chest", "");
                    ResourceLocation location = Confluence.asResource("entity/chest/" + chestName);
                    map.put(normalChest.get(), new Material[]{
                            new Material(Sheets.CHEST_SHEET, location),
                            new Material(Sheets.CHEST_SHEET, location.withSuffix("_left")),
                            new Material(Sheets.CHEST_SHEET, location.withSuffix("_right"))
                    });
                }
            });
            private final Hashtable<Block, Material[]> lockedCache = Util.make(new Hashtable<>(), map -> {
                for (DeferredBlock<BaseChestBlock> normalChest : ChestBlocks.NORMAL_CHESTS) {
                    String chestName = "locked_" + normalChest.getId().getPath().replace("_chest", "");
                    ResourceLocation locked = Confluence.asResource("entity/chest/" + chestName);
                    map.put(normalChest.get(), new Material[]{
                            new Material(Sheets.CHEST_SHEET, locked),
                            new Material(Sheets.CHEST_SHEET, locked.withSuffix("_left")),
                            new Material(Sheets.CHEST_SHEET, locked.withSuffix("_right"))
                    });
                }
            });

            @Override
            public Material apply(BlockState blockState, ChestType chestType) {
                if (blockState.getValue(BaseChestBlock.UNLOCKED)) {
                    return unlockedCache.getOrDefault(blockState.getBlock(), defaultMaterials)[chestType.ordinal()];
                } else {
                    return lockedCache.getOrDefault(blockState.getBlock(), defaultMaterials)[chestType.ordinal()];
                }
            }
        };
    }

    @Override
    protected Material getMaterial(BaseChestBlock.BEntity blockEntity, ChestType chestType) {
        BlockState state = blockEntity.getBlockState();
        if (state.getValue(BaseChestBlock.UNLOCKED) && isLooted(blockEntity)) {
            Material[] looted = lootedCache.get(state.getBlock());
            if (looted != null) return looted[chestType.ordinal()];
        }
        return function.apply(state, chestType);
    }

    public static boolean isLooted(BaseChestBlock.BEntity blockEntity) {
        Player player = Minecraft.getInstance().player;
        return player != null && blockEntity.isLootrChest() && blockEntity.hasLootrOpened(player);
    }
}
