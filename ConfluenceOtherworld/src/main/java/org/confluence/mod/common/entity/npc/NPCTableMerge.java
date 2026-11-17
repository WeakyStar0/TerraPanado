package org.confluence.mod.common.entity.npc;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EntityType;
import org.confluence.mod.Confluence;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/// Loads `data/<namespace>/<path>` from every namespace and every datapack, lowest priority first,
/// and merges the per-entity entries. A datapack file only has to list the NPCs it changes.
/// A file that fails to decode is logged and skipped; the other files still apply.
public final class NPCTableMerge {
    private NPCTableMerge() {}

    public static <T> Map<EntityType<?>, T> load(ResourceManager manager, String path, Codec<Map<EntityType<?>, T>> codec) {
        Map<EntityType<?>, T> merged = new HashMap<>();
        for (String namespace : manager.getNamespaces()) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path);
            for (Resource resource : manager.getResourceStack(id)) {
                try (Reader reader = resource.openAsReader()) {
                    JsonElement json = JsonParser.parseReader(reader);
                    DataResult<Map<EntityType<?>, T>> decoded = codec.parse(JsonOps.INSTANCE, json);
                    if (decoded.error().isPresent()) {
                        Confluence.LOGGER.error("Cannot load {} from pack '{}': {}", id, resource.sourcePackId(), decoded.error().get().message());
                        continue;
                    }
                    merged.putAll(decoded.result().orElseThrow());
                } catch (Exception e) {
                    Confluence.LOGGER.error("Cannot read {} from pack '{}'", id, resource.sourcePackId(), e);
                }
            }
        }
        return merged;
    }
}
