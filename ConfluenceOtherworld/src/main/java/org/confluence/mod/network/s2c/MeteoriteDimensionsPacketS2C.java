package org.confluence.mod.network.s2c;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import org.confluence.lib.network.IPacketS2C;
import org.confluence.mod.Confluence;
import org.confluence.mod.client.handler.MeteorLandingHandler;
import org.confluence.mod.common.ModCommonConfigs;

import java.util.ArrayList;
import java.util.List;

/// 把服务端配置的陨石降落维度同步给客户端，只用于物品提示。
public record MeteoriteDimensionsPacketS2C(List<ResourceLocation> dimensions) implements IPacketS2C {
    public static final Type<MeteoriteDimensionsPacketS2C> TYPE = Confluence.createType("meteorite_dimensions");
    public static final StreamCodec<ByteBuf, MeteoriteDimensionsPacketS2C> STREAM_CODEC = ResourceLocation.STREAM_CODEC
            .apply(ByteBufCodecs.list())
            .map(MeteoriteDimensionsPacketS2C::new, MeteoriteDimensionsPacketS2C::dimensions);

    @Override
    public Type<MeteoriteDimensionsPacketS2C> type() {
        return TYPE;
    }

    @Override
    public void work(Player player) {
        MeteorLandingHandler.setLandingDimensions(dimensions);
    }

    public static void sendToClient(ServerPlayer player) {
        List<ResourceLocation> dimensions = new ArrayList<>();
        for (String id : ModCommonConfigs.METEORITE_DIMENSIONS.get()) {
            ResourceLocation location = ResourceLocation.tryParse(id);
            if (location != null) dimensions.add(location);
        }
        PacketDistributor.sendToPlayer(player, new MeteoriteDimensionsPacketS2C(dimensions));
    }
}
