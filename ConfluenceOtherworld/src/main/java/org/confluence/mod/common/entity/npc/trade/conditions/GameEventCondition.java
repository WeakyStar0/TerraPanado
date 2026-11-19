package org.confluence.mod.common.entity.npc.trade.conditions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.confluence.mod.common.entity.npc.BaseNPC;
import org.confluence.mod.common.entity.npc.trade.TradeCondition;
import org.confluence.mod.common.gameevent.GameEvent;
import org.confluence.mod.common.gameevent.GameEventSystem;
import org.confluence.mod.common.init.ModTradeConditions;

public record GameEventCondition(ResourceKey<? extends GameEvent> event) implements TradeCondition {
    public static final MapCodec<GameEventCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            GameEvent.KEY_CODEC.fieldOf("event").forGetter(GameEventCondition::event)
    ).apply(instance, GameEventCondition::new));

    @Override
    public boolean test(ServerPlayer player, BaseNPC npc) {
        return GameEventSystem.INSTANCE.isEventStarted(event);
    }

    @Override
    public MapCodec<? extends TradeCondition> codec() {
        return ModTradeConditions.GAME_EVENT.get();
    }

    @Override
    public Component describe() {
        String path = event.location().getPath();
        return Component.translatable("trade_condition.confluence.game_event.named", Component.translatableWithFallback("bestiary.confluence.filter." + path + ".title", titleCase(path)));
    }

    private static String titleCase(String path) {
        StringBuilder out = new StringBuilder();
        for (String word : path.split("_")) {
            if (word.isEmpty()) continue;
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }
}
