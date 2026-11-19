package org.confluence.mod.client.handler;

import net.minecraft.resources.ResourceLocation;
import org.confluence.mod.common.entity.npc.trade.NPCTradeOffer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/// The NPC shop tables last sent by the server. Used only by the recipe viewer.
public final class ClientNPCShops {
    private static Map<ResourceLocation, List<NPCTradeOffer>> shops = Map.of();
    private static @Nullable Runnable listener;

    private ClientNPCShops() {}

    public static Map<ResourceLocation, List<NPCTradeOffer>> get() {
        return shops;
    }

    public static void update(Map<ResourceLocation, List<NPCTradeOffer>> value) {
        shops = Map.copyOf(value);
        if (listener != null) listener.run();
    }

    public static void setListener(@Nullable Runnable value) {
        listener = value;
    }
}
