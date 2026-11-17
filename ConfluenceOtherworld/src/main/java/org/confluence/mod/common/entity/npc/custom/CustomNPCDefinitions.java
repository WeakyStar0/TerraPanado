package org.confluence.mod.common.entity.npc.custom;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.confluence.mod.Confluence;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/// Server-side store of datapack NPC definitions. Reloads with `/reload`; a broken file is
/// logged and skipped without affecting the others.
public final class CustomNPCDefinitions extends SimpleJsonResourceReloadListener {
    private static final CustomNPCDefinitions INSTANCE = new CustomNPCDefinitions();
    private static volatile Map<ResourceLocation, CustomNPCDefinition> definitions = Map.of();

    private CustomNPCDefinitions() {
        super(new Gson(), "npc/definitions");
    }

    public static CustomNPCDefinitions getInstance() {
        return INSTANCE;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, CustomNPCDefinition> loaded = new HashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            try {
                DataResult<CustomNPCDefinition> result = CustomNPCDefinition.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
                if (result.error().isPresent()) {
                    Confluence.LOGGER.error("Cannot load custom NPC {}: {}", entry.getKey(), result.error().get().message());
                    continue;
                }
                loaded.put(entry.getKey(), result.result().orElseThrow());
            } catch (RuntimeException e) {
                // e.g. an offer with an empty item stack throws while the codec builds the record
                Confluence.LOGGER.error("Cannot load custom NPC {}", entry.getKey(), e);
            }
        }
        definitions = Map.copyOf(loaded);
    }

    public static @Nullable CustomNPCDefinition get(ResourceLocation id) {
        return definitions.get(id);
    }

    public static Map<ResourceLocation, CustomNPCDefinition> all() {
        return definitions;
    }

    public static Map<ResourceLocation, CustomNPCDefinition.View> views() {
        Map<ResourceLocation, CustomNPCDefinition.View> views = new HashMap<>();
        definitions.forEach((id, definition) -> views.put(id, definition.view()));
        return views;
    }
}
