package org.confluence.mod.common.entity.npc.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.confluence.mod.common.entity.npc.BaseNPC;
import org.confluence.mod.common.entity.npc.ai.NPCCombatProfile;
import org.confluence.mod.common.entity.npc.chat.ChatLine;
import org.confluence.mod.common.entity.npc.trade.NPCTradeOffer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/// A town NPC whose name, shop, speech, stats and look come from a datapack definition
/// (see {@link CustomNPCDefinition}). One entity type serves every definition; the definition id
/// is stored on the entity.
public class CustomNPC extends BaseNPC {
    private static final EntityDataAccessor<String> DATA_NPC_ID = SynchedEntityData.defineId(CustomNPC.class, EntityDataSerializers.STRING);
    private static final String NBT_KEY = "CustomNpcId";

    public CustomNPC(EntityType<? extends BaseNPC> type, Level level, NPCCombatProfile combatProfile) {
        super(type, level, combatProfile);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_NPC_ID, "");
    }

    public void setNpcId(ResourceLocation id) {
        entityData.set(DATA_NPC_ID, id.toString());
    }

    public @Nullable ResourceLocation getNpcId() {
        return ResourceLocation.tryParse(entityData.get(DATA_NPC_ID));
    }

    /// Server side only: the client does not receive full definitions.
    public @Nullable CustomNPCDefinition definition() {
        ResourceLocation id = getNpcId();
        return id == null ? null : CustomNPCDefinitions.get(id);
    }

    public Item weapon() {
        CustomNPCDefinition definition = definition();
        return definition == null ? Items.AIR : definition.weapon();
    }

    @Override
    public List<NPCTradeOffer> configuredOffers(Map<EntityType<?>, List<NPCTradeOffer>> table) {
        CustomNPCDefinition definition = definition();
        return definition == null ? List.of() : definition.offers();
    }

    @Override
    public List<ChatLine> configuredChat(Map<EntityType<?>, List<ChatLine>> table) {
        CustomNPCDefinition definition = definition();
        return definition == null ? List.of() : definition.chat();
    }

    @Override
    protected @Nullable String pickName() {
        CustomNPCDefinition definition = definition();
        if (definition == null) return null;
        Map<String, Float> names = definition.namesWeights();
        float total = 0.0F;
        for (float weight : names.values()) {
            if (weight > 0.0F) total += weight;
        }
        if (total <= 0.0F) return null;
        float choice = getRandom().nextFloat() * total;
        for (Map.Entry<String, Float> entry : names.entrySet()) {
            if (entry.getValue() <= 0.0F) continue;
            choice -= entry.getValue();
            if (choice < 0.0F) return entry.getKey();
        }
        return names.keySet().iterator().next();
    }

    @Override
    public Component getTypeName() {
        CustomNPCDefinition definition = definition();
        return definition == null ? super.getTypeName() : Component.literal(definition.name());
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount % 20 == 1) applyDefinitionStats();
    }

    /// The registered defaults are applied by {@link BaseNPC}; this lays the definition's health
    /// and damage on top, and keeps doing so after a datapack reload.
    private void applyDefinitionStats() {
        CustomNPCDefinition definition = definition();
        if (definition == null) return;
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null && health.getBaseValue() != definition.health()) {
            boolean full = getHealth() >= getMaxHealth() - 0.001F;
            health.setBaseValue(definition.health());
            setHealth(full ? getMaxHealth() : Math.min(getHealth(), getMaxHealth()));
        }
        var damage = getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null && damage.getBaseValue() != definition.damage()) {
            damage.setBaseValue(definition.damage());
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString(NBT_KEY, entityData.get(DATA_NPC_ID));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        entityData.set(DATA_NPC_ID, compound.getString(NBT_KEY));
    }
}
