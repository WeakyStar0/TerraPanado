package org.confluence.mod.integration.jei.category;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import org.confluence.mod.Confluence;
import org.confluence.mod.common.component.ValueComponent;
import org.confluence.mod.common.entity.npc.trade.NPCTradeOffer;
import org.confluence.mod.util.ClientUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/// One page per shop offer: what an NPC sells, what it costs and when it is available.
/// Offers come from [org.confluence.mod.client.handler.ClientNPCShops], which the server fills.
public class NPCShopCategory implements IRecipeCategory<NPCShopCategory.Offer> {
    public static final RecipeType<Offer> TYPE = RecipeType.create(Confluence.MODID, "npc_shop", Offer.class);
    private static final int WIDTH = 150;
    private static final int HEIGHT = 76;
    private final IDrawable icon;

    public NPCShopCategory(IJeiHelpers jeiHelpers) {
        this.icon = jeiHelpers.getGuiHelper().createDrawableItemStack(Items.EMERALD.getDefaultInstance());
    }

    @Override
    public RecipeType<Offer> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("title.confluence.npc_shop");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Offer recipe, IFocusGroup focuses) {
        builder.addOutputSlot(2, 2).setStandardSlotBackground().addItemStack(recipe.offer().stack());
        List<ItemStack> costs = recipe.offer().costs();
        for (int i = 0; i < costs.size(); i++) {
            builder.addInputSlot(28 + i * 18, 2).setStandardSlotBackground().addItemStack(costs.get(i));
        }
        ItemStack egg = recipe.egg();
        if (!egg.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.CATALYST, WIDTH - 20, 2).setStandardSlotBackground().addItemStack(egg);
        }
    }

    @Override
    public void draw(Offer recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        int y = 24;
        y = drawWrapped(guiGraphics, font, Component.translatable("jei.confluence.npc_shop.sold_by", recipe.npc().getDescription()).withStyle(ChatFormatting.DARK_GREEN), y);
        if (recipe.offer().costs().isEmpty()) {
            long price = ValueComponent.getValueLong(recipe.offer().stack(), 0) * 5;
            if (price > 0) {
                y = drawWrapped(guiGraphics, font, Component.translatable("jei.confluence.npc_shop.price", ClientUtils.formatPrice(price)).withStyle(ChatFormatting.BLACK), y);
                y = drawWrapped(guiGraphics, font, Component.translatable("jei.confluence.npc_shop.price_mood").withStyle(ChatFormatting.GRAY), y);
            }
        }
        Component condition = recipe.offer().condition().describe();
        if (!condition.getString().isEmpty()) {
            y = drawWrapped(guiGraphics, font, Component.translatable("jei.confluence.npc_shop.requires", condition).withStyle(ChatFormatting.DARK_RED), y);
        }
        if (recipe.randomStock()) {
            drawWrapped(guiGraphics, font, Component.translatable("jei.confluence.npc_shop.random_stock").withStyle(ChatFormatting.GRAY), y);
        }
    }

    private static int drawWrapped(GuiGraphics guiGraphics, Font font, Component text, int y) {
        for (FormattedCharSequence line : font.split(text, WIDTH - 2)) {
            guiGraphics.drawString(font, line, 1, y, -1, false);
            y += font.lineHeight + 1;
        }
        return y + 1;
    }

    @Override
    public ResourceLocation getRegistryName(Offer recipe) {
        return Confluence.asResource("npc_shop/" + recipe.id());
    }

    /// Every offer of every shop that has one, as received from the server.
    public static List<Offer> collectAll(Map<ResourceLocation, List<NPCTradeOffer>> shops) {
        List<Offer> offers = new ArrayList<>();
        shops.forEach((npcId, list) -> {
            EntityType<?> npc = BuiltInRegistries.ENTITY_TYPE.get(npcId);
            ItemStack egg = SpawnEggItem.byId(npc) == null ? ItemStack.EMPTY : SpawnEggItem.byId(npc).getDefaultInstance();
            boolean randomStock = npcId.getPath().equals("traveling_merchant");
            for (int i = 0; i < list.size(); i++) {
                offers.add(new Offer(npcId.getPath() + "/" + i, npc, list.get(i), egg, randomStock));
            }
        });
        return offers;
    }

    public record Offer(String id, EntityType<?> npc, NPCTradeOffer offer, ItemStack egg, boolean randomStock) {}
}
