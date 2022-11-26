package tfar.lockon;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static net.minecraftforge.common.MinecraftForge.EVENT_BUS;
import static tfar.lockon.LockOn.MODID;


public class LockOnHandler {

    public static KeyMapping LOCK_ON;
    public static KeyMapping TAB;

    public static List<LivingEntity> list = new ArrayList<>();

    private static final Minecraft mc = Minecraft.getInstance();

    public static void client(FMLClientSetupEvent e) {
        LOCK_ON = new KeyMapping("key." + MODID + ".lock_on", GLFW.GLFW_KEY_O, "key.categories." + MODID);
        TAB = new KeyMapping("key." + MODID + ".tab", GLFW.GLFW_KEY_TAB, "key.categories." + MODID);
        ClientRegistry.registerKeyBinding(LOCK_ON);
        ClientRegistry.registerKeyBinding(TAB);
        EVENT_BUS.addListener(LockOnHandler::logOff);
        EVENT_BUS.addListener(LockOnHandler::tick);
    }



    public static void renderWorldLast(Entity entity, PoseStack poseStack, MultiBufferSource buffers, Quaternion quaternion) {
        if (targeted == entity && LockOn.ClientConfig.renderIcons.get()) {
            VertexConsumer builder = buffers.getBuffer(ModRenderType.RENDER_TYPE);
            poseStack.pushPose();

            poseStack.translate(0, entity.getBbHeight()/2, 0);

            poseStack.mulPose(quaternion);


            float rotate = (Util.getNanos() /-8_000_000f);

            poseStack.mulPose(Vector3f.ZP.rotationDegrees(rotate));


            float w = (float)(double)LockOn.ClientConfig.width.get();float h = (float)(double)LockOn.ClientConfig.height.get();

            int color = 0xffffff00;

            try {
                color = Integer.decode(LockOn.ClientConfig.color.get());
            } catch (NumberFormatException e) {
                //wut
            }
            RenderSystem.disableCull();
            fillTriangles(builder,poseStack.last().pose(),0,entity.getBbHeight() / 2f, -w/2f, h,entity.getBbHeight() / 2f,0,color);
            poseStack.popPose();
            //     RenderSystem.enableCull();
        }
    }

    private static void tick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.START) {
            while (LOCK_ON.consumeClick()) {
                if (lockedOn) {
                    leaveLockOn();
                } else {
                    attemptEnterLockOn(Minecraft.getInstance().player);
                }
            }

            while (TAB.consumeClick()) {
                tabToNextEnemy(Minecraft.getInstance().player);
            }
            tickLockedOn();
        }
    }

    private static void logOff(ClientPlayerNetworkEvent.LoggedOutEvent e) {
        leaveLockOn();
    }

    public static boolean lockedOn;
    private static Entity targeted;

    public static boolean lockY = true;

    public static boolean handleKeyPress(Player player, double d2, double d3) {
        if (player != null && !mc.isPaused()) {
            if (targeted != null) {
                Vec3 targetPos = targeted.position().add(0,targeted.getEyeHeight(),0);
                Vec3 targetVec = targetPos.subtract(player.position().add(0,player.getEyeHeight(),0)).normalize();
                double targetAngleX = Mth.wrapDegrees(Math.atan2(-targetVec.x, targetVec.z) * 180 / Math.PI);
                double targetAngleY = Math.atan2(targetVec.y , targetVec.horizontalDistance()) * 180 / Math.PI;
                double xRot = Mth.wrapDegrees(player.getXRot());
                double yRot = Mth.wrapDegrees(player.getYRot());
                double toTurnX = Mth.wrapDegrees(yRot - targetAngleX);
                double toTurnY = Mth.wrapDegrees(xRot + targetAngleY);

                player.turn(-toTurnX,-toTurnY);
                return true;
            }
        }
        return false;
    }

    private static void attemptEnterLockOn(Player player) {
        tabToNextEnemy(player);
        if (targeted != null) {
            lockedOn = true;
        }
    }

    private static void tickLockedOn() {
        list.removeIf(livingEntity -> mc.player == null || !livingEntity.isAlive());
        if (targeted != null && !targeted.isAlive()) {
            targeted = null;
            lockedOn = false;
        }
    }

    private static final Predicate<LivingEntity> ENTITY_PREDICATE = entity -> entity.isAlive() && entity.attackable() && entity instanceof Enemy;
    private static final TargetingConditions ENEMY_CONDITION = TargetingConditions.forCombat().range(20.0D).selector(ENTITY_PREDICATE);

    private static int cycle = -1;

    public static Entity findNearby(Player player) {
        List<LivingEntity> entities = player.level
                .getNearbyEntities(LivingEntity.class, ENEMY_CONDITION, player, player.getBoundingBox().inflate(10.0D, 2.0D, 10.0D)).stream().filter(livingEntity -> player.hasLineOfSight(livingEntity)).collect(Collectors.toList());
        if (lockedOn) {
            cycle++;
            for (LivingEntity entity : entities) {
                if (!list.contains(entity)) {
                    list.add(entity);
                    return entity;
                }
            }

           //cycle existing entity
            if (cycle >= list.size()) {
                cycle = 0;
            }
            return list.get(cycle);
        } else {
            if (!entities.isEmpty()) {
                LivingEntity first = entities.get(0);
                list.add(first);
                return entities.get(0);
            } else {
                return null;
            }
        }
    }

    private static void tabToNextEnemy(Player player) {
        targeted = findNearby(player);
    }

    private static void leaveLockOn() {
        targeted = null;
        lockedOn = false;
        list.clear();
    }

    public enum Dir {
        up,down,left,right;
    }

    public static void fillTriangles(VertexConsumer builder, Matrix4f matrix4f, float x, float y,float width, float height,float bbHeight, float z, int aarrggbb) {
        float a = (aarrggbb >> 24 & 0xff) / 255f;
        float r = (aarrggbb >> 16 & 0xff) / 255f;
        float g = (aarrggbb >> 8 & 0xff) / 255f;
        float b = (aarrggbb & 0xff) / 255f;

        fillTriangle(builder, matrix4f,x,y, width, height,bbHeight,z, r, g, b, a,Dir.up);
        fillTriangle(builder, matrix4f,x,-y, width, height,bbHeight,z, r, g, b, a,Dir.down);
        fillTriangle(builder, matrix4f,x,0, width, height,bbHeight,z, r, g, b, a,Dir.left);
        fillTriangle(builder, matrix4f,x,0, width, height,bbHeight,z, r, g, b, a,Dir.right);

    }


    public static void fillTriangle(VertexConsumer builder, Matrix4f matrix4f, float x, float y,float width, float height, float z, int aarrggbb,Dir dir) {
        float a = (aarrggbb >> 24 & 0xff) / 255f;
        float r = (aarrggbb >> 16 & 0xff) / 255f;
        float g = (aarrggbb >> 8 & 0xff) / 255f;
        float b = (aarrggbb & 0xff) / 255f;

        fillTriangle(builder, matrix4f,x,y, width, height,0,z, r, g, b, a,dir);
    }

    public static void fillTriangle(VertexConsumer builder, Matrix4f matrix4f, float x, float y, float width, float height,float bbHeight, float z, float r, float g, float b, float a, Dir dir) {
    //    builder.vertex(matrix4f, u, v, z).color(r, g, b, a).endVertex();

        switch (dir) {
            case up -> {
                builder.vertex(matrix4f, x, y, z).color(r, g, b, a).endVertex();
                builder.vertex(matrix4f, x+width/2, y+height, z).color(r, g, b, a).endVertex();
                builder.vertex(matrix4f, x-width/2, y+height, z).color(r, g, b, a).endVertex();
            }
            case down -> {
                builder.vertex(matrix4f, x, y, z).color(r, g, b, a).endVertex();
                builder.vertex(matrix4f, x+width/2, y-height, z).color(r, g, b, a).endVertex();
                builder.vertex(matrix4f, x-width/2, y-height, z).color(r, g, b, a).endVertex();
            }

            case left -> {
                builder.vertex(matrix4f, x - bbHeight, y, z).color(r, g, b, a).endVertex();
                builder.vertex(matrix4f, x-  bbHeight-height, y-width/2, z).color(r, g, b, a).endVertex();
                builder.vertex(matrix4f, x-  bbHeight-height, y+width/2, z).color(r, g, b, a).endVertex();
            }

            case right -> {
                builder.vertex(matrix4f, x +bbHeight, y, z).color(r, g, b, a).endVertex();
                builder.vertex(matrix4f, x+bbHeight+ height, y-width/2, z).color(r, g, b, a).endVertex();
                builder.vertex(matrix4f, x+bbHeight+ height, y+width/2, z).color(r, g, b, a).endVertex();
            }
        }
    }
}
