package tfar.lockon;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(LockOn.MODID)
public class LockOn {
    // Directly reference a log4j logger.

    public static final String MODID = "lockon";

    public LockOn() {
        if (FMLEnvironment.dist == Dist.CLIENT)
        FMLJavaModLoadingContext.get().getModEventBus().addListener(LockOnHandler::client);
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
