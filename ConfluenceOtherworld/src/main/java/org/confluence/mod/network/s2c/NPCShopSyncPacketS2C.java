package org.confluence.mod.network.s2c;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import org.confluence.lib.network.IPacketS2C;
import org.confluence.mod.Confluence;
import org.confluence.mod.client.handler.ClientNPCShops;
import org.confluence.mod.common.entity.npc.trade.NPCTradeList;
import org.confluence.mod.common.entity.npc.trade.NPCTradeOffer;

import java.util.List;
import java.util.Map;

/// Sends the NPC shop tables to the client so the recipe viewer can list them. Shops are otherwise server-only.
public record NPCShopSyncPacketS2C(Map<ResourceLocation, List<NPCTradeOffer>> shops) implements IPacketS2C {
    public static final CustomPacketPayload.Type<NPCShopSyncPacketS2C> TYPE = Confluence.createType("npc_shop_sync");
    public static final StreamCodec<RegistryFriendlyByteBuf, NPCShopSyncPacketS2C> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(
            Codec.unboundedMap(ResourceLocation.CODEC, NPCTradeOffer.CODEC.listOf())
    ).map(NPCShopSyncPacketS2C::new, NPCShopSyncPacketS2C::shops);

    @Override
    public CustomPacketPayload.Type<NPCShopSyncPacketS2C> type() {return TYPE;}

    @Override
    public void work(Player player) {ClientNPCShops.update(shops);}

    public static void sendToClient(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new NPCShopSyncPacketS2C(NPCTradeList.shopsForViewer()));
    }
}
