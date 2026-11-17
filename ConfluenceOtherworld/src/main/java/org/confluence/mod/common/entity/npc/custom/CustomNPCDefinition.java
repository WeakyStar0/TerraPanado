package org.confluence.mod.common.entity.npc.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.confluence.mod.common.entity.npc.chat.ChatLine;
import org.confluence.mod.common.entity.npc.trade.NPCTradeOffer;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/// Datapack definition of a custom NPC: `data/<ns>/npc/definitions/<id>.json`.
///
/// The file id is the NPC id. Every field is optional except that an NPC with no `offers`
/// simply has an empty shop.
public record CustomNPCDefinition(
        String name,
        ResourceLocation baseModel,
        Optional<ResourceLocation> texture,
        Map<String, Float> namesWeights,
        List<String> dialogs,
        List<NPCTradeOffer> offers,
        List<ChatLine> chat,
        double health,
        double damage,
        Item weapon,
        int eggModelData
) {
    public static final ResourceLocation DEFAULT_BASE_MODEL = ResourceLocation.fromNamespaceAndPath("confluence", "guide");

    public static final Codec<CustomNPCDefinition> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.optionalFieldOf("name", "Custom NPC").forGetter(CustomNPCDefinition::name),
            ResourceLocation.CODEC.optionalFieldOf("base_model", DEFAULT_BASE_MODEL).forGetter(CustomNPCDefinition::baseModel),
            ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(CustomNPCDefinition::texture),
            Codec.unboundedMap(Codec.STRING, Codec.FLOAT).optionalFieldOf("names_weights", Map.of()).forGetter(CustomNPCDefinition::namesWeights),
            Codec.STRING.listOf().optionalFieldOf("dialogs", List.of()).forGetter(CustomNPCDefinition::dialogs),
            NPCTradeOffer.CODEC.listOf().optionalFieldOf("offers", List.of()).forGetter(CustomNPCDefinition::offers),
            ChatLine.CODEC.listOf().optionalFieldOf("chat", List.of()).forGetter(CustomNPCDefinition::chat),
            Codec.doubleRange(1.0, 1024.0).optionalFieldOf("health", 65.0).forGetter(CustomNPCDefinition::health),
            Codec.doubleRange(0.0, 1024.0).optionalFieldOf("damage", 3.0).forGetter(CustomNPCDefinition::damage),
            BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("weapon", Items.AIR).forGetter(CustomNPCDefinition::weapon),
            Codec.INT.optionalFieldOf("egg_model_data", 0).forGetter(CustomNPCDefinition::eggModelData)
    ).apply(i, CustomNPCDefinition::new));

    /// The part of a definition clients need: look, name, dialog and spawn egg icon.
    public record View(String name, ResourceLocation baseModel, Optional<ResourceLocation> texture,
                       List<String> dialogs, int eggModelData) {
        public static final Codec<View> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("name").forGetter(View::name),
                ResourceLocation.CODEC.fieldOf("base_model").forGetter(View::baseModel),
                ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(View::texture),
                Codec.STRING.listOf().fieldOf("dialogs").forGetter(View::dialogs),
                Codec.INT.fieldOf("egg_model_data").forGetter(View::eggModelData)
        ).apply(i, View::new));
        public static final Codec<Map<ResourceLocation, View>> MAP_CODEC = Codec.unboundedMap(ResourceLocation.CODEC, CODEC);
    }

    public View view() {
        return new View(name, baseModel, texture, dialogs, eggModelData);
    }
}
