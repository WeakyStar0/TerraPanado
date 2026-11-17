package org.confluence.mod.common.entity.npc.dialog;

import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import org.confluence.mod.common.entity.npc.NPCTableMerge;
import org.jetbrains.annotations.Nullable;

import java.util.Map;


public class NPCDialogLoader extends SimpleJsonResourceReloadListener {
    private static NPCDialogLoader INSTANCE;
    private Map<EntityType<?>, NPCDialog> dialogs = ImmutableMap.of();

    public NPCDialogLoader() {
        super(new GsonBuilder().create(), "npc");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profiler) {
        this.dialogs = ImmutableMap.copyOf(NPCTableMerge.load(manager, "npc/dialogs.json", NPCDialog.MAP_CODEC));
    }

    public Map<EntityType<?>, NPCDialog> getDialogs() {
        return dialogs;
    }

    @Nullable
    public NPCDialog getDialog(EntityType<?> type) {
        return dialogs.get(type);
    }

    @Nullable
    public String getRandomDialogKey(RandomSource random, EntityType<?> type) {
        NPCDialog dialog = getDialog(type);
        return dialog == null || dialog.isEmpty() ? null : dialog.randomKey(random);
    }

    public static NPCDialogLoader getInstance() {
        if (INSTANCE == null) INSTANCE = new NPCDialogLoader();
        return INSTANCE;
    }
}
