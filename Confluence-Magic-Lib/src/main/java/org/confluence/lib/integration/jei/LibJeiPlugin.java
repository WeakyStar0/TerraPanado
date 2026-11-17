package org.confluence.lib.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.confluence.lib.ConfluenceMagicLib;
import org.confluence.lib.common.item.GroupItem;

@JeiPlugin
public class LibJeiPlugin implements IModPlugin {
    public static final ResourceLocation UID = ConfluenceMagicLib.asResource("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(GroupItem.getInstance(), new ISubtypeInterpreter<>() {
            @Override
            public Object getSubtypeData(ItemStack ingredient, UidContext context) {
                return ingredient.getOrDefault(ConfluenceMagicLib.GROUP_STACKS, GroupItem.Stacks.EMPTY);
            }

            @Override
            public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
                return getSubtypeData(ingredient, context).toString();
            }
        });
    }

    /// 分组物品只是创造模式标签页里的折叠界面，放进 JEI 里点击会尝试发给服务器，所以从 JEI 隐藏。
    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        var manager = jeiRuntime.getIngredientManager();
        var groups = manager.getAllItemStacks().stream().filter(stack -> stack.is(GroupItem.getInstance())).toList();
        if (!groups.isEmpty()) {
            manager.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, groups);
        }
    }
}
