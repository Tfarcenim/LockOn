package tfar.lockon;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(LockOn.MODID)
public class LockOn {
    // Directly reference a log4j logger.

    public static final String MODID = "lockon";

    public LockOn() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.CLIENT_SPEC);

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        if (FMLEnvironment.dist.isClient()) {
            bus.addListener(LockOnHandler::client);
            bus.addListener(LockOnHandler::keyBind);
            bus.addListener(this::setup);
        }
    }

    void setup(FMLClientSetupEvent event) {
    }

    public static class ClientConfig {

        public static ClientConfig CLIENT;
        public static ForgeConfigSpec CLIENT_SPEC;

        static {
            final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
            CLIENT_SPEC = specPair.getRight();
            CLIENT = specPair.getLeft();
        }

        public static ForgeConfigSpec.BooleanValue renderIcons;
        public static ForgeConfigSpec.ConfigValue<? extends String> color;
        public static ForgeConfigSpec.DoubleValue width;

        public final ForgeConfigSpec.IntValue range;
        public final ForgeConfigSpec.DoubleValue height;

        public final ConfigHelper.ConfigObject<List<EntityType<?>>> allowed;
        public ClientConfig(ForgeConfigSpec.Builder builder) {
            builder.push("client");
            renderIcons = builder
                    .comment("Render the lock on icon around the targeted enemy")
                    .define("render_icon", true);
            color = builder
                    .comment("Color of lock on, #AARRGGBB format")
                    .define("color","#FFFFFF00");
            range = builder
                    .comment("Range of lock on")
                    .defineInRange("range",16,0,128);
            width = builder
                    .comment("Width of triangle")
                    .defineInRange("width",1,0,10d);
            height = builder
                    .comment("Height of triangle")
                    .defineInRange("height",.25f,0,10d);
            allowed = ConfigHelper.defineObject(builder.comment("Targetable entity types"),"allowed", BuiltInRegistries.ENTITY_TYPE.byNameCodec().listOf(), defaults());
            builder.pop();
        }

        static List<EntityType<?>> defaults() {
            List<EntityType<?>> types = new ArrayList<>();
            for (EntityType<?> type :BuiltInRegistries.ENTITY_TYPE) {
                if (type.getCategory() == MobCategory.MONSTER) {
                    types.add(type);
                }
            }

            types.add(EntityType.PLAYER);
            return types;
        }
    }

    /*public static Optional<Entity> rayTrace(PlayerEntity player) {
        double distance = 64;
        Vector3d vector3d = player.getEyePosition(1);
        Vector3d vector3d1 = player.getLook(1.0F);
        Vector3d vector3d2 = vector3d.add(vector3d1.x * distance, vector3d1.y * distance, vector3d1.z * distance);
        Entity pointedEntity = null;
        List<Entity> list = player.world.getEntitiesInAABBexcluding(player, player.getBoundingBox()
                        .expand(vector3d1.x * distance, vector3d1.y * distance, vector3d1.z * distance).grow(1.0D, 1.0D, 1.0D),
                EntityPredicates.NOT_SPECTATING);
        double d2 = distance;

        for (int j = 0; j < list.size(); ++j) {
            Entity entity1 = list.get(j);
            AxisAlignedBB axisalignedbb = entity1.getBoundingBox().grow(entity1.getCollisionBorderSize());
            EntityRayTraceResult entityraytraceresult = ProjectileHelper.rayTraceEntities(player.world, player, vector3d, vector3d2, axisalignedbb,
                    (entity) -> !entity.isSpectator() && entity.canBeCollidedWith());
            if (axisalignedbb.contains(vector3d)) {
                if (d2 >= 0.0D) {
                    pointedEntity = entity1;
                    d2 = 0.0D;
                }
            } else if (entityraytraceresult != null) {
                double d3 = vector3d.distanceTo(entityraytraceresult.getHitVec());

                if (d3 < d2 || d2 == 0.0D) {
                    if (entity1.getLowestRidingEntity() == player.getLowestRidingEntity() && !entity1.canRiderInteract()) {
                        if (d2 == 0.0D) {
                            pointedEntity = entity1;
                        }
                    } else {
                        pointedEntity = entity1;
                        d2 = d3;
                    }
                }
            }
        }
        return Optional.ofNullable(pointedEntity);
    }*/
}
