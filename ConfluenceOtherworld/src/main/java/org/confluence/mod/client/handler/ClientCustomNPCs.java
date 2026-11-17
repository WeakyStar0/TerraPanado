package org.confluence.mod.client.handler;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import org.confluence.mod.common.init.item.SpawnEggItems;
import net.minecraft.util.RandomSource;
import org.confluence.mod.common.entity.npc.custom.CustomNPCDefinition;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/// Client copy of the custom NPC definitions the connected server sent.
public final class ClientCustomNPCs {
    private static Map<ResourceLocation, CustomNPCDefinition.View> views = Map.of();

    private ClientCustomNPCs() {}

    public static void update(Map<ResourceLocation, CustomNPCDefinition.View> value) {
        views = Map.copyOf(value);
    }

    public static void clear() {
        views = Map.of();
    }

    public static Map<ResourceLocation, CustomNPCDefinition.View> all() {
        return views;
    }

    public static @Nullable CustomNPCDefinition.View get(@Nullable ResourceLocation id) {
        return id == null ? null : views.get(id);
    }

    public static @Nullable String randomDialogKey(RandomSource random, @Nullable ResourceLocation id) {
        CustomNPCDefinition.View view = get(id);
        if (view == null || view.dialogs().isEmpty()) return null;
        return view.dialogs().get(random.nextInt(view.dialogs().size()));
    }

    /// One spawn egg per definition. The egg is the shared custom NPC egg carrying the NPC id as entity data;
    /// `egg_model_data` becomes custom_model_data so a resource pack can give each egg its own icon.
    public static void addEggs(CreativeModeTab.Output output) {
        views.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> output.accept(createEgg(entry.getKey(), entry.getValue())));
    }

    public static ItemStack createEgg(ResourceLocation id, CustomNPCDefinition.View view) {
        ItemStack stack = new ItemStack(SpawnEggItems.CUSTOM_NPC_SPAWN_EGG.get());
        CompoundTag tag = new CompoundTag();
        tag.putString("id", "confluence:custom_npc");
        tag.putString("CustomNpcId", id.toString());
        stack.set(DataComponents.ENTITY_DATA, CustomData.of(tag));
        if (view.eggModelData() != 0) stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(view.eggModelData()));
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable("item.confluence.custom_npc_spawn_egg.named", view.name()).withStyle(style -> style.withItalic(false)));
        return stack;
    }
}
