package org.confluence.mod.common.init;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import org.confluence.mod.Confluence;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

/// 给原本不会损坏的装备补上耐久：护甲、枪矛、链锤、鞭子、悠悠球和少数特殊弓。
///
/// 通过默认组件统一修改，不必改动几百处注册代码。数值按泰拉瑞亚的进度和伤害排序，
/// 同阶段的物品参考已有的剑、镐和弓：铜 250，铁 286，金 1600，钴 1000～2200，叶绿 3600，星尘 5000。
/// 护甲耐久 = 部位系数 × 套装倍率，部位系数沿用原版（头 11、胸 16、腿 15、脚 13）。
@EventBusSubscriber(modid = Confluence.MODID)
public final class ItemDurability {
    private ItemDurability() {
    }

    /// 这些物品耐久为无限，不论它们的材质给了多少耐久。
    private static final java.util.Set<String> UNBREAKABLE = java.util.Set.of("zenith", "true_copper_shortsword");

    /// 玩家 → 物品 → 上次扣耐久的游戏刻。只在服务端线程使用。
    private static final HashMap<UUID, Object2LongOpenHashMap<Item>> LAST_CHARGE = new HashMap<>();

    /// 按"持有者的一次行为"扣耐久，而不是按弹幕或命中次数扣。
    ///
    /// 悠悠球背包的副球和平衡锤、链锤旋转时的连续命中、武器命中后额外生成的弹幕，
    /// 都不应该让耐久成倍消失。同一个玩家的同一种武器，在 `minInterval` 刻内最多扣 1 点，
    /// 所以持续使用的最大磨损只取决于时间，与弹幕数量和命中频率无关。
    public static void charge(LivingEntity owner, ItemStack stack, EquipmentSlot slot, int minInterval) {
        if (!stack.isDamageableItem()) return;
        long now = owner.level().getGameTime();
        Object2LongOpenHashMap<Item> last = LAST_CHARGE.computeIfAbsent(owner.getUUID(), id -> {
            Object2LongOpenHashMap<Item> map = new Object2LongOpenHashMap<>();
            map.defaultReturnValue(Long.MIN_VALUE / 2);
            return map;
        });
        if (now - last.getLong(stack.getItem()) < minInterval) return;
        last.put(stack.getItem(), now);
        stack.hurtAndBreak(1, owner, slot);
    }

    private static final String TAG_PREFIX = "confluence_wear:";
    private static final String TAG_DONE = "confluence_wear_done";
    private static final ThreadLocal<ItemStack> SCOPE = new ThreadLocal<>();

    /// 把弹幕和发射它的武器绑定：弹幕第一次造成伤害时，武器扣 1 点耐久，之后不再扣。
    public static void bind(Entity projectile, ItemStack weapon) {
        if (weapon.isEmpty() || !weapon.isDamageableItem()) return;
        projectile.addTag(TAG_PREFIX + BuiltInRegistries.ITEM.getKey(weapon.getItem()).getPath());
    }

    /// 在作用域内进入世界的弹幕自动绑定到这把武器，用于命中特效和刺击这类一次生成多个弹幕的地方。
    public static Scope scope(ItemStack weapon) {
        ItemStack previous = SCOPE.get();
        SCOPE.set(weapon);
        return new Scope(previous);
    }

    public record Scope(ItemStack previous) implements AutoCloseable {
        @Override
        public void close() {
            if (previous == null) SCOPE.remove();
            else SCOPE.set(previous);
        }
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        ItemStack weapon = SCOPE.get();
        Entity entity = event.getEntity();
        if (weapon == null || event.getLevel().isClientSide()) return;
        if (entity instanceof LivingEntity || entity instanceof ItemEntity || entity instanceof ExperienceOrb) return;
        bind(entity, weapon);
    }

    /// 弹幕真正伤害到目标后才扣耐久，而且每个弹幕最多扣 1 点，穿透多个目标也一样。
    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
        DamageSource source = event.getSource();
        Entity direct = source.getDirectEntity();
        if (direct == null || direct == source.getEntity() || !(source.getEntity() instanceof LivingEntity owner)) return;
        if (owner.level().isClientSide() || event.getNewDamage() <= 0.0F) return;
        String weaponId = null;
        for (String tag : direct.getTags()) {
            if (tag.equals(TAG_DONE)) return;
            if (tag.startsWith(TAG_PREFIX)) weaponId = tag.substring(TAG_PREFIX.length());
        }
        if (weaponId == null) return;
        direct.addTag(TAG_DONE);
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND}) {
            ItemStack stack = owner.getItemBySlot(slot);
            if (isWeapon(stack, weaponId)) {
                stack.hurtAndBreak(1, owner, slot);
                return;
            }
        }
        if (owner instanceof Player player && player.level() instanceof ServerLevel level) {
            for (ItemStack stack : player.getInventory().items) {
                if (isWeapon(stack, weaponId)) {
                    stack.hurtAndBreak(1, level, player, item -> {});
                    return;
                }
            }
        }
    }

    private static boolean isWeapon(ItemStack stack, String weaponId) {
        return !stack.isEmpty() && stack.isDamageableItem() && BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().equals(weaponId);
    }

    /// 按物品所在的手选择槽位，给只拿到 ItemStack 的调用点使用。
    public static void chargeHeld(LivingEntity owner, ItemStack stack, int minInterval) {
        charge(owner, stack, owner.getMainHandItem() == stack ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND, minInterval);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_CHARGE.remove(event.getEntity().getUUID());
    }

    /// 悠悠球和链锤用 charge() 限速，长枪、骑枪、鞭子、弓每次攻击只扣一次，所以耐久都按同阶段的剑来定。
    private static final Map<String, Integer> WEAPONS = Map.ofEntries(
            // 长枪：每次命中扣 1 点，按剑的耐久对应阶段
            Map.entry("spear", 250), Map.entry("storm_spear", 450), Map.entry("the_rotted_fork", 520),
            Map.entry("streamstrike_halberd", 700), Map.entry("dark_lance", 900),
            Map.entry("cobalt_naginata", 1000), Map.entry("palladium_pike", 1160), Map.entry("mythril_halberd", 1200),
            Map.entry("ghastly_glaive", 1300), Map.entry("orichalcum_halberd", 1360),
            Map.entry("titanium_trident", 1440), Map.entry("adamantite_glaive", 1440),
            Map.entry("chlorophyte_partisan", 1700), Map.entry("mushroom_spear", 1800),
            Map.entry("gungnir", 2000), Map.entry("north_pole", 2500),
            // 骑枪
            Map.entry("jousting_lance", 1000), Map.entry("hallowed_jousting_lance", 1500), Map.entry("shadow_jousting_lance", 2000),
            // 链锤：每秒最多扣 1 点
            Map.entry("mace", 300), Map.entry("flaming_mace", 350), Map.entry("chain_knife", 400),
            Map.entry("wind_anchor", 500), Map.entry("guardian_flail", 550), Map.entry("ancient_guardian_flail", 650),
            Map.entry("ball_o_hurt", 700), Map.entry("the_meatball", 760), Map.entry("blue_moon", 1000),
            Map.entry("sunfury", 1200), Map.entry("ko_cannon", 1300), Map.entry("chain_guillotines", 1600),
            Map.entry("anchor", 1700), Map.entry("golem_fist", 1900), Map.entry("dao_of_pow", 2000),
            Map.entry("drippler_crippler", 2200), Map.entry("flower_power", 2400), Map.entry("flairon", 2500),
            // 鞭子：每次挥动扣 1 点
            Map.entry("leather_whip", 250), Map.entry("slub_whip", 250), Map.entry("amethyst_whip", 300),
            Map.entry("topaz_whip", 340), Map.entry("sapphire_whip", 380), Map.entry("jade_whip", 420),
            Map.entry("ruby_whip", 460), Map.entry("amber_whip", 500), Map.entry("diamond_whip", 540),
            Map.entry("swamp_whip", 560), Map.entry("snapthorn", 600), Map.entry("spinal_tap", 700),
            Map.entry("firecracker", 1500),
            // 悠悠球：主球每秒最多扣 1 点（见 charge），耐久按同阶段的剑来定
            Map.entry("wooden_yoyo", 300), Map.entry("rally", 400), Map.entry("malaise", 450), Map.entry("artery", 500),
            Map.entry("amazon", 550), Map.entry("code_1", 650), Map.entry("hive_five", 750), Map.entry("cascade", 850),
            Map.entry("valor", 900), Map.entry("chik", 1200), Map.entry("format_c", 1250), Map.entry("hel_fire", 1350),
            Map.entry("amarok", 1450), Map.entry("gradient", 1600), Map.entry("code_2", 1650), Map.entry("yelets", 1800),
            Map.entry("the_eye_of_cthulhu", 2400), Map.entry("kraken", 2500), Map.entry("terrarian", 3500),
            // 特殊弓：已有的木弓 384 到铂金弓 1536、连弩 1600～2300 之间排序
            Map.entry("hunting_bow", 800), Map.entry("tendon_bow", 900), Map.entry("demon_bow", 900),
            Map.entry("fossil_bow", 1000), Map.entry("scarebow", 1100), Map.entry("the_bees_knees", 1300),
            Map.entry("molten_fury", 1650), Map.entry("hellwing_bow", 1900), Map.entry("blood_rain_bow", 2100),
            Map.entry("daedalus_storm_bow", 2300),
            // 枪：每颗子弹首次造成伤害时扣 1 点，一颗子弹打穿多个目标也只扣 1 点
            Map.entry("blowgun", 450), Map.entry("flintlock_pistol", 500), Map.entry("snowball_cannon", 640),
            Map.entry("bee_gun", 800), Map.entry("minishark", 900), Map.entry("boomstick", 900), Map.entry("the_undertaker", 900),
            Map.entry("musket", 1100), Map.entry("space_gun", 1100), Map.entry("star_cannon", 1200), Map.entry("hand_gun", 1300),
            Map.entry("phoenix_blaster", 1650), Map.entry("shotgun", 1800), Map.entry("tactical_shotgun", 2100),
            // 法杖：每个弹幕首次造成伤害时扣 1 点；红手套每次放电造成过伤害扣 1 点
            Map.entry("wand_of_sparking", 300), Map.entry("wand_of_frosting", 320), Map.entry("amethyst_staff", 340),
            Map.entry("topaz_staff", 380), Map.entry("sapphire_staff", 420), Map.entry("ruby_staff", 460),
            Map.entry("jade_staff", 480), Map.entry("amber_staff", 520), Map.entry("vilethron", 520), Map.entry("crimson_rod", 520),
            Map.entry("water_bolt", 560), Map.entry("diamond_staff", 600), Map.entry("thunder_zapper", 600),
            Map.entry("book_of_skulls", 700), Map.entry("aqua_scepter", 700), Map.entry("weather_pain", 700),
            Map.entry("demon_scythe", 800), Map.entry("magic_missile", 800), Map.entry("flamelash", 900),
            Map.entry("flower_of_fire", 1000), Map.entry("crystal_vile_shard", 1100), Map.entry("nimbus_rod", 1200),
            Map.entry("golden_shower", 1200), Map.entry("stellar_studies_compilation", 1200), Map.entry("rainbow_rod", 1800), Map.entry("star_spirit", 1800),
            Map.entry("arc_surge", 2000), Map.entry("thunderstrike", 2200),
            // 召唤杖：只在召唤时扣 1 点，随从的攻击不扣
            Map.entry("finch_staff", 300), Map.entry("slime_staff", 300), Map.entry("snow_flinx_staff", 300),
            Map.entry("new_hornet_staff", 380), Map.entry("vampire_frog_staff", 420), Map.entry("imp_staff", 520),
            Map.entry("sculk_wisp_staff", 600), Map.entry("vex_totem", 800), Map.entry("iron_golem_staff", 900),
            Map.entry("spider_staff", 1100), Map.entry("eye_laser_turret_staff", 1200), Map.entry("sanguine_staff", 1300),
            Map.entry("desert_tiger_staff", 1500), Map.entry("ruinous_staff", 1500), Map.entry("deadly_sphere_staff", 1900),
            Map.entry("terraprisma", 2500),
            // 其它
            Map.entry("lucy_the_axe", 600), Map.entry("axe_of_regrowth", 1661),
            Map.entry("cobalt_garden_shears", 1200), Map.entry("palladium_garden_shears", 1300)
    );

    /// 护甲材质 → 套装倍率。键是护甲材质的注册名，多头盔套装的各个头盔材质也在这里。
    private static final Map<String, Integer> ARMOR = Map.ofEntries(
            // 早期功能套装
            Map.entry("angler_armor_materials", 8), Map.entry("raincoat_armor_materials", 8),
            Map.entry("snow_armor_materials", 12), Map.entry("pink_snow_armor_materials", 12),
            Map.entry("pumpkin_armor_materials", 14), Map.entry("mining_armor_materials", 14),
            Map.entry("soul_hood_material", 14), Map.entry("souldancer_hood_material", 14),
            Map.entry("soul_rope_material", 14), Map.entry("souldancer_rope_material", 14),
            // 单件长袍和帽子，按防御力
            Map.entry("topaz_robe_material", 6), Map.entry("sapphire_robe_material", 6), Map.entry("goggles_material", 6),
            Map.entry("flinx_fur_coat_material", 6),
            Map.entry("emerald_robe_material", 8), Map.entry("ruby_robe_material", 8), Map.entry("mystic_robe_material", 8),
            Map.entry("magic_hat_material", 8), Map.entry("green_cap_material", 8),
            Map.entry("amber_robe_material", 10), Map.entry("diamond_robe_material", 10),
            Map.entry("wizard_hat_material", 12), Map.entry("viking_helmet_material", 12),
            // 困难模式前的套装，大致每点套装防御 +1 倍率，对应已有的铁～铂金（10～21）
            Map.entry("evocation_robe_armor_materials", 15), Map.entry("hermit_armormaterials", 15),
            Map.entry("wolf_armor_materials", 17), Map.entry("verdant_robe_armor_materials", 17),
            Map.entry("battle_robe_armor_materials", 17), Map.entry("phantom_armor_materials", 17),
            Map.entry("ninja_armor_materials", 17), Map.entry("cold_crystal_armor_materials", 17),
            Map.entry("hunters_armor_materials", 18), Map.entry("scale_mail_armor_materials", 18),
            Map.entry("white_pumpkin_armor_materials", 19), Map.entry("root_rot_armor_materials", 19),
            Map.entry("black_spot_armor_materials", 19), Map.entry("climbing_armor_materials", 19),
            Map.entry("archers_armor_materials", 19), Map.entry("guards_armor_materials", 19),
            Map.entry("spelunker_armor_materials", 19), Map.entry("bee_armor_materials", 19),
            Map.entry("fossil_armor_materials", 19),
            Map.entry("obsidian_armor_materials", 20),
            Map.entry("entertainers_garb_armor_materials", 21), Map.entry("reinforced_mail_armor_materials", 21),
            Map.entry("mercenary_armor_materials", 21), Map.entry("ember_robe_armor_materials", 21),
            Map.entry("splendid_robe_armor_materials", 21), Map.entry("heim_armor_materials", 21),
            Map.entry("gladiator_armor_materials", 21), Map.entry("meteor_armor_materials", 21),
            Map.entry("jungle_armor_materials", 21), Map.entry("seeker_armor_materials", 21),
            Map.entry("necro_armor_materials", 22), Map.entry("shadow_armor_materials", 22),
            Map.entry("crimson_armor_materials", 22),
            Map.entry("highland_armor_materials", 23), Map.entry("spider_armor_materials", 23),
            Map.entry("troubadour_armor_materials", 24),
            Map.entry("stalwart_armor_materials", 25), Map.entry("renegade_armor_materials", 25),
            Map.entry("molten_armor_materials", 28),
            // 困难模式：按进度排序（钴 < 钯金 < 秘银 < 山铜 < 精金 < 钛金 < 神圣）
            Map.entry("cobalt_armor_materials", 30), Map.entry("cobalt_hat_material", 30), Map.entry("cobalt_mask_material", 30),
            Map.entry("palladium_armor_materials", 31), Map.entry("palladium_headgear_material", 31), Map.entry("palladium_mask_material", 31),
            Map.entry("crystal_assassin_armor_materials", 32),
            Map.entry("mythril_armor_materials", 33), Map.entry("mythril_hood_material", 33), Map.entry("mythril_hat_material", 33),
            Map.entry("orichalcum_armor_materials", 34), Map.entry("orichalcum_headgear_material", 34), Map.entry("orichalcum_mask_material", 34),
            Map.entry("adamantite_armor_materials", 36), Map.entry("adamantite_headgear_material", 36), Map.entry("adamantite_mask_material", 36),
            Map.entry("titanium_armor_materials", 37), Map.entry("titanium_headgear_material", 37), Map.entry("titanium_mask_material", 37),
            Map.entry("hallowed_armor_materials", 40), Map.entry("hallowed_hood_material", 40),
            Map.entry("hallowed_headgear_material", 40), Map.entry("hallowed_mask_material", 40)
    );

    private static int armorSlotFactor(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> 11;
            case CHESTPLATE -> 16;
            case LEGGINGS -> 15;
            case BOOTS -> 13;
            case BODY -> 16;
        };
    }

    private static int uses(Item item) {
        Optional<String> id = Optional.ofNullable(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item))
                .filter(key -> key.getNamespace().equals(Confluence.MODID)).map(key -> key.getPath());
        if (id.isEmpty()) return 0;
        Integer weapon = WEAPONS.get(id.get());
        if (weapon != null) return weapon;
        if (item instanceof ArmorItem armor) {
            Integer multiplier = armor.getMaterial().unwrapKey().map(key -> ARMOR.get(key.location().getPath())).orElse(null);
            if (multiplier != null) return armorSlotFactor(armor.getType()) * multiplier;
        }
        return 0;
    }

    public static void apply(ModifyDefaultComponentsEvent event) {
        for (Item item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
            var key = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
            if (key.getNamespace().equals(Confluence.MODID) && UNBREAKABLE.contains(key.getPath())) {
                event.modify(item, builder -> builder.set(DataComponents.UNBREAKABLE, org.confluence.mod.common.init.item.ModItems.UNBREAKABLE));
                continue;
            }
            int uses = uses(item);
            if (uses <= 0) continue;
            event.modify(item, builder -> builder
                    .set(DataComponents.MAX_DAMAGE, uses)
                    .set(DataComponents.DAMAGE, 0)
                    .set(DataComponents.MAX_STACK_SIZE, 1)
                    .remove(DataComponents.UNBREAKABLE));
        }
    }
}
