package org.confluence.mod.common.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.confluence.lib.common.menu.ToggleSlot;

/// 占位用的隐藏槽位：保留容器下标与存档数据，但不显示、不能放入或取出物品。
public class HiddenSlot extends ToggleSlot {
    public HiddenSlot(Container container, int slot) {
        super(container, slot, -2000, -2000);
        this.isActive = false;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void setEnable(boolean enable) {
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }
}
