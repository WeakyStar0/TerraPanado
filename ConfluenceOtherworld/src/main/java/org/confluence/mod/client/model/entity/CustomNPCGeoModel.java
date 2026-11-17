package org.confluence.mod.client.model.entity;

import net.minecraft.resources.ResourceLocation;
import org.confluence.mod.client.handler.ClientCustomNPCs;
import org.confluence.mod.common.entity.npc.custom.CustomNPC;
import org.confluence.mod.common.entity.npc.custom.CustomNPCDefinition;

/// Model for datapack NPCs: geometry and animation come from the definition's `base_model`
/// (an existing NPC id such as `confluence:guide`), and the texture can be replaced with `texture`.
public final class CustomNPCGeoModel extends NPCHumanoidGeoModel<CustomNPC> {
    private static final ResourceLocation FALLBACK = CustomNPCDefinition.DEFAULT_BASE_MODEL;

    public CustomNPCGeoModel() {
        super(FALLBACK.withPrefix("npc/"));
    }

    private static ResourceLocation base(CustomNPC npc) {
        CustomNPCDefinition.View view = npc == null ? null : ClientCustomNPCs.get(npc.getNpcId());
        return (view == null ? FALLBACK : view.baseModel()).withPrefix("npc/");
    }

    @Override
    public ResourceLocation getModelResource(CustomNPC npc) {
        return base(npc).withPrefix("geo/entity/").withSuffix(".geo.json");
    }

    @Override
    public ResourceLocation getAnimationResource(CustomNPC npc) {
        return base(npc).withPrefix("animations/entity/").withSuffix(".animation.json");
    }

    @Override
    public ResourceLocation getTextureResource(CustomNPC npc) {
        CustomNPCDefinition.View view = npc == null ? null : ClientCustomNPCs.get(npc.getNpcId());
        if (view != null && view.texture().isPresent()) return view.texture().get();
        return base(npc).withPrefix("textures/entity/").withSuffix(".png");
    }
}
