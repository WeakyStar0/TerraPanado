package org.confluence.mod.common.wip;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.confluence.lib.common.LibTags;
import org.confluence.mod.Confluence;
import org.confluence.mod.ModStartupConfigs;
import org.confluence.mod.common.recipe.ItemTransmutationRecipe;

import java.util.ArrayList;
import java.util.List;

/// Items tagged [LibTags.Items#WIP] are unfinished. Besides hiding them from the creative tabs, this stops them
/// from being obtained any other way: recipes (including Shimmer), loot (see [RemoveWipLootModifier]) and NPC shops.
/// Turned off by the `forceAllowWipItemsDisplayInCreativeModeTab` startup option, which is meant for development.
@EventBusSubscriber(modid = Confluence.MODID)
public final class WipRemoval {
    private WipRemoval() {}

    public static boolean enabled() {
        return !ModStartupConfigs.forceAllowWipItemsDisplayInCreativeModeTab();
    }

    public static boolean isWip(ItemStack stack) {
        return !stack.isEmpty() && stack.is(LibTags.Items.WIP);
    }

    /// First server start. Recipes are already loaded and the item tags are bound by now.
    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        filterRecipes(event.getServer());
    }

    /// `/reload` and datapack changes. Recipes are replaced by the reload, so they have to be filtered again.
    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() != TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) return;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) filterRecipes(server);
    }

    private static void filterRecipes(MinecraftServer server) {
        if (!enabled()) return;
        RecipeManager manager = server.getRecipeManager();
        List<RecipeHolder<?>> kept = new ArrayList<>();
        int removed = 0, trimmed = 0;
        for (RecipeHolder<?> holder : manager.getRecipes()) {
            if (holder.value() instanceof ItemTransmutationRecipe shimmer) {
                List<ItemStack> targets = shimmer.target().stream().filter(stack -> !isWip(stack)).toList();
                if (targets.size() == shimmer.target().size()) {
                    kept.add(holder);
                } else if (targets.isEmpty()) {
                    removed++;
                } else {
                    kept.add(new RecipeHolder<>(holder.id(), new ItemTransmutationRecipe(shimmer.source(), targets, shimmer.shrink(), shimmer.gamePhase())));
                    trimmed++;
                }
                continue;
            }
            ItemStack result;
            try {
                result = holder.value().getResultItem(server.registryAccess());
            } catch (RuntimeException e) {
                kept.add(holder);
                continue;
            }
            if (isWip(result)) removed++;
            else kept.add(holder);
        }
        if (removed > 0 || trimmed > 0) {
            manager.replaceRecipes(kept);
            Confluence.LOGGER.info("Removed {} recipes for unfinished items and trimmed {} Shimmer recipes", removed, trimmed);
        }
    }
}
