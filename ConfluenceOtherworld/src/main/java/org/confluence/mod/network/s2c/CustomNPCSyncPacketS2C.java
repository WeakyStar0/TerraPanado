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
import org.confluence.mod.client.handler.ClientCustomNPCs;
import org.confluence.mod.common.entity.npc.custom.CustomNPCDefinition;
import org.confluence.mod.common.entity.npc.custom.CustomNPCDefinitions;

import java.util.Map;

/// Sends the client-visible part of every custom NPC definition (look, name, dialog, egg icon).
public record CustomNPCSyncPacketS2C(Map<ResourceLocation, CustomNPCDefinition.View> views) implements IPacketS2C {
    public static final Type<CustomNPCSyncPacketS2C> TYPE = Confluence.createType("custom_npc_sync");
    public static final StreamCodec<ByteBuf, CustomNPCSyncPacketS2C> STREAM_CODEC = ByteBufCodecs.fromCodec(CustomNPCDefinition.View.MAP_CODEC)
            .map(CustomNPCSyncPacketS2C::new, CustomNPCSyncPacketS2C::views);

    @Override
    public Type<CustomNPCSyncPacketS2C> type() {return TYPE;}

    @Override
    public void work(Player player) {ClientCustomNPCs.update(views);}

    public static void sendToClient(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new CustomNPCSyncPacketS2C(CustomNPCDefinitions.views()));
    }
}
