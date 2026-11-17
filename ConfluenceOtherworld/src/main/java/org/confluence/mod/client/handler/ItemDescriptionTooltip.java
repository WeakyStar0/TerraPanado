package org.confluence.mod.client.handler;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/// Shows the description of an item (`<descriptionId>.desc` or `.desc.0`, `.desc.1`, ...) while Shift is held.
public final class ItemDescriptionTooltip {
    private ItemDescriptionTooltip() {}

    public static void add(ItemStack stack, List<Component> tooltip) {
        String base = stack.getDescriptionId() + ".desc";
        boolean single = I18n.exists(base);
        if (!single && !I18n.exists(base + ".0")) return;
        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.confluence.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        if (single) {
            tooltip.add(Component.translatable(base).withStyle(ChatFormatting.GRAY));
            return;
        }
        for (int i = 0; I18n.exists(base + "." + i); i++) {
            tooltip.add(Component.translatable(base + "." + i).withStyle(ChatFormatting.GRAY));
        }
    }
}
