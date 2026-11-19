package org.confluence.mod.common.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.phys.Vec3;
import org.confluence.lib.util.LibUtils;
import org.confluence.mod.common.init.ModLootTables;
import org.confluence.mod.common.init.ModStructures;
import org.confluence.mod.common.init.ModTags;
import org.confluence.mod.util.DateUtils;
import org.confluence.mod.util.ModUtils;
import org.confluence.mod.util.OverworldUtils;

import java.util.Comparator;
import java.util.Set;

/// A yes/no question about the world at the loot origin. Used by pot loot, which depends on depth, Hardmode,
/// difficulty and the nearest player. Tests that read players use the player nearest to the origin.
public record WorldStateLootItemCondition(Check check) implements LootItemCondition {
    public static final MapCodec<WorldStateLootItemCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Check.CODEC.fieldOf("check").forGetter(WorldStateLootItemCondition::check)
    ).apply(instance, WorldStateLootItemCondition::new));

    public static WorldStateLootItemCondition of(Check check) {
        return new WorldStateLootItemCondition(check);
    }

    public static LootItemCondition.Builder builder(Check check) {
        return () -> new WorldStateLootItemCondition(check);
    }

    @Override
    public LootItemConditionType getType() {
        return ModLootTables.ItemConditions.WORLD_STATE.get();
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.ORIGIN);
    }

    @Override
    public boolean test(LootContext context) {
        ServerLevel level = context.getLevel();
        Vec3 origin = context.getParam(LootContextParams.ORIGIN);
        return switch (check) {
            case HARDMODE -> ModUtils.isHardmode();
            case EXPERT -> LibUtils.isAtLeastExpert(level);
            case MULTIPLAYER -> level.players().size() > 1;
            case NEAREST_PLAYER_HURT -> {
                Player player = nearestPlayer(level, origin);
                yield player != null && player.getHealth() < player.getMaxHealth();
            }
            case NEAREST_PLAYER_LOW_ON_TORCHES -> {
                Player player = nearestPlayer(level, origin);
                yield player != null && player.getInventory().hasAnyMatching(stack -> stack.getCount() < 20 && stack.is(ModTags.Items.TORCH));
            }
            case IN_OVERWORLD -> level.dimension() == OverworldUtils.dimension();
            case IN_UNDERWORLD -> OverworldUtils.inUnderworld(level.dimension(), origin.y);
            case IN_CAVE_LAYER -> !OverworldUtils.inUnderworld(level.dimension(), origin.y) && OverworldUtils.inCave(origin.y);
            case IN_UNDERGROUND_LAYER -> !OverworldUtils.inUnderworld(level.dimension(), origin.y) && !OverworldUtils.inCave(origin.y)
                    && OverworldUtils.inUnderground(origin.y);
            case IN_SURFACE_LAYER -> !OverworldUtils.inUnderworld(level.dimension(), origin.y) && !OverworldUtils.inCave(origin.y)
                    && !OverworldUtils.inUnderground(origin.y) && OverworldUtils.inSurface(origin.y);
            case IN_DUNGEON -> inDungeon(level, BlockPos.containing(origin));
            case IN_WATER -> level.getFluidState(BlockPos.containing(origin)).is(net.minecraft.tags.FluidTags.WATER);
            case ENEMY_DROPS_MONEY -> org.confluence.mod.common.ModCommonConfigs.ENEMY_DROPS_MONEY.get();
            case HALLOWEEN -> DateUtils.isHalloween(DateUtils.getCalendar());
            case CHRISTMAS -> DateUtils.isChristmas(DateUtils.getCalendar());
        };
    }

    private static Player nearestPlayer(ServerLevel level, Vec3 origin) {
        return level.players().stream().min(Comparator.comparingDouble(player -> player.distanceToSqr(origin))).orElse(null);
    }

    private static boolean inDungeon(ServerLevel level, BlockPos pos) {
        Structure structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ModStructures.Keys.DUNGEON);
        if (structure == null) return false;
        int chunkX = SectionPos.blockToSectionCoord(pos.getX());
        int chunkZ = SectionPos.blockToSectionCoord(pos.getZ());
        LongSet structureRefs = level.getChunk(chunkX, chunkZ, ChunkStatus.STRUCTURE_REFERENCES).getReferencesForStructure(structure);
        for (long i : structureRefs) {
            SectionPos sectionPos = SectionPos.of(new ChunkPos(i), level.getMinSection());
            StructureStart start = level.structureManager().getStartForStructure(sectionPos, structure, level.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_STARTS));
            if (start != null && start.isValid() && start.getBoundingBox().isInside(pos)) return true;
        }
        return false;
    }

    public enum Check implements StringRepresentable {
        HARDMODE("hardmode"),
        EXPERT("expert"),
        MULTIPLAYER("multiplayer"),
        NEAREST_PLAYER_HURT("nearest_player_hurt"),
        NEAREST_PLAYER_LOW_ON_TORCHES("nearest_player_low_on_torches"),
        IN_OVERWORLD("in_overworld"),
        IN_UNDERWORLD("in_underworld"),
        IN_CAVE_LAYER("in_cave_layer"),
        IN_UNDERGROUND_LAYER("in_underground_layer"),
        IN_SURFACE_LAYER("in_surface_layer"),
        IN_DUNGEON("in_dungeon"),
        IN_WATER("in_water"),
        ENEMY_DROPS_MONEY("enemy_drops_money"),
        HALLOWEEN("halloween"),
        CHRISTMAS("christmas");

        public static final com.mojang.serialization.Codec<Check> CODEC = StringRepresentable.fromEnum(Check::values);
        private final String name;

        Check(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
